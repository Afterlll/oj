package com.jxy.ojbackendjudgeservice.controller.inner;

import com.jxy.ojbackendjudgeservice.service.JudgeService;
import com.jxy.ojbackendmodel.entity.QuestionSubmit;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wangkeyao
 */
@RestController
@RequestMapping("/inner")
public class JudgeController {
    @Resource
    private JudgeService judgeService;
    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }

}
