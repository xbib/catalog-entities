package org.xbib.catalog.entities;

import org.xbib.catalog.entities.matching.endeavor.AuthoredWork;
import org.xbib.content.rdf.Literal;
import org.xbib.content.rdf.RdfContentBuilderProvider;
import org.xbib.content.rdf.RdfGraph;
import org.xbib.content.rdf.RdfGraphParams;
import org.xbib.content.rdf.Resource;
import org.xbib.content.rdf.internal.DefaultAnonymousResource;
import org.xbib.content.rdf.internal.DefaultLiteral;
import org.xbib.content.rdf.internal.DefaultRdfGraph;
import org.xbib.content.rdf.internal.DefaultResource;
import org.xbib.content.resource.IRI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class CatalogEntityWorkerState {

    private static final Logger logger = Logger.getLogger(CatalogEntityWorkerState.class.getName());

    private static final String LANGUAGE_FACET = "dc.language";

    private static final String DATE_FACET = "dc.date";

    private static final String TYPE_FACET = "dc.type";

    private static final String FORMAT_FACET = "dc.format";

    private final IRI itemIRI = IRI.create("item");

    private final CatalogEntityBuilder builder;

    private final RdfGraph<RdfGraphParams> graph;

    private final Map<IRI, RdfContentBuilderProvider<?>> builders;

    private final Map<String, Facet<String>> facets;

    private final Map<String, Sequence<Resource>> sequences;

    private final String packageName;

    private final AuthoredWork authoredWorkKey;
    private String systemIdentifier;
    private String recordIdentifier;
    private String format;
    private String type;
    private String label;
    private String isil;
    private IRI uid;
    private Resource resource;

    public CatalogEntityWorkerState(CatalogEntityBuilder builder) {
        this.builder = builder;
        this.graph = new DefaultRdfGraph();
        this.builders = builder.contentBuilderProviders();
        this.packageName = builder.getPackageName();
        this.facets = new HashMap<>();
        this.sequences = new HashMap<>();
        this.authoredWorkKey = new AuthoredWork();
    }

    public AuthoredWork getAuthoredWorkKey() {
        return authoredWorkKey;
    }

    public Map<String, Resource> getSerialsMap() {
        return builder.getSerialsMap();
    }

    public Map<String, Boolean> getMissingSerials() {
        return builder.getMissingSerials();
    }

    public String getLabel() {
        return label;
    }

    public CatalogEntityWorkerState setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    public CatalogEntityWorkerState setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
        return this;
    }

    public Resource getResource() throws IOException {
        if (!graph.getResources().hasNext()) {
            resource = new DefaultAnonymousResource();
            graph.receive(resource);
        }
        return resource;
    }

    public Resource getResource(IRI predicate) throws IOException {
        if (!graph.hasResource(predicate)) {
            DefaultResource resource = new DefaultAnonymousResource();
            graph.putResource(predicate, resource);
        }
        return graph.getResource(predicate);
    }

    public Resource getNextItemResource() {
        if (graph.hasResource(itemIRI)) {
            Resource resource = graph.removeResource(itemIRI);
            resource.setId(uid != null ? uid : resource.id());
            graph.putResource(resource.id(), resource);
        }
        uid = null;
        DefaultResource item = new DefaultAnonymousResource();
        graph.putResource(itemIRI, item);
        return item;
    }

    public Iterator<Resource> getResourceIterator() {
        return graph.getResources();
    }

    public String getIdentifier() {
        return systemIdentifier;
    }

    public CatalogEntityWorkerState setIdentifier(String identifier) {
        this.systemIdentifier = identifier;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public CatalogEntityWorkerState setFormat(String format) {
        this.format = format;
        return this;
    }

    public String getType() {
        return type;
    }

    public CatalogEntityWorkerState setType(String type) {
        this.type = type;
        return this;
    }

    public String getISIL() {
        return isil;
    }

    public CatalogEntityWorkerState setISIL(String isil) {
        this.isil = isil;
        return this;
    }

    public IRI getUID() {
        return uid;
    }

    public CatalogEntityWorkerState setUID(IRI uid) {
        this.uid = uid;
        return this;
    }

    public Map<String, Facet<String>> getFacets() {
        return facets;
    }

    public Map<String, Sequence<Resource>> getSequences() {
        return sequences;
    }

    public void finish() throws IOException {
                /*Iterator<Resource> it = graph().getResources();
        while (it.hasNext()) {
            Resource resource = it.next();
            if (recordIdentifier != null) {
                resource.setId(IRI.builder().fragment(recordIdentifier).build());
            }
        }
        super.finish();*/


        if (getResource().isDeleted()) {
            // no facet logic / sequence logic for deleted records
            return;
        }

        // update last item
        if (graph.hasResource(itemIRI)) {
            Resource resource = graph.removeResource(itemIRI);
            resource.setId(uid != null ? uid : resource.id());
            graph.putResource(resource.id(), resource);
        }

        // create sequences
        for (Sequence<Resource> sequence : sequences.values()) {
            String sequenceName = sequence.getName();
            if (sequenceName == null) {
                continue;
            }
            // split sequence name e.g. "dc.subject" --> "dc", "subject"
            String[] sequencePath = sequenceName.split("\\.");
            Resource resource = getResource();
            if (sequencePath.length > 1) {
                for (int i = 0; i < sequencePath.length - 1; i++) {
                    resource = resource.newResource(IRI.builder().path(sequencePath[i]).build());
                }
            }
            sequenceName = sequencePath[sequencePath.length - 1];
            IRI predicate = IRI.builder().path(sequenceName).build();
            for (Resource res : sequence.getResources()) {
                resource.add(predicate, res);
            }
        }
        sequences.clear();

        // create default facets
        Facet<String> languageFacet = facets.get(LANGUAGE_FACET);
        if (languageFacet == null) {
            CatalogEntity entity = builder.getEntitySpecification().getEntities().get(packageName + ".Language");
            if (entity != null) {
                languageFacet = entity.getDefaultFacet();
                if (languageFacet != null) {
                    facets.put(LANGUAGE_FACET, languageFacet);
                }
            }
        }
        Facet<String> formatFacet = facets.get(FORMAT_FACET);
        if (formatFacet == null) {
            CatalogEntity entity = builder.getEntitySpecification().getEntities().get(packageName + ".FormatCarrier");
            if (entity != null) {
                formatFacet = entity.getDefaultFacet();
                if (formatFacet != null) {
                    facets.put(FORMAT_FACET, formatFacet);
                }
            }
        }
        Facet<String> typeFacet = facets.get(TYPE_FACET);
        if (typeFacet == null) {
            CatalogEntity entity = builder.getEntitySpecification().getEntities().get(packageName + ".TypeMonograph");
            if (entity != null) {
                typeFacet = entity.getDefaultFacet();
                if (typeFacet != null) {
                    facets.put(TYPE_FACET, typeFacet);
                }
            }
        }
        YearFacet dateFacet = (YearFacet) facets.get(DATE_FACET);
        if (dateFacet == null) {
            CatalogEntity entity = builder.getEntitySpecification().getEntities().get(packageName + ".Date");
            if (entity != null) {
                dateFacet = (YearFacet) entity.getDefaultFacet();
                if (dateFacet != null) {
                    facets.put(DATE_FACET, dateFacet);
                }
            }
        }

        for (Facet<?> facet : facets.values()) {
            String facetName = facet.getName();
            if (facetName == null) {
                continue;
            }
            // split facet name e.g. "dc.date" --> "dc", "date"
            String[] facetPath = facetName.split("\\.");
            Resource resource = getResource();
            if (facetPath.length > 1) {
                for (int i = 0; i < facetPath.length - 1; i++) {
                    resource = resource.newResource(IRI.builder().path(facetPath[i]).build());
                }
            }
            facetName = facetPath[facetPath.length - 1];
            IRI predicate = IRI.builder().path(facetName).build();
            for (Object value : facet.getValues()) {
                Literal literal = new DefaultLiteral(value).type(facet.getType());
                try {
                    literal.object(); // provoke NumberFormatException to ensure numerical values
                    resource.add(predicate, literal);
                } catch (NumberFormatException e) {
                    logger.log(Level.FINEST, e.getMessage(), e);
                }
            }
        }
        facets.clear();

        if (graph.getResources() != null) {
            Iterator<Resource> it = graph.getResources();
            while (it.hasNext()) {
                Resource resource = it.next();
                if (builders != null) {
                    for (RdfContentBuilderProvider<?> provider : builders.values()) {
                        provider.newContentBuilder().receive(resource);
                    }
                }
            }
        }
    }

}
