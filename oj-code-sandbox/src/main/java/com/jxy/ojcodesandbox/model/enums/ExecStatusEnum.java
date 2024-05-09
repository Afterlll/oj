package com.jxy.ojcodesandbox.model.enums;

/**
 * @author wangkeyao
 *
 * 代码沙箱执行状态枚举类
 */
public enum ExecStatusEnum {
    BOX_RUN_SUCCESS(1, "代码沙箱正常运行"),
    BOX_RUN_ERROR(2, "代码沙箱运行错误"),
    USER_CODE_ERROR(3, "用户提交的代码执行中存在错误");

    private final Integer value;

    private final String name;

    ExecStatusEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    // 获取name
    public static String getName(Integer value) {
        for (ExecStatusEnum execStatusEnum : ExecStatusEnum.values()) {
            if (execStatusEnum.getValue().equals(value)) {
                return execStatusEnum.name;
            }
        }
        return null;
    }

    //获取value
    public static Integer getValue(String name) {
        for (ExecStatusEnum execStatusEnum : ExecStatusEnum.values()) {
            if (execStatusEnum.getName().equals(name)) {
                return execStatusEnum.value;
            }
        }
        return null;
    }


    public Integer getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }
}
