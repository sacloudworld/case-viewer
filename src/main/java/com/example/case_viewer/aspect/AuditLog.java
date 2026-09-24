package com.example.case_viewer.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to audit the execution of a method.
 * It can be applied to a method or a class.
 * If applied to a class, the annotation will be applied to all methods in the class.
 * If applied to a method, the annotation will be applied to the method.
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
}