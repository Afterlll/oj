package com.jxy.ojbackendserviceclient;

import com.jxy.ojbackendmodel.entity.QuestionSubmit;

/**
 * @author wangkeyao
 * 判题服务接口
 */
public interface JudgeService {
    /**
     * 执行判题逻辑
     * @param questionSubmitId 提交的题目id
     * @return 返回题目提交信息（包含执行结果，包括status、judgeInfo）
     */
    QuestionSubmit doJudge(long questionSubmitId);
}
