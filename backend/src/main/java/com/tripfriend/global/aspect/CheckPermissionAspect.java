package com.tripfriend.global.aspect;

import com.tripfriend.global.annotation.CheckPermission;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class CheckPermissionAspect {

    @Pointcut("@annotation(com.tripfriend.global.annotation.CheckPermission)")
    public void checkPermissionPointcut() {}

    @Before("checkPermissionPointcut()")
    public void before(JoinPoint joinPoint) {
        // 현재 로그인된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        // 메서드에 붙은 어노테이션 정보 가져오기
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckPermission annotation = method.getAnnotation(CheckPermission.class);

        if (annotation != null) {
            String requiredRole = annotation.value(); // 필요한 권한
            boolean hasRole = authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + requiredRole));

            if (!hasRole) {
                throw new SecurityException("접근 권한이 없습니다: " + requiredRole);
            }
        }
    }
}
