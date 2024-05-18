package com.jxy.ojbackendjudgeservice.strategy;

import com.jxy.ojbackendmodel.codesandbox.JudgeInfo;
import com.jxy.ojbackendmodel.dto.question.JudgeCase;
import com.jxy.ojbackendmodel.entity.Question;
import com.jxy.ojbackendmodel.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext {

    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;
    /**
     * 输入用例
     */
    private List<String> inputList;
    /**
     * 输出用例
     */
    private List<String> outputList;
    /**
     * 判题用例
     */
    private List<JudgeCase> judgeCaseList;
    /**
     * 题目
     */
    private Question question;
    /**
     * 提交题目信息
     */
    private QuestionSubmit questionSubmit;

}
