package com.jxy.ojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import com.jxy.ojcodesandbox.util.DockerUtil;
import com.jxy.ojcodesandbox.util.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * java docker代码沙箱实现
 */
@Component
@Slf4j
public class JavaDockerCodeSandBox extends JavaCodeSandBoxTemplate {

    /**
     * 只拉取一次镜像
     */
    private static Boolean FIRST_INIT = true;

    /**
     * 拉取的java镜像
     */
    private static final String image = "openjdk:8-alpine";

    private static final long TIME_OUT = 5000L;

    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        // 拉取镜像
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
        hostConfig.setBinds(new Bind(userCodeFile.getParent(), new Volume("/app")));
        String containerId = DockerUtil.createContainer(image, hostConfig);
        // 启动容器
        DockerUtil.startContainer(containerId);

        // 3. 执行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        try {
            for (String input : inputList) {
                StopWatch stopWatch = new StopWatch();
                String[] inputArgsArray = input.split(" ");
                String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
                // docker exec 容器id java -cp /app Main 1 3
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
            log.error("代码执行错误，", e);
            throw new RuntimeException(e);
        }

        return executeMessageList;
    }
}
