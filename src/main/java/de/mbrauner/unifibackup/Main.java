package de.mbrauner.unifibackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.jcraft.jsch.JSchException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        File cfg = new File("./config.properties");
        if (!cfg.exists()) {
            LOGGER.error("config file cannot be loaded");
            System.exit(-1);
        } else if (!cfg.canRead()) {
            LOGGER.error("config file cannot be read");
            System.exit(-2);
        } else {
            try (FileInputStream fis = new FileInputStream(cfg)) {
                LOGGER.debug("read config file {}", cfg.getCanonicalPath());
                UnifiProperties p = new UnifiProperties();
                p.load(fis);
                if (!propertiesValid(p)) {
                    LOGGER.error("config file seems not to be valid");
                    System.exit(-3);
                } else {
                    new SSH(p).perform();
                }
            } catch (IOException | JSchException e) {
                LOGGER.error(e.getMessage(), e);
                System.exit(-4);
            }
        }
    }

    public static boolean propertiesValid(UnifiProperties p) {
        return checkProperty(p, UnifiProperties.USER) && checkProperty(p, UnifiProperties.PW) && checkProperty(p, UnifiProperties.HOST);
    }

    protected static boolean checkProperty(UnifiProperties p, String key) {
        if (p.getProperty(key, "").trim().isEmpty()) {
            LOGGER.warn("\"{}\" seems to be empty or not available", key);
            return false;
        } else {
            return true;
        }
    }
}
