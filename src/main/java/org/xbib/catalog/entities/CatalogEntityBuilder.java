package org.xbib.catalog.entities;

import org.xbib.content.rdf.RdfContentBuilderProvider;
import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;
import org.xbib.content.settings.Settings;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordListener;
import org.xbib.marc.label.RecordLabel;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

/**
 * The catalog entity builder is a worker pool for generating bibliographic entities.
 *
 * [source,java]
 * --
 * class MyBuilder extends CatalogEntityBuilder {
 *
 *   MyBuilder(Setting settings) throws IOException {
 *      super(settings, listener);
 *   }
 *
 *   @Override
 *   protected void afterFinishState(CatalogEntityWorkerState state) {
 *     RdfXContentParams params = new RdfXContentParams();
 *     RdfContentBuilder<RdfXContentParams> builder = rdfXContentBuilder(params);
 *     builder.receive(state.getResource());
 *     String content = params.getGenerator().get();
 *     if (content != null) {
 *       logger.log(Level.FINE, "rdf=" + content);
 *     }
 *   }
 * }
 * --
 *
 */
public class CatalogEntityBuilder extends AbstractWorkerPool<MarcRecord>
        implements WorkerPool<MarcRecord>, MarcListener, MarcRecordListener {

    private static final Logger logger = Logger.getLogger(CatalogEntityBuilder.class.getName());

    private static final MarcRecord poison = MarcRecord.EMPTY;

    protected final Settings settings;

    protected final Set<String> unmapped;

    private final Map<String, Integer> mapped;

    private final AtomicLong checksum;

    private final Set<String> invalid;

    private final boolean isMapped;

    private CatalogEntitySpecification entitySpecification;

    private Marc.Builder marcBuilder;

    private IdentifierMapper identifierMapper;

    private ValueMapper valueMapper;

    private Classifier classifier;

    private Map<String, Object> facetElements;

    private Map<String, Resource> serialsMap;

    private Map<String, Boolean> missingSerials;

    private boolean enableChecksum;

    public CatalogEntityBuilder(Settings settings, WorkerPoolListener<WorkerPool<MarcRecord>> listener)
            throws IOException {
        super(settings.getAsInt("workers", Runtime.getRuntime().availableProcessors()), listener);
        this.settings = settings;
        this.unmapped = Collections.synchronizedSet(new TreeSet<>());
        this.mapped = Collections.synchronizedMap(new TreeMap<>());
        this.checksum = new AtomicLong();
        this.invalid = Collections.synchronizedSet(new TreeSet<>());
        this.isMapped = settings.containsSetting("elements");
        if (isMapped) {
            String packageName = settings.get("package");
            String elements = settings.get("elements");
            InputStream inputStream = null;
            try {
                URL url = new URL(elements);
                inputStream = url.openStream();
            } catch (MalformedURLException e) {
                logger.log(Level.FINER, e.getMessage(), e);
                inputStream = getClass().getResourceAsStream(elements);
            }
            Map<String, Object> params = settings.getAsStructuredMap();
            logger.log(Level.INFO, () -> MessageFormat.format("package:{0} elements:{1}",
                    packageName, elements));
            this.entitySpecification = new CatalogEntitySpecification(inputStream, new HashMap<>(), params, packageName);
            for (String key : entitySpecification.getMap().keySet()) {
                mapped.put(key, 0);
            }
            logger.log(Level.INFO, () -> MessageFormat.format("spec: map of {0} field keys with {1} entities",
                    entitySpecification.getMap().size(), entitySpecification.getEntities().size()));
            this.identifierMapper = setupIdentifierMapper(params);
            if (!identifierMapper.getMap().isEmpty()) {
                logger.log(Level.INFO, () -> MessageFormat.format("identifier mapper: {0} entries",
                        identifierMapper.getMap().size()));
            }
            this.valueMapper = setupValueMapper(params);
            if (!valueMapper.getMap("status").isEmpty()) {
                logger.log(Level.INFO, () -> MessageFormat.format("status mapper: {0} entries",
                        valueMapper.getMap("status").size()));
            }
            this.facetElements = setupFacets(params);
            if (!getFacetElements().isEmpty()) {
                logger.log(Level.INFO, () -> MessageFormat.format("facets: {0}",
                        getFacetElements()));
            }
            this.serialsMap = setupSerialsMap(params);
            this.missingSerials = new HashMap<>();
        }
        open();
    }

    @Override
    public CatalogEntityWorker newWorker() {
        return isMapped ? new CatalogEntityWorker(this) : new CatalogUnmappedEntityWorker(this);
    }

    @Override
    public void beginCollection() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("beginCollection");
        }
    }

    @Override
    public void beginRecord(String format, String type) {
        marcBuilder = Marc.builder()
                .lightweightRecord()
                .setFormat(format)
                .setType(type);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("beginRecord format=" + format + " type=" + type);
        }
    }

    @Override
    public void leader(String label) {
        marcBuilder.recordLabel(RecordLabel.builder().from(label.toCharArray()).build());
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("leader=" + label);
        }
    }

    @Override
    public void field(MarcField field) {
        marcBuilder.addField(field);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("field=" + field);
        }
    }

    @Override
    public void endRecord() {
        record(marcBuilder.buildRecord());
    }

    @Override
    public void record(MarcRecord marcRecord) {
        try {
            submit(marcRecord);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            close();
        }
    }

    @Override
    public void endCollection() {
        // nothing to do here
    }

    public CatalogEntitySpecification getEntitySpecification() {
        return entitySpecification;
    }

    public String getPackageName() {
        return entitySpecification != null ? entitySpecification.getPackageName() : null;
    }

    @Override
    public MarcRecord getPoison() {
        return poison;
    }

    public Map<IRI, RdfContentBuilderProvider<?>> contentBuilderProviders() {
        return new HashMap<>();
    }

    public boolean isEnableChecksum() {
        return enableChecksum;
    }

    public CatalogEntityBuilder setEnableChecksum(boolean enableChecksum) {
        this.enableChecksum = enableChecksum;
        return this;
    }

    public CatalogEntityBuilder addIdentifierMapper(String path) throws IOException {
        identifierMapper.load(getClass().getResource(path).openStream());
        return this;
    }

    public IdentifierMapper getIdentifierMapper() {
        return identifierMapper;
    }

    public CatalogEntityBuilder addStatusMapper(String path, String key) throws IOException {
        valueMapper.getMap(path, key);
        return this;
    }

    public ValueMapper getValueMapper() {
        return valueMapper;
    }

    public CatalogEntityBuilder addClassifier(String prefix, String isil, String classifierPath) throws IOException {
        if (classifier == null) {
            classifier = new Classifier();
        }
        URL url = new URL(classifierPath);
        InputStream in = url.openStream();
        if (in == null) {
            in = getClass().getResource(classifierPath).openStream();
        }
        classifier.load(in, isil, prefix);
        logger.log(Level.INFO, () -> MessageFormat.format("added classifications for {0} with size of {1}",
                isil, classifier.getMap().size()));
        return this;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public Map<String, Object> getFacetElements() {
        return facetElements;
    }

    public Map<String, Resource> getSerialsMap() {
        return serialsMap;
    }

    public Map<String, Boolean> getMissingSerials() {
        return missingSerials;
    }

    protected void beforeFinishState(CatalogEntityWorkerState state) throws IOException {
        // can be overriden
    }

    protected void afterFinishState(CatalogEntityWorkerState state) throws IOException {
        // can be overriden
    }

    public void mapped(String id, MarcField marcField) {
        String k = marcField.toKey();
        mapped.put(k, mapped.containsKey(k) ? mapped.get(k) + 1 : 1);
        k = marcField.toTagKey();
        mapped.put(k, mapped.containsKey(k) ? mapped.get(k) + 1 : 1);
    }

    public Map<String, Integer> getMapped() {
        return mapped;
    }

    public void unmapped(String id, MarcField marcField, String message) {
        String k = marcField.toKey();
        if (!unmapped.contains(k)) {
            logger.log(Level.WARNING, () -> MessageFormat.format("{0} : {1}", id, message));
            unmapped.add(k);
        }
    }

    public Set<String> getUnmapped() {
        return unmapped;
    }

    public void invalid(String id, MarcField marcField, String message) {
        String k = "\"" + marcField.toKey() + "\"";
        if (!invalid.contains(k)) {
            logger.log(Level.WARNING, () -> MessageFormat.format("{0} : {1}", id, message));
            invalid.add(k);
        }
    }

    public Set<String> getInvalid() {
        return invalid;
    }

    public void checksum(CRC32 crc32) {
        checksum.accumulateAndGet(crc32.getValue(), (n, m) -> n ^ m);
    }

    public long getChecksum() {
        return checksum.get();
    }


    protected IdentifierMapper setupIdentifierMapper(Map<String, Object> params) throws IOException {
        IdentifierMapper identifierMapper = new IdentifierMapper();
        ValueMapper valueMapper = new ValueMapper();
        Map<String, Object> sigel2isil =
                valueMapper.getMap("org/xbib/catalog/entities/mab/sigel2isil.json", "sigel2isil");
        identifierMapper.add(sigel2isil);
        if (params != null && params.containsKey("tab_sigel_url")) {
            // current sigel
            URL url = new URL((String) params.get("tab_sigel_url"));
            logger.log(Level.INFO, () -> MessageFormat.format("loading tab_sigel from {0}", url));
            identifierMapper.load(url.openStream());
            logger.log(Level.INFO, () -> MessageFormat.format("sigel2isil size = {0}, plus tab_sigel = {1}",
                    sigel2isil.size(), identifierMapper.getMap().size()));
        }
        return identifierMapper;
    }

    @SuppressWarnings("unchecked")
    protected ValueMapper setupValueMapper(Map<String, Object> params) throws IOException {
        ValueMapper valueMapper = new ValueMapper();
        valueMapper.getMap("org/xbib/catalog/entities/mab/status.json", "status");
        return valueMapper;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> setupFacets(Map<String, Object> params) throws IOException {
        ValueMapper valueMapper = new ValueMapper();
        valueMapper.getMap(settings.get("facets", "org/xbib/catalog/entities/marc/facets.json"), "facets");
        Map<String, Object> map = valueMapper.getMap("facets");
        if (map == null) {
            map = Collections.emptyMap();
        }
        return map;
    }

    protected Map<String, Resource> setupSerialsMap(Map<String, Object> params) {
        // can be overriden
        return null;
    }
}
