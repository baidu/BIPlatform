package com.baidu.rigel.biplatform.ma.ds.exception;

/**
 * 数据源连接异常类
 * @author jiangyichao
 *
 */
public class DataSourceConnectionException extends Exception {

	/**
	 * 序列id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 含有异常信息的构造函数
	 * @param msg 异常信息
	 */
	public DataSourceConnectionException (String msg) {
		super(msg);
	}
	
	/**
	 * 异常构造函数
	 * @param msg 异常信息
	 * @param e 异常对象
	 */
	public DataSourceConnectionException (String msg, Exception e) {
		super(msg, e);
	}
	
	/**
	 * 无参异常构造函数
	 */
	public DataSourceConnectionException () {
		super();
	}
}
