package org.xbib.catalog.entities;

import org.xbib.content.rdf.RdfContentBuilderProvider;
import org.xbib.content.rdf.Resource;
import org.xbib.content.resource.IRI;
import org.xbib.marc.Marc;
import org.xbib.marc.MarcField;
import org.xbib.marc.MarcListener;
import org.xbib.marc.MarcRecord;
import org.xbib.marc.MarcRecordListener;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
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
 *
 */
public class CatalogEntityBuilder extends AbstractWorkerPool<MarcRecord>
        implements WorkerPool<MarcRecord>, MarcListener, MarcRecordListener, Closeable {

    private static final Logger logger = Logger.getLogger(CatalogEntityBuilder.class.getName());

    private static final MarcRecord poison = MarcRecord.EMPTY;

    protected final Set<String> unmapped;
    private final Map<String, Integer> mapped;
    private final AtomicLong checksum;
    private final Set<String> invalid;
    private final boolean isMapped;
    private CatalogEntitySpecification entitySpecification;
    private Marc.Builder marcBuilder;
    private IdentifierMapper identifierMapper;
    private StatusCodeMapper statusMapper;
    private Classifier classifier;
    private Map<String, Resource> serialsMap;
    private Map<String, Boolean> missingSerials;
    private boolean enableChecksum;
    private volatile boolean errorstate;

    public CatalogEntityBuilder(String packageName, URL url) throws IOException {
        this(packageName, Runtime.getRuntime().availableProcessors(), url, new HashMap<>(), true);
    }

    public CatalogEntityBuilder(String packageName, URL url, WorkerPoolListener<WorkerPool<MarcRecord>> listener)
            throws IOException {
        this(packageName, Runtime.getRuntime().availableProcessors(), url, new HashMap<>(), true, listener);
    }

    public CatalogEntityBuilder(String packageName, int workers, URL url) throws IOException {
        this(packageName, workers, url, new HashMap<>(), true);
    }

    public CatalogEntityBuilder(String packageName, int workers, URL url,
                                WorkerPoolListener<WorkerPool<MarcRecord>> listener) throws IOException {
        this(packageName, workers, url, new HashMap<>(), true, listener);
    }

    public CatalogEntityBuilder(String packageName, URL url, boolean mapped) throws IOException {
        this(packageName, Runtime.getRuntime().availableProcessors(), url, new HashMap<>(), mapped);
    }

    public CatalogEntityBuilder(String packageName, int workers, URL url, boolean mapped) throws IOException {
        this(packageName, workers, url, new HashMap<>(), mapped);
    }

    public CatalogEntityBuilder(String packageName, int workers, URL url, Map<String, Object> params, boolean isMapped)
            throws IOException {
        this(packageName, workers, url, params, isMapped, null);
    }

    public CatalogEntityBuilder(String packageName, int workers, URL url, Map<String, Object> params,
        boolean isMapped, WorkerPoolListener<WorkerPool<MarcRecord>> listener)
            throws IOException {
        super(workers, listener);
        this.unmapped = Collections.synchronizedSet(new TreeSet<>());
        this.mapped = Collections.synchronizedMap(new TreeMap<>());
        this.checksum = new AtomicLong();
        this.invalid = Collections.synchronizedSet(new TreeSet<>());
        this.isMapped = isMapped;
        logger.log(Level.INFO, MessageFormat.format("workers:{1} mapped:{2} package:{0} spec:{3}",
                packageName, workers, isMapped, url));
        if (isMapped) {
            this.entitySpecification = new CatalogEntitySpecification(url, new HashMap<>(), params, packageName);
            for (String key : entitySpecification.getMap().keySet()) {
                mapped.put(key, 0);
            }
            logger.log(Level.INFO, MessageFormat.format("spec: map of {0} field keys with {1} entities",
                    entitySpecification.getMap().size(), entitySpecification.getEntities().size()));
            this.identifierMapper = setupIdentifierMapper(params);
            if (!identifierMapper.getMap().isEmpty()) {
                logger.log(Level.INFO, MessageFormat.format("identifier mapper: {0} entries",
                        identifierMapper.getMap().size()));
            }
            this.statusMapper = setupStatusMapper(params);
            if (!statusMapper.getMap().isEmpty()) {
                logger.log(Level.INFO, MessageFormat.format("status mapper: {0} entries",
                        statusMapper.getMap().size()));
            }
            this.serialsMap = setupSerialsMap(params);
            this.missingSerials = new HashMap<>();
        }
        open();
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

    public CatalogEntityBuilder addStatusMapper(String path) throws IOException {
        statusMapper.load(path);
        return this;
    }

    public StatusCodeMapper getStatusMapper() {
        return statusMapper;
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
        logger.log(Level.INFO, MessageFormat.format("added classifications for {0} with size of {1}",
                isil, classifier.getMap().size()));
        return this;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public Map<String, Resource> getSerialsMap() {
        return serialsMap;
    }

    public Map<String, Boolean> getMissingSerials() {
        return missingSerials;
    }

    protected void beforeFinishState(CatalogEntityWorkerState state) {
        // can be overriden
    }

    protected void afterFinishState(CatalogEntityWorkerState state) {
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
            logger.log(Level.WARNING, id + " : " + message);
            unmapped.add(k);
        }
    }

    public Set<String> getUnmapped() {
        return unmapped;
    }

    public void invalid(String id, MarcField marcField, String message) {
        String k = "\"" + marcField.toKey() + "\"";
        if (!invalid.contains(k)) {
            logger.log(Level.WARNING, id + " : " + message);
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

    @Override
    public CatalogEntityWorker newWorker() {
        return isMapped ? new CatalogEntityWorker(this) : new CatalogUnmappedEntityWorker(this);
    }

    @Override
    public void close() {
        logger.info("closing");
        super.close();
        logger.info("closed");
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
        marcBuilder.leader(label);
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
        if (errorstate) {
            return;
        }
        try {
            submit(marcRecord);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            errorstate = true;
            close();
        }
    }

    @Override
    public void endCollection() {
        // nothing to do here
    }

    private IdentifierMapper setupIdentifierMapper(Map<String, Object> params) throws IOException {
        IdentifierMapper identifierMapper = new IdentifierMapper();
        ValueMaps valueMaps = new ValueMaps();
        Map<String, String> sigel2isil =
                valueMaps.getAssocStringMap("org/xbib/catalog/entities/mab/sigel2isil.json", "sigel2isil");
        identifierMapper.add(sigel2isil);
        if (params != null && params.containsKey("tab_sigel_url")) {
            // current sigel
            URL url = new URL((String) params.get("tab_sigel_url"));
            logger.log(Level.INFO, MessageFormat.format("loading tab_sigel from {0}", url));
            identifierMapper.load(url.openStream());
            logger.log(Level.INFO, MessageFormat.format("sigel2isil size = {0}, plus tab_sigel = {1}",
                    sigel2isil.size(), identifierMapper.getMap().size()));
        }
        return identifierMapper;
    }

    @SuppressWarnings("unchecked")
    private StatusCodeMapper setupStatusMapper(Map<String, Object> params) throws IOException {
        StatusCodeMapper statusMapper = new StatusCodeMapper();
        ValueMaps valueMaps = new ValueMaps();
        Map<String, Object> statuscodes = valueMaps.getMap("org/xbib/catalog/entities/mab/status.json", "status");
        statusMapper.add(statuscodes);
        return statusMapper;
    }

    private Map<String, Resource> setupSerialsMap(Map<String, Object> params) {
        // can be overriden
        return null;
    }
}
