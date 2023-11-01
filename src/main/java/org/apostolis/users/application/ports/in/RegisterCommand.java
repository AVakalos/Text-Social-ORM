package org.apostolis.users.application.ports.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apostolis.users.domain.Role;
import org.apostolis.common.validation.SelfValidating;
import org.apostolis.common.validation.StringEnumeration;

public record RegisterCommand(
        @NotNull
        @NotBlank
        @Email String username,
        @NotNull
        @NotBlank
        @Size(min = 8, max = 20) String password,
        @StringEnumeration(enumClass = Role.class)
        @NotBlank
        String role) implements SelfValidating<RegisterCommand> {

    public RegisterCommand(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.selfValidate();
    }
}
