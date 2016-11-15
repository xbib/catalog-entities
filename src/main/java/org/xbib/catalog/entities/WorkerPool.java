package org.xbib.catalog.entities;

import java.io.Flushable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A worker pool for processing request by a number of worker threads.
 * If worker threads exit early, they are removed and finished, not reused.
 * If no worker is left, the pool closes.
 *
 * @param <R> the request type
 */
abstract class WorkerPool<R> implements Flushable, AutoCloseable {

    private static final int DEFAULT_WAIT_SECONDS = 30;

    private final BlockingQueue<R> queue;

    private final ExecutorService executorService;

    private final List<Worker<R>> workers;

    private final List<Worker<R>> activeWorkers;

    private final Lock lock = new ReentrantLock();

    private final int workerCount;

    private final int waitSeconds;

    private final AtomicBoolean closed;

    public WorkerPool() {
        this(Runtime.getRuntime().availableProcessors(), DEFAULT_WAIT_SECONDS);
    }

    public WorkerPool(int workerCount) {
        this(workerCount, DEFAULT_WAIT_SECONDS);
    }

    public WorkerPool(int workerCount, int waitSeconds) {
        this.workerCount = workerCount;
        this.waitSeconds = waitSeconds;
        this.queue = new SynchronousQueue<>(true);
        this.executorService = Executors.newFixedThreadPool(workerCount);
        this.workers = new LinkedList<>();
        this.activeWorkers = new LinkedList<>();
        this.closed = new AtomicBoolean(true);
    }

    public WorkerPool<R> open() {
        if (closed.compareAndSet(true, false)) {
            for (int i = 0; i < workerCount; i++) {
                Worker<R> worker = newWorker();
                workers.add(worker);
                Wrapper wrapper = new Wrapper(worker);
                executorService.submit(wrapper);
            }
        }
        return this;
    }

    public boolean isClosed() {
        return closed.get();
    }

    public BlockingQueue<R> getQueue() {
        return queue;
    }

    public abstract R getPoison();

    protected abstract Worker<R> newWorker();

    public void addActiveWorker(Worker<R> worker) {
        lock.lock();
        try {
            activeWorkers.add(worker);
        } finally {
            lock.unlock();
        }
    }

    public void removeActiveWorker(Worker<R> worker) {
        lock.lock();
        try {
            activeWorkers.remove(worker);
        } finally {
            lock.unlock();
        }
    }

    public void submit(R request) {
        if (closed.get()) {
            throw new UncheckedIOException(new IOException("closed"));
        }
        lock.lock();
        try {
            if (activeWorkers.isEmpty()) {
                throw new UncheckedIOException(new IOException("no worker available"));
            }
            if (request.equals(getPoison())) {
                throw new UncheckedIOException(new IOException("ignoring poison"));
            }
            queue.put(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedIOException(new IOException(e));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void flush() throws IOException {
        // can be overriden, does nothing by default
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            lock.lock();
            try {
                // if workers are still active, send some poison pills
                for (Worker<R> worker : activeWorkers) {
                    queue.put(getPoison());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
            try {
                flush();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            try {
                executorService.shutdown();
                executorService.awaitTermination(waitSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UncheckedIOException(new IOException(e));
            }
        }
    }

    private class Wrapper implements Runnable {

        private final Logger logger = Logger.getLogger(Worker.class.getName());

        private final Worker<R> worker;

        private Wrapper(Worker<R> worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            try {
                logger.log(Level.INFO, "start of worker " + worker);
                addActiveWorker(worker);
                while (true) {
                    R request = getQueue().take();
                    if (request.equals(getPoison())) {
                        break;
                    }
                    worker.execute(request);
                }
            } catch (InterruptedException e) {
                // we got interrupted, this may lead to data loss. Clear interrupt state and log warning.
                Thread.currentThread().interrupt();
                logger.log(Level.WARNING, e.getMessage(), e);
            } catch (Exception e) {
                // unexpected exception
                logger.log(Level.SEVERE, e.getMessage(), e);
                throw new UncheckedIOException(new IOException(e));
            } finally {
                removeActiveWorker(worker);
                logger.log(Level.INFO, "end of worker " + worker);
            }
        }
    }
}
