package com.jxy.ojcodesandbox.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * ACM 交互模式的示例代码
 */
public class ExecuteMainThroughProcess {
    public static void main(String[] args) throws IOException, InterruptedException {
        String userCodeParentPath = "D:\\code\\project\\oj\\oj-code-sandbox\\src\\main\\resources\\exampleAcm"; // 替换为实际的类路径
        String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s OjCodeSandBoxController", userCodeParentPath);

        // 创建ProcessBuilder来执行Java命令
        ProcessBuilder pb = new ProcessBuilder(runCmd.split(" "));
        pb.redirectErrorStream(true); // 合并错误和标准输出

        // 启动进程
        Process process = pb.start();

        // 使用Scanner从控制台读取输入
        Scanner consoleScanner = new Scanner(System.in);

        // 获取进程的输出流以便读取结果
        BufferedReader resultReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // 获取进程的输入流以便写入数据（即模拟控制台输入）
        PrintWriter writer = new PrintWriter(process.getOutputStream());

        System.out.println("请输入第一个数字：");
        int num1 = consoleScanner.nextInt();
        writer.println(num1);

        System.out.println("请输入第二个数字：");
        int num2 = consoleScanner.nextInt();
        writer.println(num2);

        // 确保所有数据被写入，然后关闭写入流
        writer.flush();
        writer.close();

        // 读取并打印进程输出的结果
        String resultLine;
        while ((resultLine = resultReader.readLine()) != null) {
            System.out.println(resultLine);
        }

        // 等待进程结束
        process.waitFor();

        consoleScanner.close(); // 关闭控制台扫描器
        resultReader.close(); // 关闭结果读取流
    }
}
