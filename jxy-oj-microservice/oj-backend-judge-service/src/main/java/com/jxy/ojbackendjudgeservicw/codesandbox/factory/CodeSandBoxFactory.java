package com.jxy.ojbackendjudgeservicw.codesandbox.factory;


import com.jxy.ojbackendjudgeservicw.codesandbox.impl.ExampleCodeSandBox;
import com.jxy.ojbackendjudgeservicw.codesandbox.impl.RemoteCodeSandBox;
import com.jxy.ojbackendjudgeservicw.codesandbox.impl.ThirdPartyCodeSandBox;
import com.jxy.ojbackendjudgeservicw.codesandbox.service.CodeSandBox;

/**
 * @author wangkeyao
 * 代码沙箱静态工厂类
 */
public class CodeSandBoxFactory {
    // todo 考虑使用单例模式是否存在线程安全问题（此时可以使用单例工厂模式）
    public static CodeSandBox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandBox();
            case "remote":
                return new RemoteCodeSandBox();
            case "thirdParty":
                return new ThirdPartyCodeSandBox();
            default:
                return new ExampleCodeSandBox();
        }
    }
}
