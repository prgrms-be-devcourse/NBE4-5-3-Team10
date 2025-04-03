package com.tripfriend.global.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) // 런타임까지 유지
@Target(ElementType.METHOD) // 메서드에만 적용 가능
public @interface CheckPermission {
    String value(); // 권한 (ex: "ADMIN", "USER")
}
