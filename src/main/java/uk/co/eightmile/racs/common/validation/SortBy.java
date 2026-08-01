package uk.co.eightmile.racs.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SortByValidator.class)
public @interface SortBy {
    String message() default "Invalid sortBy value";
    String[] allowed();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}