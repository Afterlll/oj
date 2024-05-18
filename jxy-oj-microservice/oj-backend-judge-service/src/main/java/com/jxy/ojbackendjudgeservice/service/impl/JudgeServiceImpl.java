package com.jxy.ojbackendjudgeservice.service.impl;

import cn.hutool.json.JSONUtil;
import com.jxy.ojbackendcommon.common.ErrorCode;
import com.jxy.ojbackendcommon.exception.BusinessException;
import com.jxy.ojbackendjudgeservice.codesandbox.factory.CodeSandBoxFactory;
import com.jxy.ojbackendjudgeservice.codesandbox.porxy.CodeSandBoxProxy;
import com.jxy.ojbackendjudgeservice.codesandbox.service.CodeSandBox;
import com.jxy.ojbackendjudgeservice.manger.JudgeManager;
import com.jxy.ojbackendjudgeservice.service.JudgeService;
import com.jxy.ojbackendjudgeservice.strategy.JudgeContext;
import com.jxy.ojbackendmodel.codesandbox.JudgeInfo;
import com.jxy.ojbackendmodel.codesandbox.dto.ExecuteCodeRequest;
import com.jxy.ojbackendmodel.codesandbox.vo.ExecuteCodeRespond;
import com.jxy.ojbackendmodel.dto.question.JudgeCase;
import com.jxy.ojbackendmodel.entity.Question;
import com.jxy.ojbackendmodel.entity.QuestionSubmit;
import com.jxy.ojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.jxy.ojbackendserviceclient.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private QuestionFeignClient questionFeignClient;

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
    @Transactional
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1. 根据提交题目的id获取对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (null == questionSubmit) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
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
        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
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
        update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        return questionFeignClient.getQuestionSubmitById(questionId);
    }
}
