package org.apostolis.users.application.ports.in;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public LoginCommand(@JsonProperty("username") String username, @JsonProperty("password") String password) {
        this.username = username;
        this.password = password;
        this.selfValidate();
    }

}
