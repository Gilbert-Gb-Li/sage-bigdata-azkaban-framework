package com.haima.sage.bigdata.azkaban;

import com.haima.sage.bigdata.azkaban.constants.JobStatus;
import com.haima.sage.bigdata.azkaban.service.BaseService;
import com.haima.sage.bigdata.azkaban.utils.DateFormatUtil;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author liuyang
 */
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        boolean iSuccess = true;
        String projectName = "dummyProjectName";
        String scriptName = "dummyScriptName";
        String date;
        String hour;
        logger.info("脚本启动...");
        try {
            if (args == null) {
                logger.error("参数为空");
                throw new RuntimeException("参数为空");
            }
            logger.info("参数:" + Arrays.asList(args));
            if (args.length < 4) {
                logger.error("参数个数错误,个数:" + args.length);
                throw new RuntimeException("参数个数错误");
            }
            projectName = args[0];
            scriptName = args[1];
            String inputDate = args[2];
            String inputHour = args[3];
            BaseService baseService = BaseService.getInstance();
            int jobType = baseService.getConfigJobType(projectName, scriptName);
            if (jobType <= 0) {
                throw new RuntimeException("Job Type错误");
            }
            date = baseService.getExecuteDate(inputDate, inputHour, jobType);
            if (!DateFormatUtil.checkDate(date)) {
                throw new RuntimeException("日期格式错误");
            }
            logger.info("date:" + date);
            hour = baseService.getExecuteHour(inputDate, inputHour, jobType);
            logger.info("hour:" + hour);
            boolean existJobConfig = baseService.existConfig(projectName, scriptName);
            if (!existJobConfig) {
                throw new RuntimeException("Job配置不存在");
            }
            boolean existJobRecord = baseService.existJobRecord(projectName, scriptName, date);
            if (!existJobRecord) {
                String params = baseService.getRunParams(projectName, scriptName);
                logger.info("执行参数:" + params);
                baseService.insertJobRecord(projectName, scriptName, date, params, jobType);
                int runningRecordId = baseService.getRecordId(projectName, scriptName, date);
                logger.info("Running Record ID:" + runningRecordId);
                if (runningRecordId <= 0) {
                    throw new RuntimeException("未获取到Running状态的Record ID");
                }
                int result = baseService.executeShell(projectName, scriptName, date, hour, params,
                    runningRecordId);
                logger.info("SHELL执行结果:" + result);
                if (result == 0) {
                    baseService.updateRecordStatus(runningRecordId, JobStatus.FINISH);
                } else {
                    iSuccess = false;
                    baseService.updateRecordStatus(runningRecordId, JobStatus.FAILED);
                }
            } else {
                logger.info("正在执行或者已经执行完成");
            }
        } catch (Exception e) {
            logger.error("Application Error", e);
            iSuccess = false;
        } finally {
            logger.info("执行结果:" + iSuccess);
            logger.info("脚本结束...");
            logger.info(projectName + ":" + scriptName + " - " +
                (System.currentTimeMillis() - start) + " - " + iSuccess);
        }
        if (iSuccess) {
            System.exit(0);
        } else {
            System.exit(-1);
        }
    }
}
