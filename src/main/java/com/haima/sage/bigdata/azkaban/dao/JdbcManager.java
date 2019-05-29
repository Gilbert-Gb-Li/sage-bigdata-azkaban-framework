package com.haima.sage.bigdata.azkaban.dao;

import com.haima.sage.bigdata.azkaban.constants.Constant;
import com.haima.sage.bigdata.azkaban.utils.ConfigUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuyang
 */
public class JdbcManager {

    private static Logger logger = LoggerFactory.getLogger(JdbcManager.class);

    private static String CLASS_NAME;
    private static String URL;
    private static String USERNAME;
    private static String PASSWORD;

    static {
        CLASS_NAME = Constant.DATA_SOURCE_DRIVER_CLASS;
        URL = ConfigUtil.getProperty(Constant.DATA_SOURCE_URL_KEY);
        USERNAME = ConfigUtil.getProperty(Constant.DATA_SOURCE_USERNAME_KEY);
        PASSWORD = ConfigUtil.getProperty(Constant.DATA_SOURCE_PASSWORD_KEY);
    }

    public static void execute(String sql) {
        logger.info("execute sql:" + sql);
        Connection conn = null;
        PreparedStatement pst = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            int result = pst.executeUpdate();
            logger.info("执行SQL结果:" + result);
        } catch (Exception e) {
            logger.error("SQL执行错误", e);
            throw new RuntimeException("执行SQL错误");
        } finally {
            close(null, pst, conn);
        }
    }

    public static String executeQuery(String sql) {
        logger.info("execute sql:" + sql);
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        String result = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                result = rs.getString(1);
                logger.info("查询结果:" + result);
            }
        } catch (Exception e) {
            logger.error("SQL查询错误", e);
            throw new RuntimeException("查询SQL错误");
        } finally {
            close(rs, pst, conn);
        }
        return result;
    }

    private static Connection getConnection() throws Exception {
        Class.forName(CLASS_NAME);
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    private static void close(ResultSet rs, Statement stat, Connection conn) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stat != null) {
                stat.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("DB连接关闭错误", e);
            throw new RuntimeException(e);
        }
    }
}
