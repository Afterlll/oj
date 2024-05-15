package com.jxy.ojcodesandbox.controller;

import com.jxy.ojcodesandbox.JavaNativeCodeSandBox;
import com.jxy.ojcodesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.ojcodesandbox.model.vo.ExecuteCodeRespond;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wangkeyao
 */
@RestController
@RequestMapping("/")
public class OjCodeSandBoxController {

    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;
    
    // 定义鉴权请求头
    public static final String AUTH_REQUEST_HEADER = "auth";
    // 定义鉴权密钥
    public static final String AUTH_REQUEST_SECRET = "dwjodjpdqwojowq*Djwqjpqdoj";

    @RequestMapping("health")
    public String health() {
        return "ok";
    }

    @PostMapping("executeCode")
    public ExecuteCodeRespond executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request, HttpServletResponse response) {
        // api认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_SECRET.equals(authHeader)) {
            response.setStatus(403); // 无权限
            return null;
        }
        if (null == executeCodeRequest) {
            throw new RuntimeException("请求参数为空！");
        }
        return javaNativeCodeSandBox.executeCode(executeCodeRequest);
    }

}
