package org.apostolis.users.application.ports.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apostolis.common.validation.SelfValidating;

public record LoginCommand(
        @NotNull
        @NotBlank
        String username,
        @NotNull
        @NotBlank
        String password) implements SelfValidating<LoginCommand> {

    public LoginCommand(String username,String password) {
        this.username = username;
        this.password = password;
        this.selfValidate();
    }

}
