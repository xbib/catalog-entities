package org.xbib.catalog.entities.marc.zdb.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class AlternateGraphicRepresentation extends CatalogEntity {

    public AlternateGraphicRepresentation(Map<String, Object> params) {
        super(params);
    }

    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        // http://www.loc.gov/marc/bibliographic/ecbdcntf.html
        // find linkage in $6 [linking tag]-[occurrence number]/[script identification code]/[field orientation code]
        MarcField.Builder builder = MarcField.builder();
        boolean link = false;
        for (MarcField.Subfield subfield : field.getSubfields()) {
            if ("6".equals(subfield.getId())) {
                String value = subfield.getValue();
                // find linking tag, ignore occurrence number
                int pos = value.indexOf('-');
                if (pos > 0) {
                    String linkingtag = value.substring(0, pos);
                    String tag = linkingtag.substring(0, 3);
                    String indicator = linkingtag.substring(3);
                    builder.tag(tag).indicator(indicator);
                }
                link = true;
            } else {
                builder.subfield(subfield.getId(), subfield.getValue());
            }
        }
        if (!link) {
            builder.tag(field.getTag()).indicator(field.getIndicator());
        }
        return super.transform(worker, builder.build());
    }
}
