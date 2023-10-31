package org.apostolis.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringEnumerationValidator implements ConstraintValidator<StringEnumeration, String> {

    private Set<String> AVAILABLE_ENUM_NAMES;

    public static Set<String> getNamesSet(Class<? extends Enum<?>> e) {
        Enum<?>[] enums = e.getEnumConstants();
        String[] names = new String[enums.length];
        for (int i = 0; i < enums.length; i++) {
            names[i] = enums[i].name();
        }
        return new HashSet<>(Arrays.asList(names));
    }

    @Override
    public void initialize(StringEnumeration stringEnumeration) {
        Class<? extends Enum<?>> enumSelected = stringEnumeration.enumClass();
        AVAILABLE_ENUM_NAMES = getNamesSet(enumSelected);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!AVAILABLE_ENUM_NAMES.contains(value)) {

            String violation_message = value+" is not valid.\n\nThe valid options are:\n";
            for(String v: AVAILABLE_ENUM_NAMES){
                violation_message = violation_message.concat("\t"+v+"\n");
            }
            context.disableDefaultConstraintViolation();
            context
                    .buildConstraintViolationWithTemplate(violation_message)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}