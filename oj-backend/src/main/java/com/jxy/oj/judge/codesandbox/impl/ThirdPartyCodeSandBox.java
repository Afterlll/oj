package com.jxy.oj.judge.codesandbox.impl;

import com.jxy.oj.judge.codesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.oj.judge.codesandbox.model.vo.ExecuteCodeRespond;
import com.jxy.oj.judge.codesandbox.service.CodeSandBox;

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
