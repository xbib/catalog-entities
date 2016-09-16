package org.xbib.catalog.entities.unimarc.bib;

import org.xbib.catalog.entities.CatalogEntity;
import org.xbib.catalog.entities.CatalogEntityWorker;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Map;

public class RecordLabel extends CatalogEntity {

    private Map<String, Object> codes;

    private String predicate;

    @SuppressWarnings("unchecked")
    public RecordLabel(Map<String, Object> params) {
        super(params);
        this.codes = (Map<String, Object>) params.get("codes");
        this.predicate = params.containsKey("_predicate") ?
                (String) params.get("_predicate") : "leader";
    }

    @SuppressWarnings("unchecked")
    @Override
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        if (codes == null) {
            return super.transform(worker, field);
        }
        String value = getValue(field);
        for (Map.Entry<String, Object> entry : codes.entrySet()) {
            String k = entry.getKey();
            int pos = Integer.parseInt(k);
            Map<String, String> v = (Map<String, String>) codes.get(k);
            String code = value.length() > pos ? value.substring(pos, pos + 1) : "";
            if (v.containsKey(code)) {
                worker.getWorkerState().getResource().add(predicate, v.get(code));
            }
        }
        char ch5 = value.charAt(5);
        if (ch5 == 'd') {
            worker.getWorkerState().getResource().add("deleted", "true");
        }

        char ch6 = value.charAt(6);
        char ch7 = value.charAt(7);

        boolean isBook = (ch6 == 'a' || ch6 == 't') &&
                (ch7 == 'a' || ch7 == 'c' || ch7 == 'd' || ch7 == 'm');
        if (isBook) {
            worker.getWorkerState().getResource().add("type", "book");
        }

        boolean isComputerFile = ch6 == 'm';
        if (isComputerFile) {
            worker.getWorkerState().getResource().add("type", "computerfile");
        }

        boolean isMap = (ch6 == 'e' || ch6 == 'f');
        if (isMap) {
            worker.getWorkerState().getResource().add("type", "map");
        }

        boolean isMusic = (ch6 == 'c' || ch6 == 'd' || ch6 == 'i' || ch6 == 'j');
        if (isMusic) {
            worker.getWorkerState().getResource().add("type", "music");
        }

        boolean isContinuingResource = ch6 == 'a' &&
                (ch7 == 'b' || ch7 == 'i' || ch7 == 's');
        if (isContinuingResource) {
            worker.getWorkerState().getResource().add("type", "continuingresource");
        }

        boolean isVisualMaterial = (ch6 == 'g' || ch6 == 'k' || ch6 == 'o' || ch6 == 'r');
        if (isVisualMaterial) {
            worker.getWorkerState().getResource().add("type", "visualmaterial");
        }

        boolean isMixedMaterial = ch6 == 'p';
        if (isMixedMaterial) {
            worker.getWorkerState().getResource().add("type", "mixedmaterial");
        }
        return super.transform(worker, field);
    }
}
