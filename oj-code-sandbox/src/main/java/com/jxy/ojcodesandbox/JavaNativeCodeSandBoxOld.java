package com.jxy.ojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.jxy.ojcodesandbox.model.JudgeInfo;
import com.jxy.ojcodesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.ojcodesandbox.model.enums.ExecStatusEnum;
import com.jxy.ojcodesandbox.model.vo.ExecuteCodeRespond;
import com.jxy.ojcodesandbox.util.ExecuteMessage;
import com.jxy.ojcodesandbox.util.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author wangkeyao
 *
 * java原生实现代码沙箱
 */
@Slf4j
public class JavaNativeCodeSandBoxOld implements CodeSandBox {

    private final static String CODE_DIR_PATH = "src\\main\\resources\\tmpCode";

    private final static String CODE_MAIN_FILE = "Main.java";

    /**
     * 时间限制
     */
    private final static Long TIME_OUT = 3000L;

    /**
     * 黑名单
     */
    private final static List<String> BLACK_LIST = Arrays.asList("Files", "exec");

    /**
     * 字典树
     */
    private static WordTree WORD_TREE;

    {
        // 初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(BLACK_LIST);
    }

    /**
     * 安全管理器路径
     */
    private static final String SECURITY_MANAGER_PATH = "D:\\code\\project\\oj\\oj-code-sandbox\\src\\main\\resources\\security";

    /**
     * 安全管理器主类
     */
    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";


    public static void main(String[] args) {
        JavaNativeCodeSandBoxOld javaNativeCodeSandBox = new JavaNativeCodeSandBoxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
//        String code = ResourceUtil.readUtf8Str("example/exampleArg/Main.java");
//        String code = ResourceUtil.readUtf8Str("example/exampleAcm/Main.java");
        String code = ResourceUtil.readUtf8Str("example/error/RunFileError.java");
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeRespond executeCodeRespond = javaNativeCodeSandBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeRespond);
    }

    @Override
    public ExecuteCodeRespond executeCode(ExecuteCodeRequest executeCodeRequest) {
//        System.setSecurityManager(new DefaultSecurityManager());

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        // 检验代码种是否含有黑名单中的禁用词
//        if (null != WORD_TREE.matchWord(code)) {
//            log.error("包含禁用词，禁止访问！");
//            return null;
//        }

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
                // 1. 限制资源分配（最大堆内存256m）,这种方式只能进行程序方面的限制，系统方面的限制使用cGroup（系统）
                // 2. 使用安全管理器
                String runCmd = String.format("java -Xms256m -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath, SECURITY_MANAGER_PATH, SECURITY_MANAGER_CLASS_NAME, input);
                Process runProcess = Runtime.getRuntime().exec(runCmd);

                // 开启后台线程监控超时
//                new Thread(() -> {
//                    try {
//                        Thread.sleep(TIME_OUT);
//                        log.error("用户程序运行超时，强制中断！");
//                        runProcess.destroy();
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }).start();

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
