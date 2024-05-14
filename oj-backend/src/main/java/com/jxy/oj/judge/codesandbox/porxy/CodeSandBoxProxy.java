package com.jxy.oj.judge.codesandbox.porxy;

import com.jxy.oj.judge.codesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.oj.judge.codesandbox.model.vo.ExecuteCodeRespond;
import com.jxy.oj.judge.codesandbox.service.CodeSandBox;
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
