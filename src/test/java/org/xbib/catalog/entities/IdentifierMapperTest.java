package org.xbib.catalog.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 */
public class IdentifierMapperTest {

    @Test
    public void testIdentifierMapper() throws IOException {
        IdentifierMapper identifierMapper = new IdentifierMapper();
        ValueMaps valueMaps = new ValueMaps();
        Map<String, String> sigel2isil =
                valueMaps.getAssocStringMap("org/xbib/catalog/entities/mab/sigel2isil.json", "sigel2isil");
        assertFalse(sigel2isil.isEmpty());
        identifierMapper.add(sigel2isil);
        identifierMapper.load(new URL("http://index.hbz-nrw.de/alephxml/tab_sigel").openStream());
        assertFalse(identifierMapper.getMap().isEmpty());
        assertEquals("DE-38", identifierMapper.lookup("DE-38"));
        assertEquals("DE-38", identifierMapper.lookup("38"));
        assertEquals("DE-38", identifierMapper.lookup("K0001"));
        assertEquals("foobar", identifierMapper.lookup("foobar"));
        assertNull(identifierMapper.lookup(null));
    }
}
