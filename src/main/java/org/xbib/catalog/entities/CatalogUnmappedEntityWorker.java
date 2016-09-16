package org.xbib.catalog.entities;

import org.xbib.marc.MarcField;
import org.xbib.marc.MarcRecord;
import org.xbib.rdf.Resource;
import org.xbib.rdf.memory.BlankMemoryResource;

import java.io.IOException;

/**
 *
 */
public class CatalogUnmappedEntityWorker extends CatalogEntityWorker {

    CatalogUnmappedEntityWorker(CatalogEntityBuilder entityBuilder) {
        super(entityBuilder);
    }

    @Override
    public void build(MarcRecord marcRecord) throws IOException {
        for (MarcField field : marcRecord.getFields()) {
            Resource resource = new BlankMemoryResource();
            if (field.isControl()) {
                if ("001".equals(field.getTag())) {
                    getWorkerState().setRecordIdentifier(field.getValue());
                }
                resource.add("_", field.getValue());
            } else {
                for (MarcField.Subfield subfield : field.getSubfields()) {
                    resource.newResource(field.getIndicator().replaceAll("\\s", "_"))
                            .add(subfield.getId(), subfield.getValue());
                }
            }
            getWorkerState().getResource().add(field.getTag(), resource);
        }
    }
}
