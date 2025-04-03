package com.tripfriend.global.exception;

import com.tripfriend.global.dto.RsData;

public class ServiceException extends RuntimeException{

    private RsData<?> rsData;

    public ServiceException(String code, String msg){
        super(msg);
        rsData = new RsData<>(code, msg);
    }

    public String getCode(){
        return rsData.getCode();
    }

    public String getMsg(){
        return rsData.getMsg();
    }

    public int getStatusCode(){
        return rsData.getStatusCode();
    }

}

