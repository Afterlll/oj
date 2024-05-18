package com.jxy.ojbackendjudgeservice.codesandbox.service;


import com.jxy.ojbackendmodel.codesandbox.dto.ExecuteCodeRequest;
import com.jxy.ojbackendmodel.codesandbox.vo.ExecuteCodeRespond;

/**
 * @author wangkeyao
 * 代码沙箱接口
 */
public interface CodeSandBox {
    /**
     * 代码沙箱执行代码
     * @param executeCodeRequest 代码沙箱执行代码需要的参数
     * @return 代码沙箱执行完代码之后响应信息
     */
    ExecuteCodeRespond executeCode(ExecuteCodeRequest executeCodeRequest);
}
