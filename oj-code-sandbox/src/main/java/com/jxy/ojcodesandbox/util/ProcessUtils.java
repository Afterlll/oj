package com.jxy.ojcodesandbox.util;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // 等待程序执行，获取错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            // 正常退出
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));
            } else {
                // 异常退出
                System.out.println(opName + "失败，错误码： " + exitValue);
                // 分批获取进程的正常输出
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));

                // 分批获取进程的错误输出
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                // 逐行读取
                List<String> errorOutputStrList = new ArrayList<>();
                // 逐行读取
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStrList.add(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorOutputStrList, "\n"));
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
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
