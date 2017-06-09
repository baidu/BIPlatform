package com.baidu.rigel.biplatform.parser.result;



public abstract class AbstractResult<T> implements ComputeResult {

    /** 
     * serialVersionUID
     */
    private static final long serialVersionUID = 4414418789997355453L;
    /** 
     * data
     */
    private T data;

    public AbstractResult() {
        super();
    }

    /** 
     * 获取 data 
     * @return the data 
     */
    public T getData() {
    
        return data;
    }

    /** 
     * 设置 data 
     * @param data the data to set 
     */
    public void setData(T data) {
    
        this.data = data;
    }

    
    
    @Override
    public String toString() {
        return data + "";
    }

}
