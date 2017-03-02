package org.xbib.catalog.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.xbib.content.rdf.Literal;
import org.xbib.content.rdf.Resource;
import org.xbib.content.rdf.internal.DefaultLiteral;
import org.xbib.content.resource.IRI;
import org.xbib.content.resource.Node;
import org.xbib.content.settings.Settings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public class FieldConsolidationMapper {

    private static final Logger logger = Logger.getLogger(FieldConsolidationMapper.class.getName());

    private Map<String, Object> source;

    private Map<String, Object> target;

    private List<String> targetKeys;

    @SuppressWarnings("unchecked")
    public FieldConsolidationMapper(Settings settings) throws IOException {
        String resource = settings.get("field_mapping_source");
        this.source = resource != null && resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("field_mapping_source").getAsStructuredMap();
        resource = settings.get("field_mapping_target");
        this.target =  resource != null &&  resource.endsWith(".json") ?
                new ObjectMapper().readValue(getClass().getClassLoader().getResource(resource).openStream(), Map.class) :
                settings.getAsSettings("field_mapping_target").getAsStructuredMap();
        this.targetKeys = Arrays.asList(settings.getAsArray("field_mapping_target_keys"));
        logger.log(Level.INFO, "field mapper source: " + source.size() +
                " field mapper target: " + target.size() +
                " field mapper target keys: " + targetKeys);
    }

    @SuppressWarnings("unchecked")
    public void consolidate(Resource root) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String sourceField = entry.getKey();
            Map<String, Object> sourceValues = (Map<String, Object>) entry.getValue();
            List<Node> sourceNodes = find(root, sourceField);
            if (sourceNodes != null) {
                for (Node node : sourceNodes) {
                    String value = node instanceof Literal ? ((Literal) node).object().toString() : node.toString();
                    if (sourceValues.containsKey(value)) {
                        List<String> targetValues = (List<String>) sourceValues.get(value);
                        push(result, targetKeys, targetValues);
                    }
                }
            }
        }
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            Resource resource = root;
            // split name e.g. "dc.date" --> "dc", "date"
            String name = entry.getKey();
            String[] path = name.split("\\.");
            if (path.length > 1) {
                for (int i = 0; i < path.length - 1; i++) {
                    resource = resource.newResource(IRI.builder().path(path[i]).build());
                }
            }
            name = path[path.length - 1];
            IRI predicate = IRI.builder().path(name).build();
            for (String value : entry.getValue()) {
                Literal literal = new DefaultLiteral(value).type(Literal.STRING);
                resource.add(predicate, literal);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void push(Map<String, List<String>> result, List<String> targetKeys, List<String> targetValues) {
        for (String key : targetKeys) {
            Map<String, Object> map = (Map<String, Object>) target.get(key);
            for (String value : targetValues) {
                if (map.containsKey(value)) {
                    String translation = (String) map.get(value);
                    if (translation != null) {
                        result.putIfAbsent(key, new ArrayList<>());
                        List<String> list = result.get(key);
                        list.add(translation);
                        result.put(key, list);
                    }
                }
            }
        }
    }

    private List<Node> find(Resource res, String path) {
        Resource resource = res;
        String[] p = path.split("\\.");
        String obj = path;
        if (p.length > 1) {
            for (int i = 0; i < p.length - 1; i++) {
                List<Node> list = resource.objects(p[i]);
                if (!list.isEmpty()) {
                    resource = (Resource) list.get(0);
                }
            }
            obj = p[p.length - 1];
        }
        return resource.objects(obj);
    }
}
