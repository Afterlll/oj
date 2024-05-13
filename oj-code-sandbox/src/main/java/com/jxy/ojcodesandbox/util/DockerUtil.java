package com.jxy.ojcodesandbox.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.LogContainerResultCallback;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * docker 操作类
 */
@Slf4j
public class DockerUtil {

    private static final DockerClient dockerClient;
    
    static {
        dockerClient = DockerClientBuilder.getInstance().build();
    }

    /**
     * 获取运行容器的状态（启动内存监控）
     * @param containerId 容器id
     * @param maxMemory 存储最大内存
     * @return
     */
    public static StatsCmd statsCmd(String containerId, long[] maxMemory) {
        StatsCmd statsCmd = dockerClient.statsCmd(containerId);
        ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {

            @Override
            public void onNext(Statistics statistics) {
                log.info("内存占用：{}", statistics.getMemoryStats().getUsage());
                maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
            }

            @Override
            public void close() throws IOException {

            }

            @Override
            public void onStart(Closeable closeable) {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
        statsCmd.exec(statisticsResultCallback);
        return statsCmd;
    }

    /**
     * 获取docker中程序的运行时间
     * @param execId 运行的容器id
     * @param timeLimit 程序运行事件限制
     * @param message docker执行的正常信息
     * @param errorMessage docker执行的错误信息
     * @return 是否超时
     * @throws InterruptedException
     */
    public static boolean execStartCmd(String execId,  long timeLimit, String[] message, String[] errorMessage) throws InterruptedException {
        final boolean[] timeout = {true};
        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
            @Override
            public void onComplete() {
                // 如果执行完成，则表示没超时
                timeout[0] = false;
                super.onComplete();
            }

            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                if (StreamType.STDERR.equals(streamType)) {
                    errorMessage[0] = new String(frame.getPayload());
                    log.error("输出错误结果：{}", errorMessage[0]);
                } else {
                    message[0] = new String(frame.getPayload());
                    log.info("输出结果：{}", message[0]);
                }
                super.onNext(frame);
            }
        };
        dockerClient.execStartCmd(execId)
                .exec(execStartResultCallback)
                .awaitCompletion(timeLimit, TimeUnit.MICROSECONDS);
        return timeout[0];
    }

    /**
     * 创建容器执行命令并获取容器执行id
     * @param containerId 容器id
     * @param cmd 执行的命令
     */
    public static ExecCreateCmdResponse execCreateCmd(String containerId, String[] cmd) {
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        log.info("创建执行命令：{}", execCreateCmdResponse);
        return execCreateCmdResponse;
    }

    /**
     * 删除镜像
     * @param image 镜像名称
     */
    public static void removeImage(String image) {
        dockerClient.removeImageCmd(image).exec();
    }

    /**
     * 删除容器
     * @param containerId 容器id
     */
    public static void removeContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
    }

    /**
     * 查看容器日志
     * @param containerId 容器id
     * @throws InterruptedException
     */
    public static void lookContainerLog(String containerId) throws InterruptedException {
        LogContainerResultCallback logContainerResultCallback = new LogContainerResultCallback() {
            @Override
            public void onNext(Frame item) {
                log.info(String.valueOf(item.getStreamType()));
                log.info("日志：" + new String(item.getPayload()));
                super.onNext(item);
            }
        };

        dockerClient.logContainerCmd(containerId)
                .withStdErr(true)
                .withStdOut(true)
                .exec(logContainerResultCallback)
                .awaitCompletion();
    }

    /**
     * 启动容器
     * @param containerId 容器id
     */
    public static void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
    }

    /**
     * 查看容器状态
     */
    public static void lookContainerStatus() {
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd.withShowAll(true).exec();
        for (Container container : containerList) {
            log.info("container====" + container);
        }
    }

    /**
     * 创建容器
     * @param image 镜像名称
     * @param hostConfig 配置信息
     * @return
     */
    public static String createContainer(String image, HostConfig hostConfig) {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true) // 禁用网络
                .withReadonlyRootfs(true) // 禁止用户向 /root 目录写入内容
                .withAttachStdin(true) // 支持标准输入
                .withAttachStderr(true) // 支持输出错误信息
                .withAttachStdout(true) // 支持标准输出
                .withTty(true) // 支持交互模式
                .exec();
        log.info(String.valueOf(createContainerResponse));
        return createContainerResponse.getId();
    }

    /**
     * 拉取镜像
     * @param image 镜像名称
     * @return
     * @throws InterruptedException
     */
    public synchronized static String pullDocker(String image) throws InterruptedException {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                log.info("下载镜像：{}", item.getStatus());
                super.onNext(item);
            }
        };
        pullImageCmd
                .exec(pullImageResultCallback)
                .awaitCompletion();
        log.info("下载完成");
        return image;
    }

}
