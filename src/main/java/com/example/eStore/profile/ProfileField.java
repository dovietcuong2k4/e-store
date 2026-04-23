package com.example.eStore.profile;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ProfileField {
    String label();

    ProfileFieldType type() default ProfileFieldType.TEXT;

    int order() default 0;

    boolean required() default false;

    int maxLength() default 255;

    String pattern() default "";

    String placeholder() default "";
}
