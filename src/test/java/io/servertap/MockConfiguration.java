package io.servertap;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MockConfiguration {

    public static YamlConfiguration authConfig() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(new File(YamlConfiguration.class.getClassLoader().getResource("auth-config.yml").getFile()));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return config;
    }

}
