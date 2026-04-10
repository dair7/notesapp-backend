package com.notesapp.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // Un solo lugar en todo el proyecto donde vive esta regla
    private static final String REGEX = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // null se considera válido: la obligatoriedad la maneja @NotBlank por separado
        if (password == null) return true;
        return password.matches(REGEX);
    }
}
