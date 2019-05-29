package com.haima.sage.bigdata.azkaban.constants;

import com.haima.sage.bigdata.azkaban.utils.ConfigUtil;

/**
 * @author liuyang
 */
public class Constant {

    public static final String DATA_SOURCE_DRIVER_CLASS = "com.mysql.jdbc.Driver";
    public static final String DATA_SOURCE_URL_KEY = "db.mysql.url";
    public static final String DATA_SOURCE_USERNAME_KEY = "db.mysql.username";
    public static final String DATA_SOURCE_PASSWORD_KEY = "db.mysql.password";
    public static final String LOGGER_NAME_TIME = "timerCalculator_Logger";
    public static final String SHELL_TEMPLATE_FILE = "execute-template.sh";
    public static final String LINE_SEPARATOR_UNIX = "\n";
    public static final String ENCODING = "UTF-8";

    public static final String SHELL_CMD_PREFIX = "sh -x";
    public static final String SHELL_CMD_SCRIPT_PATH = ConfigUtil
        .getProperty(Constant.SHELL_SCRIPT_PATH);
    public static final String SHELL_CMD_LOGS_PATH = ConfigUtil
        .getProperty(Constant.SHELL_LOGS_PATH);
    public static final String SHELL_CMD_SEP = " ";

    public static final String SQL_EXIST_RECORD =
        "SELECT COUNT(1) FROM tbl_azkaban_exec_record WHERE project_name='${projectName}' AND script_name='${scriptName}' AND date_value='${dateValue}' AND job_type=1 AND job_status IN ("
            + JobStatus.RUNNING.getIndex() + "," + JobStatus.FINISH.getIndex() + ")";
    public static final String SQL_GET_RUNNING_RECORD_ID =
        "SELECT id FROM tbl_azkaban_exec_record WHERE project_name='${projectName}' AND script_name='${scriptName}' AND date_value='${dateValue}' AND job_status="
            + JobStatus.RUNNING.getIndex();
    public static final String SQL_INSERT_JOB_RECORD = "INSERT INTO tbl_azkaban_exec_record (project_name,script_name,date_value,params,job_type) VALUE ('${projectName}','${scriptName}','${dateValue}','${params}', ${jobType})";
    public static final String SQL_UPDATE_RECORD_STATUS = "UPDATE tbl_azkaban_exec_record SET job_status=${jobStatus} WHERE id=${id}";
    public static final String SQL_GET_EXEC_PARAMS = "SELECT params FROM tbl_azkaban_exec_config WHERE project_name='${projectName}' AND script_name='${scriptName}'";
    public static final String SQL_GET_CONFIG_JOB_TYPE = "SELECT job_type FROM tbl_azkaban_exec_config WHERE project_name='${projectName}' AND script_name='${scriptName}'";
    public static final String SQL_GET_EXEC_CONFIG = "SELECT id FROM tbl_azkaban_exec_config WHERE project_name='${projectName}' AND script_name='${scriptName}'";

    public static final String ZERO_HOUR_STR = "00";

    private static final String SHELL_SCRIPT_PATH = "shell.script.path";
    private static final String SHELL_LOGS_PATH = "shell.logs.path";

    private Constant() {
    }
}
