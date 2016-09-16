package org.xbib.catalog.entities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.marc.MarcField;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class CatalogEntitySpecification extends HashMap<String, CatalogEntity> {

    private static final Logger logger = Logger.getLogger(CatalogEntitySpecification.class.getName());

    private static final long serialVersionUID = 905552856037880666L;

    private final Map<String, CatalogEntity> entities;

    private final Map<String, Object> params;

    private final String packageName;

    public CatalogEntitySpecification() throws IOException {
        this(null, new HashMap<>(), new HashMap<>(), "org.xbib.catalog.entities.marc.bib");
    }

    @SuppressWarnings("unchecked")
    public CatalogEntitySpecification(URL url, Map<String, CatalogEntity> entities, Map<String, Object> params,
                                      String packageName) throws IOException {
        this.entities = entities;
        this.params = params;
        this.packageName = packageName;
        InputStream inputStream = url != null ? url.openStream() : null;
        Map<String, Map<String, Object>> defs = new HashMap<>();
        if (inputStream != null) {
            defs = new ObjectMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true).readValue(inputStream, Map.class);
            inputStream.close();
        }

        init(defs, packageName);
    }

    public String getPackageName() {
        return packageName;
    }

    /**
     * Allows acces to {@link CatalogEntity} over class name, e.g. for facet construction.
     *
     * @return a map of class name / entity instance
     */
    public Map<String, CatalogEntity> getEntities() {
        return entities;
    }

    @SuppressWarnings("unchecked")
    private void init(Map<String, Map<String, Object>> defs, String packageName) throws IOException {
        for (Map.Entry<String, Map<String, Object>> entry : defs.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> struct = entry.getValue();
            // allow override static struct map from json with given params
            struct.putAll(params);
            CatalogEntity entity = null;
            // load class
            Class<CatalogEntity> clazz = loadClass(getClass().getClassLoader(), packageName + "." + key);
            if (clazz == null) {
                // custom class name, try without package
                clazz = loadClass(getClass().getClassLoader(), key);
            }
            if (clazz != null) {
                try {
                    entity = clazz.getDeclaredConstructor(Map.class).newInstance(struct);
                } catch (Throwable t1) {
                    try {
                        entity = clazz.newInstance();
                    } catch (Throwable t2) {
                        logger.log(Level.SEVERE, "can't instantiate class " + clazz.getName());
                    }
                }
                if (entity != null) {
                    entities.put(packageName + "." + key, entity);
                }
            }
            // connect each value to an entity class
            Collection<String> values = null;
            Object tags = struct.get("tags");
            if (tags instanceof Map) {
                values = ((Map) tags).keySet();
            } else if (tags instanceof Collection) {
                values = (Collection) tags;
            }
            if (values != null) {
                for (String value : values) {
                    associate(value, entity);
                }
            }
        }
    }

    public CatalogEntitySpecification associate(MarcField marcField, CatalogEntity entity) {
        if (containsKey(marcField.toTagKey())) {
            logger.log(Level.WARNING, "key already exist: " + marcField.toTagKey());
            return this;
        }
        put(marcField.toTagKey(), entity);
        return this;
    }

    public CatalogEntitySpecification associate(String key, CatalogEntity entity) {
        String k = clean(key);
        if (containsKey(k)) {
            logger.log(Level.WARNING, "key " + k + " already exist: " + key);
            return this;
        }
        put(k, entity);
        return this;
    }

    public CatalogEntity retrieve(MarcField marcField) {
        return retrieve(marcField.toTagKey());
    }

    public CatalogEntity retrieve(String key) {
        return super.get(clean(key));
    }

    @SuppressWarnings("unchecked")
    public void dump(Writer writer) throws IOException {
        new ObjectMapper().writeValue(writer, this);
    }

    private String clean(String key) {
        int pos = key.indexOf('$');
        return pos > 0 ? key.substring(0, pos) : key;
    }

    @SuppressWarnings("unchecked")
    private Class<CatalogEntity> loadClass(ClassLoader cl, String className) {
        Class<?> clazz = null;
        try {
            // load from custom class loader
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e1) {
                // last resort: load from system class loader
                try {
                    clazz = ClassLoader.getSystemClassLoader().loadClass(className);
                } catch (ClassNotFoundException e2) {
                    logger.log(Level.SEVERE, "class not found: " + e.getMessage(), e);
                }
            }
        }
        return (Class<CatalogEntity>) clazz;
    }

}
