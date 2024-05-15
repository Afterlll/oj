package com.jxy.ojcodesandbox.util;

import lombok.Data;

/**
 * @author wangkeyao
 *
 * 程序执行状态信息类
 */
@Data
public class ExecuteMessage {
    /**
     * 执行程序的退出码
     */
    private Integer exitValue;
    /**
     * 正常执行信息
     */
    private String message;
    /**
     * 错误执行信息
     */
    private String errorMessage;
    /**
     * 执行时间
     */
    private Long time;
    /**
     * 程序执行消耗的内存
     */
    private Long memory;
}
