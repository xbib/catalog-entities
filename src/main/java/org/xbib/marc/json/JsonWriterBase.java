
package org.xbib.marc.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;

class JsonWriterBase<SELF extends JsonWriterBase<SELF>> {
	protected final Appendable appendable;
	private Stack<Boolean> states = new Stack<Boolean>();
	private boolean first = true;
	private boolean inObject;

	/**
	 * Sequence to use for indenting.
	 */
	private String indentString;

	/**
	 * Current indent amount.
	 */
	private int indent = 0;

	JsonWriterBase(Appendable appendable, String indent) {
		this.appendable = appendable;
		this.indentString = indent;
	}

	/**
	 * This is guaranteed to be safe as the type of "this" will always be the
	 * type of "SELF".
	 */
	@SuppressWarnings("unchecked")
	private SELF castThis() {
		return (SELF) this;
	}

	public SELF array(Collection<?> c) throws IOException {
		return array(null, c);
	}

	public SELF array(String key, Collection<?> c) throws IOException {
		if (key == null)
			array();
		else
			array(key);

		for (Object o : c) {
			value(o);
		}

		return end();
	}

	public SELF object(Map<?, ?> map) throws IOException {
		return object(null, map);
	}

	public SELF object(String key, Map<?, ?> map) throws IOException {
		if (key == null)
			object();
		else
			object(key);

		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object o = entry.getValue();
			if (!(entry.getKey() instanceof String))
				throw new IOException("Invalid key type for map: "
						+ (entry.getKey() == null ? "null" : entry.getKey()
								.getClass()));
			String k = (String) entry.getKey();
			value(k, o);
		}

		return end();
	}

	public SELF nul() throws IOException {
		preValue();
		raw("null");
		return castThis();
	}

	public SELF nul(String key) throws IOException {
		preValue(key);
		raw("null");
		return castThis();
	}

	public SELF value(Object o) throws IOException {
		if (o == null)
			return nul();
		else if (o instanceof String)
			return value((String) o);
		else if (o instanceof Number)
			return value(((Number) o));
		else if (o instanceof Boolean)
			return value((boolean) (Boolean) o);
		else if (o instanceof Collection)
			return array((Collection<?>) o);
		else if (o instanceof Map)
			return object((Map<?, ?>) o);
		else if (o.getClass().isArray()) {
			int length = Array.getLength(o);
			array();
			for (int i = 0; i < length; i++)
				value(Array.get(o, i));
			return end();
		} else
			throw new IOException("Unable to handle type: "
					+ o.getClass());
	}

	public SELF value(String key, Object o) throws IOException {
		if (o == null)
			return nul(key);
		else if (o instanceof String)
			return value(key, (String) o);
		else if (o instanceof Number)
			return value(key, (Number) o);
		else if (o instanceof Boolean)
			return value(key, (boolean) (Boolean) o);
		else if (o instanceof Collection)
			return array(key, (Collection<?>) o);
		else if (o instanceof Map)
			return object(key, (Map<?, ?>) o);
		else if (o.getClass().isArray()) {
			int length = Array.getLength(o);
			array(key);
			for (int i = 0; i < length; i++)
				value(Array.get(o, i));
			return end();
		} else
			throw new IOException("Unable to handle type: "
					+ o.getClass());
	}

	public SELF value(String s) throws IOException {
		if (s == null)
			return nul();
		preValue();
		emitStringValue(s);
		return castThis();
	}

	public SELF value(int i) throws IOException {
		preValue();
		raw(Integer.toString(i));
		return castThis();
	}

	public SELF value(long l) throws IOException {
		preValue();
		raw(Long.toString(l));
		return castThis();
	}

	public SELF value(boolean b) throws IOException {
		preValue();
		raw(Boolean.toString(b));
		return castThis();
	}

	public SELF value(double d) throws IOException {
		preValue();
		raw(Double.toString(d));
		return castThis();
	}

	public SELF value(float d) throws IOException {
		preValue();
		raw(Float.toString(d));
		return castThis();
	}

	public SELF value(Number n) throws IOException {
		preValue();
		if (n == null)
			raw("null");
		else
			raw(n.toString());
		return castThis();
	}

	public SELF value(String key, String s) throws IOException {
		if (s == null)
			return nul(key);
		preValue(key);
		emitStringValue(s);
		return castThis();
	}

	public SELF value(String key, int i) throws IOException {
		preValue(key);
		raw(Integer.toString(i));
		return castThis();
	}

	public SELF value(String key, long l) throws IOException {
		preValue(key);
		raw(Long.toString(l));
		return castThis();
	}

	public SELF value(String key, boolean b) throws IOException {
		preValue(key);
		raw(Boolean.toString(b));
		return castThis();
	}

	public SELF value(String key, double d) throws IOException {
		preValue(key);
		raw(Double.toString(d));
		return castThis();
	}

	public SELF value(String key, float d) throws IOException {
		preValue(key);
		raw(Float.toString(d));
		return castThis();
	}

	public SELF value(String key, Number n) throws IOException {
		if (n == null)
			return nul(key);
		preValue(key);
		raw(n.toString());
		return castThis();
	}

	public SELF array() throws IOException {
		preValue();
		states.push(inObject);
		inObject = false;
		first = true;
		raw('[');
		return castThis();
	}

	public SELF object() throws IOException {
		preValue();
		states.push(inObject);
		inObject = true;
		first = true;
		raw('{');
		if (indentString != null) {
			indent++;
			appendNewLine();
		}
		return castThis();
	}

	public SELF array(String key) throws IOException {
		preValue(key);
		states.push(inObject);
		inObject = false;
		first = true;
		raw('[');
		return castThis();
	}

	public SELF object(String key) throws IOException {
		preValue(key);
		states.push(inObject);
		inObject = true;
		first = true;
		raw('{');
		if (indentString != null) {
			indent++;
			appendNewLine();
		}
		return castThis();
	}

	public SELF end() throws IOException {
		if (states.size() == 0)
			throw new IOException("Invalid call to end()");

		if (inObject) {
			if (indentString != null) {
				indent--;
				appendNewLine();
				appendIndent();
			}
			raw('}');
		} else {
			raw(']');
		}

		first = false;
		inObject = states.pop();
		return castThis();
	}

	/**
	 * Ensures that the object is in the finished state.
	 * 
	 * @throws IOException
	 *             if the written JSON is not properly balanced, ie: all arrays
	 *             and objects that were started have been properly ended.
	 */
	protected void doneInternal() throws IOException {
		if (states.size() > 0)
			throw new IOException(
					"Unclosed JSON objects and/or arrays when closing writer");
		if (first)
			throw new IOException(
					"Nothing was written to the JSON writer");
	}

	private void appendIndent() throws IOException {
		for (int i = 0; i < indent; i++) {
			raw(indentString);
		}
	}

	private void appendNewLine() throws IOException {
		raw('\n');
	}

	private void raw(String s) throws IOException {
		appendable.append(s);
	}

	private void raw(char c) throws IOException {
    	appendable.append(c);
	}

	private void pre() throws IOException {
		if (first) {
			first = false;
		} else {
			if (states.size() == 0)
				throw new IOException(
						"Invalid call to emit a value in a finished JSON writer");
			raw(',');
			if (indentString != null && inObject) {
				appendNewLine();
			}
		}
	}

	private void preValue() throws IOException {
		if (inObject)
			throw new IOException(
					"Invalid call to emit a keyless value while writing an object");

		pre();
	}

	private void preValue(String key) throws IOException {
		if (!inObject)
			throw new IOException(
					"Invalid call to emit a key value while not writing an object");

		pre();

		if (indentString != null) {
			appendIndent();
		}
		emitStringValue(key);
		raw(':');
	}

	/**
	 * Emits a quoted string value, escaping characters that are required to be
	 * escaped.
	 */
	private void emitStringValue(String s) throws IOException {
		raw('"');
		char b = 0, c = 0;
		for (int i = 0; i < s.length(); i++) {
			b = c;
			c = s.charAt(i);

			switch (c) {
			case '\\':
			case '"':
				raw('\\');
				raw(c);
				break;
			case '/':
				// Special case to ensure that </script> doesn't appear in JSON
				// output
				if (b == '<')
					raw('\\');
				raw(c);
				break;
			case '\b':
				raw("\\b");
				break;
			case '\t':
				raw("\\t");
				break;
			case '\n':
				raw("\\n");
				break;
			case '\f':
				raw("\\f");
				break;
			case '\r':
				raw("\\r");
				break;
			default:
				if (shouldBeEscaped(c)) {
					String t = "000" + Integer.toHexString(c);
					raw("\\u" + t.substring(t.length() - "0000".length()));
				} else {
					raw(c);
				}
			}
		}

		raw('"');
	}

	/**
	 * json.org spec says that all control characters must be escaped.
	 */
	private boolean shouldBeEscaped(char c) {
		return c < ' ' || (c >= '\u0080' && c < '\u00a0')
				|| (c >= '\u2000' && c < '\u2100');
	}
}