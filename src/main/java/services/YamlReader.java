package services;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class YamlReader {
    public static Map<String, String> getConfigs(String prefix) {
        Map<String, Object> data = getDataFromConfigFile();
        return (Map<String, String>) data.get(prefix);
    }

    public static List<Map<String, String>> getSiteList() {
        Map<String, Object> data = getDataFromConfigFile();
        return (List<Map<String, String>>) data.get("sites");

    }

    private static Map<String, Object> getDataFromConfigFile() {
        try {
            InputStream inputStream = new FileInputStream("application.yaml");

            Yaml yaml = new Yaml();
            return yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
