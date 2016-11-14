package org.xbib.catalog.entities;

import java.io.IOException;

/**
 * Worker interface.
 *
 * @param <R> request type
 */
public interface Worker<R> {
    /**
     * Execute a request.
     *
     * @param request request
     * @throws IOException if execution fails
     */
    void execute(R request) throws IOException;
}
