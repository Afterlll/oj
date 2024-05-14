package com.jxy.oj.judge.codesandbox.impl;

import cn.hutool.core.compiler.JavaFileObjectUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.jxy.oj.common.ErrorCode;
import com.jxy.oj.exception.BusinessException;
import com.jxy.oj.judge.codesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.oj.judge.codesandbox.model.vo.ExecuteCodeRespond;
import com.jxy.oj.judge.codesandbox.service.CodeSandBox;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.http.HttpUtils;

/**
 * @author wangkeyao
 * 远程调用代码沙箱（实现 HTTP 接口调用）
 */
public class RemoteCodeSandBox implements CodeSandBox {
    // 远程代码沙箱调用地址
    private static final String REMOTE_ADDRESS = "http://localhost:8090/executeCode";
    // 定义鉴权请求头
    public static final String AUTH_REQUEST_HEADER = "auth";
    // 定义鉴权密钥
    public static final String AUTH_REQUEST_SECRET = "dwjodjpdqwojowq*Djwqjpqdoj";

    @Override
    public ExecuteCodeRespond executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("RemoteCodeSandBox");
        String responseStr = HttpUtil.createPost(REMOTE_ADDRESS)
                .header(AUTH_REQUEST_HEADER, AUTH_REQUEST_SECRET)
                .body(JSONUtil.toJsonStr(executeCodeRequest))
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "executeCode remoteSandbox error, message = " + responseStr);
        }
        return JSONUtil.toBean(responseStr, ExecuteCodeRespond.class);
    }
}
