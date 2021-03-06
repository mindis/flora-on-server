package pt.floraon.occurrences.fields.parsers;

import pt.floraon.driver.parsers.FieldParser;
import pt.floraon.driver.FloraOnException;

import java.util.Map;

/**
 * Created by miguel on 09-02-2017.
 */
public class AliasFieldParser implements FieldParser {
    private String baseName;
    private Map<String, FieldParser> mappings;

    public AliasFieldParser(String baseName, Map<String, FieldParser> mappings) {
        this.baseName = baseName;
        this.mappings = mappings;
    }

    @Override
    public void parseValue(String inputValue, String inputFieldName, Object bean) throws IllegalArgumentException, FloraOnException {
        this.mappings.get(this.baseName).parseValue(inputValue, this.baseName, bean);
    }
}
