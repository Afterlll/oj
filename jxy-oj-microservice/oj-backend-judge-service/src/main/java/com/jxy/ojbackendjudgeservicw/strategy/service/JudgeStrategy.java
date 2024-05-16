package com.jxy.ojbackendjudgeservicw.strategy.service;


import com.jxy.ojbackendjudgeservicw.strategy.JudgeContext;
import com.jxy.ojbackendmodel.codesandbox.JudgeInfo;

/**
 * 判题策略
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
