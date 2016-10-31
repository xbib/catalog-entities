package org.xbib.catalog.entities;

import org.junit.Assert;
import org.junit.Test;
import org.xbib.marc.MarcField;

/**
 *
 */
public class CatalogEntitySpecificationTest extends Assert {

    @Test
    public void testMARCDefinition() throws Exception {
        CatalogEntity entity = new NullEntity();
        CatalogEntitySpecification specification = new CatalogEntitySpecification();
        specification.associate("100$01$abc", entity)
                .associate("100$02$abc", entity)
                .associate("100$02$def", entity)
                .associate("200$02$abc", entity);
        // test if second "100" is ignored
        assertEquals("{100=<null>, 200=<null>}", specification.getMap().toString());
        CatalogEntity e = specification.retrieve("100$01$abc");
        assertEquals("<null>", e.toString());
        // ignored?
        e = specification.retrieve("100$01$def");
        assertNotNull(e);
    }

    @Test
    public void testMARCSubfield() throws Exception {
        CatalogEntity entity = new NullEntity();
        CatalogEntitySpecification specification = new CatalogEntitySpecification();
        MarcField marcField = MarcField.builder().tag("100").indicator("01")
                .subfield("a", "Hello").subfield("b", "World").build();
        specification.associate(marcField, entity);
        CatalogEntity e = specification.retrieve(marcField.toTagKey());
        assertNotNull(e);
    }

    @Test
    public void testMARCControlField() throws Exception {
        CatalogEntity entity = new NullEntity();
        CatalogEntitySpecification specification = new CatalogEntitySpecification();
        MarcField marcField = MarcField.builder().tag("001").indicator("01")
                .value("123456").build();
        specification.associate(marcField, entity);
        CatalogEntity e = specification.retrieve(marcField.toTagKey());
        assertNotNull(e);
    }

}
