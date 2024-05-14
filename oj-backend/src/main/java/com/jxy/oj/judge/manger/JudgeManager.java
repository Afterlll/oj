package com.jxy.oj.judge.manger;

import com.jxy.oj.judge.strategy.impl.DefaultJudgeStrategy;
import com.jxy.oj.judge.strategy.impl.JavaLanguageJudgeStrategy;
import com.jxy.oj.judge.strategy.JudgeContext;
import com.jxy.oj.judge.strategy.service.JudgeStrategy;
import com.jxy.oj.judge.codesandbox.model.JudgeInfo;
import com.jxy.oj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
