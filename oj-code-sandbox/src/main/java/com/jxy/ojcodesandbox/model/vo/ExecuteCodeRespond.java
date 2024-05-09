package com.jxy.ojcodesandbox.model.vo;

import com.jxy.ojcodesandbox.model.JudgeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wangkeyao
 * 代码沙箱执行完毕之后返回的数据
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCodeRespond {
    /**
     * 代码沙箱返回一组输用例
     */
    private List<String> outputList;
    /**
     * 接口信息
     */
    private String message;
    /**
     * 接口状态
     */
    private Integer status;
    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;
}
