package com.medtrack.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DosageValidator implements ConstraintValidator<ValidDosage, String> {
    private static final Pattern DOSAGE_PATTERN = Pattern.compile("^(\\d*\\.?\\d+)\\s*.*$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        Matcher matcher = DOSAGE_PATTERN.matcher(value.trim());
        if (matcher.find()) {
            try {
                double amount = Double.parseDouble(matcher.group(1));
                return amount > 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
}
