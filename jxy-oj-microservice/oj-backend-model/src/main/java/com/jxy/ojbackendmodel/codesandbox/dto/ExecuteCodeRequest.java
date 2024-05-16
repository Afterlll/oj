package com.jxy.ojbackendmodel.codesandbox.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangkeyao
 * 执行代码沙箱需要的参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeRequest {
    /**
     * 一组输入用例
     */
    private List<String> inputList;
    /**
     * 要执行的代码
     */
    private String code;
    /**
     * 编程语言
     */
    private String language;
}
