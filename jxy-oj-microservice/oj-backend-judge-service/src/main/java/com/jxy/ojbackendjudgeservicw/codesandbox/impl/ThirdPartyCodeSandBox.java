package com.jxy.ojbackendjudgeservicw.codesandbox.impl;


import com.jxy.ojbackendjudgeservicw.codesandbox.service.CodeSandBox;
import com.jxy.ojbackendmodel.codesandbox.dto.ExecuteCodeRequest;
import com.jxy.ojbackendmodel.codesandbox.vo.ExecuteCodeRespond;

/**
 * @author wangkeyao
 * 调用第三方实现好的代码沙箱（<a href="https://github.com/criyle/go-judge">第三方代码沙箱实现示例</a>）
 */
public class ThirdPartyCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeRespond executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("ThirdPartyCodeSandBox");
        return null;
    }
}
