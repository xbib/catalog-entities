package org.xbib.marc.json;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;

//@formatter:off
/**
 * JSON writer that emits JSON to a {@link Appendable}.
 * 
 * Create this class with {@link JsonWriter#on(Appendable)} or
 * {@link JsonWriter#on(OutputStream)}.
 * 
 * <pre>
 * OutputStream out = ...;
 * JsonEmitter
 *     .indent("  ")
 *     .on(out)
 *     .object()
 *         .array("a")
 *             .value(1)
 *             .value(2)
 *         .end()
 *         .value("b", false)
 *         .value("c", true)
 *     .end()
 * .done();
 * </pre>
 */
// @formatter:on
public final class JsonAppendableWriter extends
		JsonWriterBase<JsonAppendableWriter>  {
	JsonAppendableWriter(Appendable appendable, String indent) {
		super(appendable, indent);
	}

	/**
	 * Closes this JSON writer and flushes the underlying {@link Appendable} if
	 * it is also {@link Flushable}.
	 * 
	 * @throws IOException
	 *             if the underlying {@link Flushable} {@link Appendable} failed
	 *             to flush.
	 */
	public void done() throws IOException {
		super.doneInternal();
		if (appendable instanceof Flushable)
			((Flushable) appendable).flush();
	}
}