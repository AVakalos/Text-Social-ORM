package org.apostolis.users.application.ports.in;

import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;

public record FollowsCommand(

        // User may be removed from command
//        @NotNull
//        @NotBlank
//        String user,
        @Positive
        int follows

) implements SelfValidating<FollowsCommand> {

    public FollowsCommand(int follows){
        //this.user = user;
        this.follows = follows;
        this.selfValidate();
    }
}
