package com.jxy.ojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.jxy.ojcodesandbox.model.JudgeInfo;
import com.jxy.ojcodesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.ojcodesandbox.model.enums.ExecStatusEnum;
import com.jxy.ojcodesandbox.model.vo.ExecuteCodeRespond;
import com.jxy.ojcodesandbox.util.ExecuteMessage;
import com.jxy.ojcodesandbox.util.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;

/**
 * @author wangkeyao
 *
 * java原生实现代码沙箱
 */
@Slf4j
public class JavaNativeCodeSandBox implements CodeSandBox {

    private final static String CODE_DIR_PATH = "src\\main\\resources\\tmpCode";

    private final static String CODE_MAIN_FILE = "Main.java";

    public static void main(String[] args) {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readUtf8Str("example/exampleArg/Main.java");
//        String code = ResourceUtil.readUtf8Str("example/exampleAcm/Main.java");
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeRespond executeCodeRespond = javaNativeCodeSandBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeRespond);
    }

    @Override
    public ExecuteCodeRespond executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        // 1. 把用户的代码保存到文件中
        String projectDir = System.getProperty("user.dir");
        String codeDirPath = projectDir + File.separator + CODE_DIR_PATH;
        if (!FileUtil.exist(codeDirPath)) {
            FileUtil.mkdir(codeDirPath);
        }
        // 用户代码隔离
        String userCodeDirPath = codeDirPath + File.separator + UUID.randomUUID();
        String userCodePath = userCodeDirPath + File.separator + CODE_MAIN_FILE;
        File userCodeFile = FileUtil.writeUtf8String(code, userCodePath);

        // 2， 编译代码，得到 class 文件
        try {
            String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            log.info("编译信息为：{}", executeMessage);
            // todo 编译失败之后还是否需要运行代码
        } catch (Exception e) {
            return getErrorRespond(e);
        }

        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        try {
            String userCodeParentPath = userCodeFile.getParent();
            // -Dfile.encoding=UTF-8 参数解决java代码运行时出现的乱码问题
            for (String input : inputList) {
                // arg参数方式
                String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, input);
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                log.info("执行结果：{}", executeMessage);
            }
        } catch (IOException e) {
            return getErrorRespond(e);
        }

        // 4. 收集整理错误信息
        ExecuteCodeRespond executeCodeResponse = new ExecuteCodeRespond();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0L;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                // 用户提交的代码执行中存在错误
                executeCodeResponse.setStatus(ExecStatusEnum.USER_CODE_ERROR.getValue());
                break;
            }
            outputList.add(executeMessage.getMessage());
            // 统计最大时间 todo 每一个测试用例都统计一个时间
            Long time = executeMessage.getTime();
            if (null != time) {
                maxTime = Math.max(maxTime, time);
            }
        }
        // 正常运行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(ExecStatusEnum.BOX_RUN_SUCCESS.getValue());
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setTime(maxTime);
        // todo 获取内存信息，要借助第三方库来获取内存占用，非常麻烦，此处不做实现

        // 5. 文件清理，释放空间
        String userCodeParentPath = userCodeFile.getParent();
        if (null != userCodeParentPath) {
            boolean del = FileUtil.del(userCodeParentPath);
            log.error("删除" + (del ? "成功" : "失败"));
        }

        return executeCodeResponse;
    }

    /**
     * 封装异常已处理类， 用于报错时返回默认结果,错误处理，提高程序健壮性
     *
     * @param e 抛出的异常
     * @return
     */
    private ExecuteCodeRespond getErrorRespond(Throwable e) {
        ExecuteCodeRespond executeCodeRespond = new ExecuteCodeRespond();
        executeCodeRespond.setOutputList(new ArrayList<>());
        executeCodeRespond.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executeCodeRespond.setStatus(ExecStatusEnum.BOX_RUN_ERROR.getValue());
        executeCodeRespond.setJudgeInfo(new JudgeInfo());
        return executeCodeRespond;
    }

}
