package com.jxy.ojbackendjudgeservicw.codesandbox.impl;


import com.jxy.ojbackendjudgeservicw.codesandbox.service.CodeSandBox;
import com.jxy.ojbackendmodel.codesandbox.dto.ExecuteCodeRequest;
import com.jxy.ojbackendmodel.codesandbox.vo.ExecuteCodeRespond;

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
