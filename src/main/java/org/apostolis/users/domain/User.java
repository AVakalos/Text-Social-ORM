package org.apostolis.users.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apostolis.common.StringEnumeration;

public record User(
        @NotNull
        @NotBlank
        @Email
        String username,
        @NotNull
        @NotBlank
        @Size(min=8,max=20)
        String password,
        @NotBlank
        @StringEnumeration(enumClass = Role.class)
        String role
){ }

