package com.item.sys.http;

public enum HttpResultEnum {
	SUCCESS(200, "成功"),

	FAIL(100, "失败"),

	EXCEPTION(300, "系统异常"),

	UNLOGIN(201, "未登录"),
	
	SESSION_EXPIRES(50014, "会话过期了"),
	
	LOGINFAIL(202, "没有该用户或者密码错误");
	
	private Integer code;

	private String message;

	HttpResultEnum(Integer code, String message) {

		this.code = code;

		this.message = message;

	}

	public Integer getCode() {

		return code;

	}

	public void setCode(Integer code) {

		this.code = code;

	}

	public String getMessage() {

		return message;

	}
}
