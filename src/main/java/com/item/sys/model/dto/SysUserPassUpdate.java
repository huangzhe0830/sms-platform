package com.item.sys.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SysUserPassUpdate {

    private String id;

    private String account;

    private String name;

    private String password;

    private String salt;

    private String pagePassword;

    private String newPassword;

    private String confirmNewPassword;
}