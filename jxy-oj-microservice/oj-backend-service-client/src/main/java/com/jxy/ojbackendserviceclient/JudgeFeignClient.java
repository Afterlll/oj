package com.jxy.ojbackendserviceclient;

import com.jxy.ojbackendmodel.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author wangkeyao
 * 判题服务远程调用接口
 */
@FeignClient(value = "oj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {
    /**
     * 执行判题逻辑
     * @param questionSubmitId 提交的题目id
     * @return 返回题目提交信息（包含执行结果，包括status、judgeInfo）
     */
    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}
