package uk.co.eightmile.racs.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class SortByValidator implements ConstraintValidator<SortBy, String> {

    private Set<String> allowedValues;

    @Override
    public void initialize(SortBy annotation) {
        this.allowedValues = Set.of(annotation.allowed());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String[] parts = value.split(":");

        if (parts.length != 2) {
            return buildError(context, value);
        }

        String sortValue = parts[0];
        String sortDir = parts[1];

        boolean isValidDir = sortDir.equals("asc") || sortDir.equals("desc");
        boolean isValidField = allowedValues.contains(sortValue);

        if (isValidField && isValidDir) {
            return true;
        }

        return buildError(context, value);
    }

    private boolean buildError(ConstraintValidatorContext context, String value) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                "Invalid sortBy value '" + value + "'. Allowed values: "
                        + String.join(", ", allowedValues)
                        + " with :asc or :desc"
        ).addConstraintViolation();

        return false;
    }
}