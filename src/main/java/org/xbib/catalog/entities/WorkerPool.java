package org.xbib.catalog.entities;

import java.io.Closeable;
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
 * If worker threads exit early, they are removed. If no worker is left,
 * the pool closes.
 *
 * @param <R> the request type
 */
public abstract class WorkerPool<R> implements Closeable {

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
            if (!activeWorkers.isEmpty() && !request.equals(getPoison())) {
                queue.put(request);
            } else {
                throw new UncheckedIOException(new IOException("no worker available"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UncheckedIOException(new IOException(e));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws IOException {
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
            for (Worker<R> worker : workers) {
                worker.close();
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
            } catch (Exception t) {
                // unexpected exception
                logger.log(Level.SEVERE, t.getMessage(), t);
                throw new UncheckedIOException(new IOException(t));
            } finally {
                removeActiveWorker(worker);
                logger.log(Level.INFO, "end of worker " + worker);
            }
        }
    }
}
