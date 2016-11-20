package org.xbib.catalog.entities;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interface for a worker pool.
 *
 * @param <R> the request paramter type
 */
public interface WorkerPool<R>  {

    WorkerPool<R> open();

    AtomicLong getCounter();

    BlockingQueue<R> getQueue();

    R getPoison();

    Worker<R> newWorker();

    void submit(R request);

}
