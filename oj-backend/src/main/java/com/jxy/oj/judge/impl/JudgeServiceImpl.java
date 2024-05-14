package com.jxy.oj.judge.impl;

import cn.hutool.json.JSONUtil;
import com.jxy.oj.common.ErrorCode;
import com.jxy.oj.exception.BusinessException;
import com.jxy.oj.judge.codesandbox.factory.CodeSandBoxFactory;
import com.jxy.oj.judge.codesandbox.model.dto.ExecuteCodeRequest;
import com.jxy.oj.judge.codesandbox.model.vo.ExecuteCodeRespond;
import com.jxy.oj.judge.codesandbox.porxy.CodeSandBoxProxy;
import com.jxy.oj.judge.codesandbox.service.CodeSandBox;
import com.jxy.oj.judge.manger.JudgeManager;
import com.jxy.oj.judge.service.JudgeService;
import com.jxy.oj.judge.strategy.JudgeContext;
import com.jxy.oj.model.dto.question.JudgeCase;
import com.jxy.oj.judge.codesandbox.model.JudgeInfo;
import com.jxy.oj.model.entity.Question;
import com.jxy.oj.model.entity.QuestionSubmit;
import com.jxy.oj.model.enums.QuestionSubmitStatusEnum;
import com.jxy.oj.service.QuestionService;
import com.jxy.oj.service.QuestionSubmitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wangkeyao
 * 判题接口实现类
 */
@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type}")
    private String type;


    /**
     * 执行判题逻辑
     * @param questionSubmitId 提交的题目id
     * @return 返回题目提交信息（包含执行结果，包括status、judgeInfo）
     */
    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1. 根据提交题目的id获取对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (null == questionSubmit) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (null == question) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        Integer status = questionSubmit.getStatus();

        // todo 判题状态逻辑高并发加锁
        // 2. 如果题目提交状态不为等待中，就不用重复执行了
        if (!Objects.equals(status, QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3. 更改判题（题目提交）的状态为”判题中“，防止重复执行，也能让用户即时看到题目状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4， 调用沙箱（代码沙箱）执行代码，获取到执行结果
        CodeSandBox codeSandbox = CodeSandBoxFactory.newInstance(type);
        CodeSandBoxProxy codeSandBoxProxy = new CodeSandBoxProxy(codeSandbox);
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeRespond executeCodeResponse = codeSandBoxProxy.executeCode(executeCodeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();
        // 5， 根据沙箱的执行结果，设置题目的判题状态和信息（status、judgeInfo）
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        // 6. 修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        return questionSubmitService.getById(questionId);
    }
}
