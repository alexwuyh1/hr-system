package com.example.hr.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 验证员工工号是否已存在
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmployeeNoValidator.class)
@Documented
public @interface UniqueEmployeeNo {
    
    String message() default "员工工号已存在";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /** 排除的员工 ID（用于更新时忽略自身） */
    long excludeId() default -1;
}
