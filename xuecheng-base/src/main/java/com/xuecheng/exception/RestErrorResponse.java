package com.xuecheng.exception;

import java.io.Serializable;

/**
 * @Author Planck
 * @Date 2023-03-27 - 21:52
 */
public class RestErrorResponse implements Serializable {
    private String errMessage;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
