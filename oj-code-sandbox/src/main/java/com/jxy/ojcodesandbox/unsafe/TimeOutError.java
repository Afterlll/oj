package com.jxy.ojcodesandbox.unsafe;

/**
 * @author wangkeyao
 *
 * 执行超时
 */
public class TimeOutError {
    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60 * 60 * 1000L;
        Thread.sleep(ONE_HOUR);
        System.out.println("睡眠了一小时");
    }
}
