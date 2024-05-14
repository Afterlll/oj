package com.jxy.ojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.jxy.ojcodesandbox.model.JudgeInfo;
import com.jxy.ojcodesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.ojcodesandbox.model.enums.ExecStatusEnum;
import com.jxy.ojcodesandbox.model.vo.ExecuteCodeRespond;
import com.jxy.ojcodesandbox.util.DockerUtil;
import com.jxy.ojcodesandbox.util.ExecuteMessage;
import com.jxy.ojcodesandbox.util.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wangkeyao
 *
 * docker 实现代码沙箱
 */
@Slf4j
@Component
public class JavaDockerCodeSandBox implements CodeSandBox {

    private final static String CODE_DIR_PATH = "src/main/resources/tmpCode";

    private final static String CODE_MAIN_FILE = "OjCodeSandBoxController.java";

    /**
     * 时间限制
     */
    private final static Long TIME_OUT = 5000L;

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

    private static Boolean FIRST_INIT = true;


    public static void main(String[] args) {
        JavaDockerCodeSandBox javaNativeCodeSandBox = new JavaDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        String code = ResourceUtil.readUtf8Str("example/exampleArg/Main.java");
//        String code = ResourceUtil.readUtf8Str("example/exampleAcm/OjCodeSandBoxController.java");
//        String code = ResourceUtil.readUtf8Str("example/error/RunFileError.java");
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
//        userCodeDirPath = userCodeDirPath.replace("\\", "/");
        String userCodePath = userCodeDirPath + File.separator + CODE_MAIN_FILE;
//        userCodePath = userCodePath.replace("\\", "/");
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

        // 3. 把编译好的文件上传到容器环境内
        // 拉取镜像
        String image = "openjdk:8-alpine";
        try {
            if (FIRST_INIT) {
                DockerUtil.pullDocker(image);
                FIRST_INIT = false;
            }
        } catch (InterruptedException e) {
            log.error("拉取{}镜像失败", image);
            throw new RuntimeException(e);
        }
        // 创建容器，并设置容器启动参数
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L); // 内存限制 100M
        hostConfig.withMemorySwap(0L); //
        hostConfig.withCpuCount(1L); // 使用cpu核数
//        hostConfig.withSecurityOpts(Collections.singletonList("seccomp=安全管理配置字符串"));
        // 将隔离的户目录挂载到 /app 下
        hostConfig.setBinds(new Bind(userCodeDirPath, new Volume("/app")));
        String containerId = DockerUtil.createContainer(image, hostConfig);
        // 启动容器
        DockerUtil.startContainer(containerId);

        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        try {
            for (String input : inputList) {
                StopWatch stopWatch = new StopWatch();
                String[] inputArgsArray = input.split(" ");
                String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "OjCodeSandBoxController"}, inputArgsArray);
                // docker exec 容器id java -cp /app OjCodeSandBoxController 1 3
                ExecCreateCmdResponse execCreateCmdResponse = DockerUtil.execCreateCmd(containerId, cmdArray);

                long time = 0L;
                String[] message = {null};
                String[] errorMessage = {null};
                long[] maxMemory = {0L};
                ExecuteMessage executeMessage = new ExecuteMessage();
                String execId = execCreateCmdResponse.getId();

                // 获取运行容器的状态(启动内存监控)
                StatsCmd statsCmd = DockerUtil.statsCmd(containerId, maxMemory);
                try {
                    stopWatch.start();
                    // 真正的执行程序
                    DockerUtil.execStartCmd(execId, TIME_OUT, message, errorMessage);
                    stopWatch.stop();
                    time = stopWatch.getLastTaskTimeMillis();
                    statsCmd.close();
                } catch (InterruptedException e) {
                    log.error("程序执行异常");
                    throw new RuntimeException(e);
                }
                executeMessage.setMessage(message[0]);
                executeMessage.setErrorMessage(errorMessage[0]);
                executeMessage.setTime(time);
                executeMessage.setMemory(maxMemory[0]);
                executeMessageList.add(executeMessage);

                log.info("执行结果：{}", executeMessage);
            }
        } catch (Exception e) {
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
        executeCodeResponse.setJudgeInfo(judgeInfo);
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
