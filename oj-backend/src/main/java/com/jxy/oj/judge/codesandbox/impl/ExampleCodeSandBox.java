package com.jxy.oj.judge.codesandbox.impl;

import com.jxy.oj.judge.codesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.oj.judge.codesandbox.model.vo.ExecuteCodeRespond;
import com.jxy.oj.judge.codesandbox.service.CodeSandBox;

/**
 * @author wangkeyao
 * 示例代码沙箱（仅用来跑通业务流程）
 */
public class ExampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeRespond executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("ExampleCodeSandBox");
        return null;
    }
}
