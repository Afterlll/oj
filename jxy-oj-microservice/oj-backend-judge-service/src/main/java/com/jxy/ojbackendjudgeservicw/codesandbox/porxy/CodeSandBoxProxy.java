package com.jxy.ojbackendjudgeservicw.codesandbox.porxy;

import com.jxy.ojbackendjudgeservicw.codesandbox.service.CodeSandBox;
import com.jxy.ojbackendmodel.codesandbox.dto.ExecuteCodeRequest;
import com.jxy.ojbackendmodel.codesandbox.vo.ExecuteCodeRespond;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wangkeyao
 * 代码沙箱代理类（增强功能）
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class CodeSandBoxProxy implements CodeSandBox {
    private CodeSandBox codeSandBox;
    @Override
    public ExecuteCodeRespond executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息：" + executeCodeRequest.toString());
        ExecuteCodeRespond executeCodeRespond = codeSandBox.executeCode(executeCodeRequest);
        log.info("代码沙箱响应信息：" + executeCodeRespond.toString());
        return executeCodeRespond;
    }
}
