package com.jxy.ojbackendjudgeservicw.manger;

import com.jxy.ojbackendjudgeservicw.strategy.JudgeContext;
import com.jxy.ojbackendjudgeservicw.strategy.impl.DefaultJudgeStrategy;
import com.jxy.ojbackendjudgeservicw.strategy.impl.JavaLanguageJudgeStrategy;
import com.jxy.ojbackendjudgeservicw.strategy.service.JudgeStrategy;
import com.jxy.ojbackendmodel.codesandbox.JudgeInfo;
import com.jxy.ojbackendmodel.entity.QuestionSubmit;
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
