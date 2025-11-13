package com.item.sys.model;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)//尽量不要有null，可有为空串“” 或者 0 或者 []， 但尽量不要null
@EqualsAndHashCode(callSuper = false)//调用父类的方法
public class SysUser{

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String userId;

    /**
     * 账号
     */
    private String account;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 盐
     */
    @JsonIgnore
    private String salt;

    /**
     *  姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     *  手机
     */
    private String mobile;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 删除标识
     */
    private Integer delFlag;

    /**
     * 密码输入异常次数
     */
    private Integer exceptionCount;
}