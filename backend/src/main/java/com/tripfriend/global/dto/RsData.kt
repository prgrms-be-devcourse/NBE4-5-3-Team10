package com.tripfriend.global.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RsData<T> {

    @NonNull
    private String code;

    @NonNull
    private String msg;

    private T data;

    public RsData(String code, String msg){
        this(code, msg, null);
    }

    // StatusCode가 Json 포함 되지 않는다.
    @JsonIgnore
    public int getStatusCode(){
        String statusCodeStr = code.split("-")[0];
        return Integer.parseInt(statusCodeStr);
    }
}
