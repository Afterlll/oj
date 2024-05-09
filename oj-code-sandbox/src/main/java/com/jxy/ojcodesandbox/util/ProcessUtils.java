package com.jxy.ojcodesandbox.util;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @author wangkeyao
 * <p>
 * 进程工具类
 */
@Slf4j
public class ProcessUtils {

    /**
     * 执行进程并获取信息(适合arg传参方式)
     *
     * @param runProcess 进程对象
     * @param opName 执行的操作
     * @return 程序执行状态信息
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        BufferedReader bufferedReader = null;
        BufferedReader bufferedErrorReader = null;
        try {
            // todo 需要考虑：往控制台中输出信息是否需要算进运行时间去
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行完成，获取执行状态码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            if (exitValue == 0) {
                // 正常退出
                bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                String line;
                StringBuilder stringOutputBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringOutputBuilder.append(line);
                }
                executeMessage.setMessage(stringOutputBuilder.toString());
                log.info("{}成功：{}", opName, stringOutputBuilder);
            } else {
                // 异常退出
                bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                String line;
                StringBuilder stringOutputBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringOutputBuilder.append(line);
                }

                InputStream errorStream = runProcess.getErrorStream();
                bufferedErrorReader = new BufferedReader(new InputStreamReader(errorStream));
                String errorLine;
                while ((errorLine = bufferedErrorReader.readLine()) != null) {
                    stringOutputBuilder.append(errorLine);
                }
                executeMessage.setErrorMessage(stringOutputBuilder.toString());
                log.error("{}失败,状态码为：{}，异常信息为：{}，", opName, exitValue, stringOutputBuilder);
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
            log.error("执行进程出现异常：", e);
            throw new RuntimeException(e);
        } finally {
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (null != bufferedErrorReader) {
                try {
                    bufferedErrorReader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return executeMessage;
    }

    /**
     * 交互执行进程并获取信息(适合acm模式的交互传参方式)
     *
     * @param runProcess 进程对象
     * @param args 参数
     * @param opName 操作名称
     * @return 程序执行状态信息
     */
    public static ExecuteMessage runInteractiveProcessAndGetMessage(Process runProcess, String opName, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream()))) {
            // 获取控制台的输入信息
            // todo 规则需设定好
            String[] s = args.split(" ");
            String join = StrUtil.join("\n", s) + "\n";
            bufferedWriter.write(join);
            // flush 就相当于按了回车
            bufferedWriter.flush();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            String line;
            StringBuilder stringOutputBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringOutputBuilder.append(line);
            }
            executeMessage.setMessage(stringOutputBuilder.toString());
            log.info("{}，交互执行信息：{}", opName, stringOutputBuilder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return executeMessage;
    }

}
