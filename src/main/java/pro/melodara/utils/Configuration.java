package pro.melodara.utils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class Configuration {
    public static final Properties props = new Properties();
    public final Logger LOGGER = LoggerFactory.getLogger("melodara/config");

    public void load(@NotNull String filename) throws IOException {
        LOGGER.info("Loading configuration from '{}' ...", filename);
        props.load(Configuration.class.getClassLoader().getResourceAsStream(filename));
        LOGGER.info("Successfully loaded configuration from '{}' ({} properties)", filename, props.keySet().size());
    }

    public static String get(@NotNull String key) {
        String value = props.getProperty(key);

        assert value != null : "No configuration value found for key: " + key;

        return value;
    }
}
