package com.medtrack.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DosageValidator.class)
@Documented
public @interface ValidDosage {
    String message() default "Invalid dosage. Must contain a numeric value greater than zero (e.g., '500mg')";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
