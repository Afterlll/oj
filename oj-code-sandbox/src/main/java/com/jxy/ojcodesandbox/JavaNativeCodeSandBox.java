package com.jxy.ojcodesandbox;

import com.jxy.ojcodesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.ojcodesandbox.model.vo.ExecuteCodeRespond;
import org.springframework.stereotype.Component;

/**
 * java 原生代码沙箱实现
 */
@Component
public class JavaNativeCodeSandBox extends JavaCodeSandBoxTemplate {
    @Override
    public ExecuteCodeRespond executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
