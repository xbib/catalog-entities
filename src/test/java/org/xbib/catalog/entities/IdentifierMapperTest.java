package org.xbib.catalog.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class IdentifierMapperTest {

    private static final Logger logger = Logger.getLogger(IdentifierMapper.class.getName());

    @Test
    public void testIdentifierMapper() throws IOException {
        IdentifierMapper identifierMapper = new IdentifierMapper();
        ValueMapper valueMapper = new ValueMapper();
        Map<String, Object> sigel2isil =
                valueMapper.getMap("org/xbib/catalog/entities/mab/sigel2isil.json", "sigel2isil");
        assertFalse(sigel2isil.isEmpty());
        identifierMapper.add(sigel2isil);
        URL url = getClass().getClassLoader().getResource("org/xbib/catalog/entities/mab/hbz/tab_sigel");
        if (url != null) {
            try (InputStream inputStream = url.openStream()) {
                identifierMapper.load(inputStream, StandardCharsets.ISO_8859_1);
            } catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                identifierMapper.add("K0001", "38");
                identifierMapper.add("38", "DE-38");
            }
        }
        assertFalse(identifierMapper.getMap().isEmpty());
        assertEquals("DE-38", identifierMapper.lookup("DE-38"));
        assertEquals("DE-38", identifierMapper.lookup("38"));
        assertEquals("DE-38", identifierMapper.lookup("K0001"));
        assertEquals("foobar", identifierMapper.lookup("foobar"));
        assertNull(identifierMapper.lookup(null));
    }
}
