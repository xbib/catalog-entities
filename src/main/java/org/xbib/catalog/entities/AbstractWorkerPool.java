package org.xbib.catalog.entities;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A worker pool for processing request by a number of worker threads.
 * If worker threads exit early, they are removed and finished, not reused.
 * If no worker is left, the pool closes.
 *
 * @param <R> the request type
 */
public abstract class AbstractWorkerPool<R> implements WorkerPool<R>, AutoCloseable {

    private static final int DEFAULT_WAIT_SECONDS = 30;

    private final BlockingQueue<R> queue;

    private final ThreadPoolWorkerExecutor executor;

    private final int workerCount;

    private final int waitSeconds;

    private final AtomicBoolean closed;

    private final CountDownLatch latch;

    private final Map<Runnable, Throwable> exceptions;

    private final WorkerPoolListener<WorkerPool<R>> listener;

    private final AtomicLong counter;

    public AbstractWorkerPool() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public AbstractWorkerPool(int workerCount) {
        this(workerCount, null);
    }

    public AbstractWorkerPool(int workerCount, WorkerPoolListener<WorkerPool<R>> listener) {
        this(workerCount, listener, DEFAULT_WAIT_SECONDS);
    }

    public AbstractWorkerPool(int workerCount, WorkerPoolListener<WorkerPool<R>> listener, int waitSeconds) {
        this.workerCount = workerCount;
        this.waitSeconds = waitSeconds;
        this.listener = listener;
        this.queue = new SynchronousQueue<>(true);
        this.executor = new ThreadPoolWorkerExecutor(workerCount);
        this.closed = new AtomicBoolean(true);
        this.latch = new CountDownLatch(workerCount);
        this.exceptions = new ConcurrentHashMap<>();
        this.counter = new AtomicLong();
    }

    @Override
    public WorkerPool<R> open() {
        if (closed.compareAndSet(true, false)) {
            for (int i = 0; i < workerCount; i++) {
                Worker<R> worker = newWorker();
                Wrapper wrapper = new Wrapper(worker);
                executor.submit(wrapper);
            }
        }
        return this;
    }

    @Override
    public AtomicLong getCounter() {
        return counter;
    }

    @Override
    public BlockingQueue<R> getQueue() {
        return queue;
    }

    @Override
    public void submit(R request) {
        if (closed.get()) {
            throw new UncheckedIOException(new IOException("closed"));
        }
        try {
            if (latch.getCount() == 0) {
                throw new UncheckedIOException(new IOException("no worker available"));
            }
            if (request.equals(getPoison())) {
                throw new UncheckedIOException(new IOException("ignoring poison"));
            }
            queue.put(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedIOException(new IOException(e));
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            while (latch.getCount() > 0) {
                try {
                    queue.put(getPoison());
                    // wait for latch being updated by other thread
                    Thread.sleep(50L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            try {
                executor.shutdown();
                executor.awaitTermination(waitSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UncheckedIOException(new IOException(e));
            } finally {
                if (listener != null) {
                    if (exceptions.isEmpty()) {
                        listener.success(this);
                    } else {
                        listener.failure(this, exceptions);
                    }
                }
            }
        }
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public Map<Runnable, Throwable> getExceptions() {
        return exceptions;
    }

    private class ThreadPoolWorkerExecutor extends ThreadPoolExecutor {

        private final Logger logger = Logger.getLogger(ThreadPoolWorkerExecutor.class.getName());

        ThreadPoolWorkerExecutor(int nThreads) {
            super(nThreads, nThreads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>());
        }

        /*
         * Examine Throwable or Error of a thread after execution just to log them.
         */
        @Override
        protected void afterExecute(Runnable runnable, Throwable terminationCause) {
            super.afterExecute(runnable, terminationCause);
            Throwable throwable = terminationCause;
            if (throwable == null && runnable instanceof Future<?>) {
                try {
                    Future<?> future = (Future<?>) runnable;
                    if (future.isDone()) {
                        future.get();
                    }
                } catch (CancellationException ce) {
                    logger.log(Level.FINEST, ce.getMessage(), ce);
                    throwable = ce;
                } catch (ExecutionException ee) {
                    logger.log(Level.FINEST, ee.getMessage(), ee);
                    throwable = ee.getCause();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.FINEST, ie.getMessage(), ie);
                }
            }
            if (throwable != null) {
                logger.log(Level.SEVERE, throwable.getMessage(), throwable);
                exceptions.put(runnable, throwable);
            }
        }
    }

    private class Wrapper implements Runnable {

        private final Logger logger = Logger.getLogger(Worker.class.getName());

        private final Worker<R> worker;

        private int counter;

        private Wrapper(Worker<R> worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            R request = null;
            try {
                logger.log(Level.INFO,  () -> MessageFormat.format("start of worker {0}", worker));
                while (true) {
                    request = getQueue().take();
                    if (getPoison().equals(request)) {
                        break;
                    }
                    worker.execute(request);
                    counter++;
                }
            } catch (InterruptedException e) {
                // we got interrupted, this may lead to data loss. Clear interrupt state and log warning.
                Thread.currentThread().interrupt();
                logger.log(Level.WARNING, e.getMessage(), e);
                exceptions.put(this, e);
            } catch (Exception e) {
                // catch unexpected exception. Throwables, Errors are examined in afterExecute.
                logger.log(Level.SEVERE, e.getMessage(), e);
                exceptions.put(this, e);
                if (closed.get()) {
                    try {
                        getQueue().poll(1, TimeUnit.MINUTES);
                    } catch (InterruptedException e2) {
                        Thread.currentThread().interrupt();
                        logger.log(Level.WARNING, e2.getMessage(), e2);
                    }
                }
                throw new UncheckedIOException(new IOException(e));
            } finally {
                latch.countDown();
                if (getPoison().equals(request)) {
                    logger.log(Level.INFO, () -> MessageFormat.format("end of worker {0} {1}",
                            worker, "(completed, " + counter + " requests)"));
                } else {
                    logger.log(Level.WARNING, () -> MessageFormat.format("end of worker {0} {1}",
                            worker, "(abnormal termination after " + counter + " requests)"));
                }
            }
        }
    }
}
