package com.haima.sage.bigdata.azkaban.utils;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuyang
 */
public class ConfigUtil {

    private static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);

    private static Properties prop = new Properties();

    static {
        try {
            prop.load(ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            logger.error("加载配置错误", e);
        }
    }

    private ConfigUtil() {
    }

    public static String getProperty(String key) {
        return prop.getProperty(key);
    }
}
