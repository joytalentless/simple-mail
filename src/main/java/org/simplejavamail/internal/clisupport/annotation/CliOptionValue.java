package org.simplejavamail.internal.clisupport.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface CliOptionValue {
	String name();
	String description() default "";
	String helpLabel();
	boolean required() default true;
	String[] example() default {};
}