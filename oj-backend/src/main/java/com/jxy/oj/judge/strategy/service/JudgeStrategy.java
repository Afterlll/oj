package com.jxy.oj.judge.strategy.service;

import com.jxy.oj.judge.strategy.JudgeContext;
import com.jxy.oj.judge.codesandbox.model.JudgeInfo;

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
