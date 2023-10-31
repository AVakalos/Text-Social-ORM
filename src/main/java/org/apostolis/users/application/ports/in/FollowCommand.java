package org.apostolis.users.application.ports.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apostolis.common.SelfValidating;

public record FollowCommand(
        @NotNull
        @NotBlank
        String user,
        @Positive
        int follows

) implements SelfValidating<FollowCommand> {

    public FollowCommand(String user, int follows){
        this.user = user;
        this.follows = follows;
        this.selfValidate();
    }
}
