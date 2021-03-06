package org.xbib.catalog.entities;

import org.xbib.content.rdf.Literal;
import org.xbib.content.rdf.Resource;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CatalogEntity {

    private final Map<String, Object> params;

    public CatalogEntity(Map<String, Object> params) {
        this.params = params;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Transform field.
     *
     * @param worker the worker
     * @param field  the field
     * @return null if processing has been completed and should not continue at this point,
     * another entity if processing should continue
     * @throws IOException if transformation fails
     */
    public CatalogEntity transform(CatalogEntityWorker worker, MarcField field) throws IOException {
        worker.append(worker.getWorkerState().getResource(), field, this);
        return this;
    }

    /**
     * Transform or split value.
     *
     * @param worker            the worker
     * @param resourcePredicate the resource predicate
     * @param resource          the resource
     * @param property          the property
     * @param value             the value
     * @return the transformed value(s) as a list
     * @throws IOException if transformation fails
     */
    public List<String> transform(CatalogEntityWorker worker, String resourcePredicate,
                                  Resource resource, String property, String value) throws IOException {
        return Collections.singletonList(value);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCodes() {
        return (Map<String, Object>) getParams().get("codes");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getFacetCodes() {
        return (Map<String, Object>) getParams().get("facetcodes");
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getRegexes() {
        return (Map<String, Object>) getParams().get("regexes");
    }

    public Resource getResource(CatalogEntityWorker worker) throws IOException {
        return worker.getWorkerState().getResource();
    }

    protected String getFacetName() {
        return null;
    }

    public TermFacet getDefaultFacet() {
        Object def = getParams().get("_default");
        return def != null ?
                new TermFacet().setName(getFacetName()).setType(Literal.STRING).addValue(def.toString()) :
                new TermFacet();
    }

    protected void facetize(CatalogEntityWorker worker, String value) {
        CatalogEntityWorkerState state = worker.getWorkerState();
        state.getFacets().putIfAbsent(getFacetName(), new TermFacet().setName(getFacetName()).setType(Literal.STRING));
        state.getFacets().get(getFacetName()).addValue(value);
    }

    protected String getValue(MarcField marcField) {
        if (marcField.getValue() != null) {
            return marcField.getValue();
        } else {
            return marcField.getSubfields().isEmpty() ? null : marcField.getSubfields().getLast().getValue();
        }
    }
}
