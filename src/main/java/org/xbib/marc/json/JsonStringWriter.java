
package org.xbib.marc.json;

//@formatter:off

import java.io.IOException;

/**
 * JSON writer that emits JSON to a {@link String}.
 * 
 * Create this class using {@link JsonWriter#string()}.
 * 
 * <pre>
 * String json = JsonEmitter
 *     .indent("  ")
 *     .string()
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
//@formatter:on
public final class JsonStringWriter extends JsonWriterBase<JsonStringWriter> {
	JsonStringWriter(String indent) {
		super(new StringBuilder(), indent);
	}

	/**
	 * Completes this JSON writing session and returns the internal representation as a {@link String}.
	 */
	public String done() throws IOException {
		super.doneInternal();
		return appendable.toString();
	}
}