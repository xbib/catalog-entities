package org.xbib.marc.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.Test;

/**
 * Test for the various JSON types.
 */
public class JsonTypesTest {
	@Test
	public void testObjectInt() {
		JsonObject o = new JsonObject();
		o.put("key", 1);
		assertEquals(1, o.getInt("key"));
		assertEquals(1.0, o.getDouble("key"), 0.0001f);
		assertEquals(1.0f, o.getFloat("key"), 0.0001f);
		assertEquals(1, o.getNumber("key"));
		assertEquals(1, o.get("key"));

		assertEquals(null, o.getString("key"));
		assertEquals("foo", o.getString("key", "foo"));
		assertFalse(o.isNull("key"));
	}

	@Test
	public void testObjectString() {
		JsonObject o = new JsonObject();
		o.put("key", "1");
		assertEquals(0, o.getInt("key"));
		assertEquals(0, o.getDouble("key"), 0.0001f);
		assertEquals(0f, o.getFloat("key"), 0.0001f);
		assertEquals(null, o.getNumber("key"));
		assertEquals("1", o.get("key"));
		assertFalse(o.isNull("key"));
	}

	@Test
	public void testObjectNull() {
		JsonObject o = new JsonObject();
		o.put("key", null);
		assertEquals(0, o.getInt("key"));
		assertEquals(0, o.getDouble("key"), 0.0001f);
		assertEquals(0f, o.getFloat("key"), 0.0001f);
		assertEquals(null, o.getNumber("key"));
		assertEquals(null, o.get("key"));
		assertTrue(o.isNull("key"));
	}

	@Test
	public void testArrayInt() {
		JsonArray o = new JsonArray(Arrays.asList((String) null, null, null,
				null));
		o.set(3, 1);
		assertEquals(1, o.getInt(3));
		assertEquals(1.0, o.getDouble(3), 0.0001f);
		assertEquals(1.0f, o.getFloat(3), 0.0001f);
		assertEquals(1, o.getNumber(3));
		assertEquals(1, o.get(3));

		assertEquals(null, o.getString(3));
		assertEquals("foo", o.getString(3, "foo"));
		assertFalse(o.isNull(3));
	}

	@Test
	public void testArrayString() {
		JsonArray o = new JsonArray(Arrays.asList((String) null, null, null,
				null));
		o.set(3, "1");
		assertEquals(0, o.getInt(3));
		assertEquals(0, o.getDouble(3), 0.0001f);
		assertEquals(0, o.getFloat(3), 0.0001f);
		assertEquals(null, o.getNumber(3));
		assertEquals("1", o.get(3));
		assertFalse(o.isNull(3));
	}

	@Test
	public void testArrayNull() {
		JsonArray o = new JsonArray(Arrays.asList((String) null, null, null,
				null));
		o.set(3, null);
		assertEquals(0, o.getInt(3));
		assertEquals(0, o.getDouble(3), 0.0001f);
		assertEquals(0, o.getFloat(3), 0.0001f);
		assertEquals(null, o.getNumber(3));
		assertEquals(null, o.get(3));
		assertTrue(o.isNull(3));
		assertTrue(o.has(3));
	}

	@Test
	public void testArrayBounds() {
		JsonArray o = new JsonArray(Arrays.asList((String) null, null, null,
				null));
		assertEquals(0, o.getInt(4));
		assertEquals(0, o.getDouble(4), 0.0001f);
		assertEquals(0, o.getFloat(4), 0.0001f);
		assertEquals(null, o.getNumber(4));
		assertEquals(null, o.get(4));
		assertFalse(o.isNull(4));
		assertFalse(o.has(4));
	}

	@Test
	public void testJsonArrayBuilder() throws IOException {
		JsonArray a = JsonArray.builder().value(true).value(1.0).value(1.0f)
				.value(1).value(new BigInteger("1234567890")).value("hi")
				.object().value("abc", 123).end().array().value(1).nul().end()
				.array(JsonArray.from(1, 2, 3))
				.object(JsonObject.builder().nul("a").done()).done();

		assertEquals(
				"[true,1.0,1.0,1,1234567890,\"hi\",{\"abc\":123},[1,null],[1,2,3],{\"a\":null}]",
				JsonWriter.string(a));
	}

	@Test
	public void testJsonObjectBuilder() throws IOException {
		JsonObject a = JsonObject
				.builder()
				.value("bool", true)
				.value("double", 1.0)
				.value("float", 1.0f)
				.value("int", 1)
				.value("bigint", new BigInteger("1234567890"))
				.value("string", "hi")
				.nul("null")
				.object("object")
				.value("abc", 123)
				.end()
				.array("array")
				.value(1)
				.nul()
				.end()
				.array("existingArray", JsonArray.from(1, 2, 3))
				.object("existingObject",
						JsonObject.builder().nul("a").done())
				.done();

		String[] bits = new String[] { "\"bigint\":1234567890", "\"int\":1",
				"\"string\":\"hi\"",
				"\"existingObject\":{\"a\":null}",
				"\"existingArray\":[1,2,3]", "\"object\":{\"abc\":123}",
				"\"bool\":true", "\"double\":1.0", "\"float\":1.0",
				"\"null\":null", "\"array\":[1,null]" };

		String s = JsonWriter.string(a);
		
		for (String bit : bits) {
			assertTrue(s.contains(bit));
		}
	}

	@Test(expected = IOException.class)
	public void testJsonArrayBuilderFailCantCloseRoot() throws IOException {
		JsonArray.builder().end();
	}
}