package com.haima.sage.bigdata.azkaban.service;

import com.haima.sage.bigdata.azkaban.constants.Constant;
import com.haima.sage.bigdata.azkaban.constants.JobStatus;
import com.haima.sage.bigdata.azkaban.dao.JdbcManager;
import com.haima.sage.bigdata.azkaban.utils.DateFormatUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuyang
 */
public class BaseService {

    private static Logger logger = LoggerFactory.getLogger(BaseService.class);

    private static BaseService instance;

    public static BaseService getInstance() {
        if (instance == null) {
            synchronized (BaseService.class) {
                if (instance == null) {
                    instance = new BaseService();
                }
            }
        }
        return instance;
    }

    public String getExecuteDate(String dateStr, String hourStr, int jobType)
        throws ParseException {
        Date parseDate = DateFormatUtil.parse(dateStr, DateFormatUtil.YYYYMMDD);
        if (jobType == 1) {
            return DateFormatUtil.getDateStr(parseDate, DateFormatUtil.YYYYMMDD, Calendar.DATE, -1);
        } else {
            if (Constant.ZERO_HOUR_STR.equals(hourStr)) {
                return DateFormatUtil.getDateStr(parseDate, DateFormatUtil.YYYYMMDD,
                    Calendar.DATE, -1);
            } else {
                return DateFormatUtil.format(parseDate, DateFormatUtil.YYYYMMDD);
            }
        }
    }

    public String getExecuteHour(String dateStr, String hourStr, int jobType)
        throws ParseException {
        String dateAndHourStr = dateStr + "-" + hourStr;
        Date parseDate = DateFormatUtil.parse(dateAndHourStr, DateFormatUtil.YYYYMMDDHH);
        if (jobType == 1) {
            return DateFormatUtil.getHourStr(parseDate, 0);
        } else {
            return DateFormatUtil.getHourStr(parseDate, -1);
        }
    }

    public boolean existJobRecord(String projectName, String scriptName, String dateValue) {
        String sql = Constant.SQL_EXIST_RECORD;
        sql = sql.replace("${projectName}", projectName)
            .replace("${scriptName}", scriptName)
            .replace("${dateValue}", dateValue);
        String result = JdbcManager.executeQuery(sql);
        if (StringUtils.isNotEmpty(result)) {
            int count = Integer.parseInt(result);
            return count > 0;
        }
        return false;
    }

    public int getRecordId(String projectName, String scriptName, String dateValue) {
        String sql = Constant.SQL_GET_RUNNING_RECORD_ID;
        sql = sql.replace("${projectName}", projectName)
            .replace("${scriptName}", scriptName)
            .replace("${dateValue}", dateValue);
        String result = JdbcManager.executeQuery(sql);
        if (StringUtils.isNotEmpty(result)) {
            return Integer.parseInt(result);
        }
        return 0;
    }

    public String getRunParams(String projectName, String scriptName) {
        String sql = Constant.SQL_GET_EXEC_PARAMS;
        sql = sql.replace("${projectName}", projectName)
            .replace("${scriptName}", scriptName);
        return JdbcManager.executeQuery(sql);
    }

    public int getConfigJobType(String projectName, String scriptName) {
        String sql = Constant.SQL_GET_CONFIG_JOB_TYPE;
        sql = sql.replace("${projectName}", projectName)
            .replace("${scriptName}", scriptName);
        String result = JdbcManager.executeQuery(sql);
        return Integer.parseInt(result);
    }

    public boolean existConfig(String projectName, String scriptName) {
        String sql = Constant.SQL_GET_EXEC_CONFIG;
        sql = sql.replace("${projectName}", projectName)
            .replace("${scriptName}", scriptName);
        String result = JdbcManager.executeQuery(sql);
        if (StringUtils.isNotEmpty(result)) {
            int count = Integer.parseInt(result);
            return count > 0;
        }
        return false;
    }

    public void insertJobRecord(String projectName, String scriptName, String dateValue,
        String params, int jobType) {
        String sql = Constant.SQL_INSERT_JOB_RECORD;
        sql = sql.replace("${projectName}", projectName)
            .replace("${scriptName}", scriptName)
            .replace("${dateValue}", dateValue)
            .replace("${params}", params)
            .replace("${jobType}", jobType + "");
        JdbcManager.execute(sql);
    }

    public void updateRecordStatus(int id, JobStatus jobStatus) {
        String sql = Constant.SQL_UPDATE_RECORD_STATUS;
        sql = sql.replace("${id}", id + "")
            .replace("${jobStatus}", jobStatus.getIndex() + "");
        JdbcManager.execute(sql);
    }

    @SuppressWarnings({"Convert2Lambda", "AlibabaAvoidManuallyCreateThread"})
    public int executeShell(String projectName, String scriptName, String date, String hour,
        String params, int runningRecordId)
        throws InterruptedException, IOException {
        logger.info("开始执行Shell脚本");
        final File scriptFile = getScriptFile(projectName, scriptName, date, hour, params,
            runningRecordId);
        String absolutePath = scriptFile.getAbsolutePath();
        logger.info("绝对路径:" + absolutePath);
        Process p = Runtime.getRuntime().exec("chmod +x " + absolutePath);
        p.waitFor();
        final Process pr = Runtime.getRuntime().exec("sh -x " + absolutePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
                        logger.info(line);
                    }
                    br.close();
                } catch (IOException e) {
                    logger.error("Shell日志错误", e);
                }
            }
        }).start();
        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getErrorStream(),
            Constant.ENCODING));
        String line;
        List<String> lines = new ArrayList<>();
        while ((line = in.readLine()) != null) {
            logger.info(line);
            lines.add(line.toUpperCase());
        }
        in.close();
        pr.waitFor();
        logger.info("执行Shell Process ......");
        int exitValue = pr.exitValue();
        logger.info("退出Code", exitValue);
        int logResult = 0;
        if (lines.contains("FAILED") || lines.contains("ERROR")) {
            logResult = -1;
        }
        logger.info("执行结果:" + logResult);
        if (exitValue == 0 && logResult == 0) {
            return 0;
        } else {
            return -1;
        }
    }

    private File getScriptFile(String projectName, String scriptName, String date, String hour,
        String params, int runningRecordId) {
        String fileName = projectName + File.separator + scriptName;
        String shellPath = buildShellPath(Constant.SHELL_CMD_SCRIPT_PATH, fileName);
        logger.info("原始Shell脚本文件名:" + shellPath);
        String filename = buildScriptFileName(fileName, runningRecordId);
        logger.info("执行Shell脚本文件名:" + filename);
        String logFile = buildLogFile(projectName, scriptName, runningRecordId);
        String cmd = buildCmd(shellPath, date, hour, params, logFile);
        logger.info("最终Command:" + cmd);
        return buildScriptFile(filename, cmd, logFile);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File buildScriptFile(String filename, String cmd, String logFile) {
        List<String> lines = buildScriptFileContent();
        final File scriptFile = new File(filename);
        if (scriptFile.exists()) {
            scriptFile.delete();
        }
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(scriptFile, true);
            for (String line : lines) {
                if (line.contains("$1")) {
                    line = line.replace("$1", cmd);
                }
                if (line.contains("$2")) {
                    line = line.replace("$2", logFile);
                }
                out.write(line.getBytes(Constant.ENCODING));
                out.write(Constant.LINE_SEPARATOR_UNIX.getBytes(Constant.ENCODING));
            }
        } catch (Exception e) {
            logger.error("buildScriptFile", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("关闭流错误", e);
                }
            }
        }
        return scriptFile;
    }

    private List<String> buildScriptFileContent() {
        List<String> list = new ArrayList<>();
        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getClassLoader()
                .getResourceAsStream(Constant.SHELL_TEMPLATE_FILE);
            InputStreamReader reader = new InputStreamReader(inputStream, Constant.ENCODING);
            BufferedReader br = new BufferedReader(reader);
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                list.add(line);
            }
        } catch (Exception e) {
            logger.error("buildScriptFileContent", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭流错误", e);
                }
            }
        }
        return list;
    }

    private String buildScriptFileName(String fileName, int runningRecordId) {
        return Constant.SHELL_CMD_SCRIPT_PATH + File.separator + fileName + "-" + runningRecordId
            + ".sh";
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private String buildLogFile(String projectName, String scriptName, int runningRecordId) {
        String filePath =
            Constant.SHELL_CMD_LOGS_PATH + File.separator + projectName;
        final File logPath = new File(filePath);
        if (!logPath.exists()) {
            logPath.mkdirs();
        }
        return filePath + File.separator + scriptName + "-" + runningRecordId + ".log";
    }

    private String buildCmd(String shellPath, String date, String hour, String params,
        String logFile) {
        String execute = buildShellStr(shellPath, date, hour, params);
        StringBuilder finalCommand = new StringBuilder();
        finalCommand.append(execute).append(Constant.SHELL_CMD_SEP);
        finalCommand.append(">").append(Constant.SHELL_CMD_SEP);
        finalCommand.append(logFile);
        finalCommand.append(Constant.SHELL_CMD_SEP).append("2>&1");
        return finalCommand.toString();
    }

    private String buildShellPath(String rootPath, String fileName) {
        return rootPath + File.separator + fileName;
    }

    private String buildShellStr(String fileName, String date, String hour, String params) {
        StringBuilder cmd = new StringBuilder();
        cmd.append(Constant.SHELL_CMD_PREFIX).append(Constant.SHELL_CMD_SEP);
        cmd.append(fileName).append(Constant.SHELL_CMD_SEP);
        cmd.append(date).append(Constant.SHELL_CMD_SEP).append(hour);
        if (StringUtils.isNotEmpty(params)) {
            String[] paramArray = params.split(",");
            if (paramArray.length > 0) {
                for (String param : paramArray) {
                    cmd.append(Constant.SHELL_CMD_SEP).append(param);
                }
            }
        }
        String result = cmd.toString();
        logger.info("command:" + result);
        return result;
    }
}
