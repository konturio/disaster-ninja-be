package io.kontur.disasterninja.controller.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BboxValidator.class)
@Documented
public @interface ValidBbox {

    String message() default "bbox has wrong format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
