package org.xbib.catalog.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
        ValueMaps valueMaps = new ValueMaps();
        Map<String, String> sigel2isil =
                valueMaps.getAssocStringMap("org/xbib/catalog/entities/mab/sigel2isil.json", "sigel2isil");
        assertFalse(sigel2isil.isEmpty());
        identifierMapper.add(sigel2isil);
        // a private hbz resource for Aleph Owner mapping
        URL url = new URL("http://index.hbz-nrw.de/alephxml/tab_sigel");
        try (InputStream inputStream = url.openStream()) {
            identifierMapper.load(inputStream);
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            identifierMapper.add("K0001", "38");
            identifierMapper.add("38", "DE-38");
        }
        assertFalse(identifierMapper.getMap().isEmpty());
        assertEquals("DE-38", identifierMapper.lookup("DE-38"));
        assertEquals("DE-38", identifierMapper.lookup("38"));
        assertEquals("DE-38", identifierMapper.lookup("K0001"));
        assertEquals("foobar", identifierMapper.lookup("foobar"));
        assertNull(identifierMapper.lookup(null));
    }
}
