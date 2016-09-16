package org.xbib.catalog.entities.mab;

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

    /*
               "OtherScript": [
              {
                 "6": "245-01/Cyrl",
                 "a": "Офiцiйний вiсник України",
                 "b": "щотижневий збiник актiв законодавства офiцiйне видання  = Официальный вестник Украины",
                 "c": "Мiнicтерство  Юстицiї України, Головне Державне Обьʹєднання Правової Iнформацiї та Пропаганди Правових Знань Мiнicтерства Юстицiї  України"
              },
              {
                 "0": [
                    "(DE-588)4061496-7",
                    "(DE-101)040614964"
                 ],
                 "4": "aut",
                 "6": "110-02/Cyrl",
                 "a": "Украïна"
              },
              {
                 "0": [
                    "(DE-588)5099225-9",
                    "(DE-101)940438542"
                 ],
                 "6": "710-03/Cyrl",
                 "a": "Уkpaїнa",
                 "b": "Мiнiстерство Юстицiї"
              },
              {
                 "6": "260-04/Cyrl",
                 "a": "Київ",
                 "b": "Голос"
              }
           ],
    */
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        // http://www.loc.gov/marc/bibliographic/ecbdcntf.html
        // find linkage in $6 [linking tag]-[occurrence number]/[script identification code]/[field orientation code]
        MarcField.Builder builder = MarcField.builder();
        boolean link = false;
        for (MarcField.Subfield subfield : field.getSubfields()) {
            if ("6".equals(subfield.getId())) {
                link = true;
                String value = subfield.getValue();
                // find linking tag, ignore occurrence number
                int pos = value.indexOf('-');
                if (pos > 0) {
                    String linkingtag = value.substring(0, pos);
                    String tag = linkingtag.substring(0, 3);
                    String indicator = linkingtag.substring(3);
                    builder.tag(tag).indicator(indicator);
                }
                pos = value.lastIndexOf('/');
                if (pos > 0) {
                    String script = value.substring(pos + 1);
                    builder.subfield("6", script);
                }
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
