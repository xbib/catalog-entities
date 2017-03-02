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
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    private static final MarcRecord poison = MarcRecord.emptyRecord();

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

    private FieldConsolidationMapper fieldConsolidationMapper;

    private Map<String, Object> facetElements;

    private Map<String, Resource> serialsMap;

    private Map<String, Boolean> missingSerials;

    private boolean enableChecksum;

    public CatalogEntityBuilder(Settings settings, WorkerPoolListener<WorkerPool<MarcRecord>> listener)
            throws IOException {
        this(CatalogEntityBuilder.class.getClassLoader(), settings, listener);
    }

    @SuppressWarnings("unchecked")
    public CatalogEntityBuilder(ClassLoader classLoader, Settings settings, WorkerPoolListener<WorkerPool<MarcRecord>> listener)
            throws IOException {
        super(settings.getAsInt("workers", Runtime.getRuntime().availableProcessors()), listener);
        this.settings = settings;
        this.unmapped = Collections.synchronizedSet(new TreeSet<>());
        this.mapped = Collections.synchronizedMap(new TreeMap<>());
        this.invalid = Collections.synchronizedSet(new TreeSet<>());
        this.checksum = new AtomicLong();
        this.isMapped = settings.containsSetting("elements");
        if (isMapped) {
            String packageName = settings.get("package");
            String elements = settings.get("elements");
            logger.log(Level.INFO, () -> MessageFormat.format("package:{0} elements:{1}",
                    packageName, elements));
            URL url = classLoader.getResource(elements);
            if (url == null) {
                url = new URL(elements);
            }
            try (InputStream inputStream = url.openStream()) {
                this.entitySpecification = new CatalogEntitySpecification(inputStream, new HashMap<>(),
                        settings.getAsStructuredMap(), packageName);
            }
            if (settings.containsSetting("additional-elements")) {
                Map<String, Map<String, Object>> map =
                        (Map<String, Map<String, Object>>) settings.getAsStructuredMap().get("additional-elements");
                entitySpecification.addElements(map, packageName);
            }
            for (String key : entitySpecification.getMap().keySet()) {
                mapped.put(key, 0);
            }
            logger.log(Level.INFO, () -> MessageFormat.format("spec: map of {0} field keys with {1} entities",
                    entitySpecification.getMap().size(), entitySpecification.getEntities().size()));
            this.identifierMapper = setupIdentifierMapper(settings);
            if (!identifierMapper.getMap().isEmpty()) {
                logger.log(Level.INFO, () -> MessageFormat.format("identifier mapper: {0} entries",
                        identifierMapper.getMap().size()));
            }
            this.valueMapper = setupValueMapper();
            if (!valueMapper.getMap("status").isEmpty()) {
                logger.log(Level.INFO, () -> MessageFormat.format("status mapper: {0} entries",
                        valueMapper.getMap("status").size()));
            }
            this.facetElements = setupFacets(settings);
            if (facetElements != null && !facetElements.isEmpty()) {
                logger.log(Level.INFO, () -> MessageFormat.format("facets: {0}",
                        getFacetElements()));
            }
            this.serialsMap = setupSerialsMap(settings);
            this.missingSerials = new HashMap<>();
            this.fieldConsolidationMapper = setupFieldConsolidation(settings);
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

    public FieldConsolidationMapper getFieldConsolidationMapper() {
        return fieldConsolidationMapper;
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


    protected IdentifierMapper setupIdentifierMapper(Settings settings) throws IOException {
        IdentifierMapper identifierMapper = new IdentifierMapper();
        ValueMapper valueMapper = new ValueMapper();
        Map<String, Object> sigel2isil =
                valueMapper.getMap("org/xbib/catalog/entities/mab/sigel2isil.json", "sigel2isil");
        identifierMapper.add(sigel2isil);
        URL url = getClass().getClassLoader().getResource(settings.get("tab_sigel",
                "org/xbib/catalog/entities/mab/hbz/tab_sigel"));
        try {
            if (url != null) {
                identifierMapper.load(url.openStream(), StandardCharsets.ISO_8859_1);
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "unable to load tab_sigel from classpath");
        }
        if (settings.containsSetting("tab_sigel_url")) {
            // current sigel
            url = new URL(settings.get("tab_sigel_url"));
            identifierMapper.load(url.openStream(), StandardCharsets.ISO_8859_1);
            logger.log(Level.INFO, () -> MessageFormat.format("sigel2isil size = {0}, plus tab_sigel = {1}",
                    sigel2isil.size(), identifierMapper.getMap().size()));
        }
        return identifierMapper;
    }

    @SuppressWarnings("unchecked")
    protected ValueMapper setupValueMapper() throws IOException {
        ValueMapper valueMapper = new ValueMapper();
        valueMapper.getMap("org/xbib/catalog/entities/mab/status.json", "status");
        return valueMapper;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> setupFacets(Settings settings) throws IOException {
        ValueMapper valueMapper = new ValueMapper();
        // embedded values?
        Map<String, Object> map = settings.getAsStructuredMap();
        if (map.containsKey("facets")) {
            Object object = map.get("facets");
            if (object instanceof Map) {
                map = valueMapper.getMap(settings.getAsSettings("facets").getAsMap(), "facets");
            } else {
                map = valueMapper.getMap(settings.get("facets", "org/xbib/catalog/entities/marc/facets.json"), "facets");
            }
        } else {
            map = valueMapper.getMap(settings.get("facets", "org/xbib/catalog/entities/marc/facets.json"), "facets");
        }
        return map;
    }

    protected FieldConsolidationMapper setupFieldConsolidation(Settings settings) throws IOException {
        return new FieldConsolidationMapper(settings);
    }

    protected Map<String, Resource> setupSerialsMap(Settings settings) {

        // can be overriden
        return null;
    }
}
