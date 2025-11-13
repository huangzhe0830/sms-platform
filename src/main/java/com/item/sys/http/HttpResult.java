package com.item.sys.http;

/**
 * HTTP结果封装
 */
public class HttpResult<T> {

	private int code = 200;
	private String msg;
	private T data;

	public static HttpResult error() {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, "未知异常，请联系管理员");
	}
	
	public static HttpResult error(String msg) {
		return error(HttpStatus.SC_INTERNAL_SERVER_ERROR, msg);
	}
	public static HttpResult error(HttpResultEnum httpResultEnum) {
		HttpResult r = new HttpResult();
		r.code = httpResultEnum.getCode();
		r.msg = httpResultEnum.getMessage();
		return r;
	}
	public static HttpResult error(int code, String msg) {
		HttpResult r = new HttpResult();
		r.setCode(code);
		r.setMsg(msg);
		return r;
	}

	public static HttpResult ok(Object data) {
		HttpResult r = new HttpResult();
		r.code = HttpResultEnum.SUCCESS.getCode();
		r.msg = HttpResultEnum.SUCCESS.getMessage();
		r.setData(data);
		return r;
	}
	public static HttpResult ok(HttpResultEnum httpResultEnum) {
		HttpResult r = new HttpResult();
		r.code = httpResultEnum.getCode();
		r.msg = httpResultEnum.getMessage();
		return r;
	}
	public static HttpResult ok() {
		return new HttpResult();
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	
}
