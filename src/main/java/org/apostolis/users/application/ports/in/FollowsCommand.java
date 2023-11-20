package org.apostolis.users.application.ports.in;

import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;

public record FollowsCommand(

        // User may be removed from command
        @Positive
        long user,
        @Positive
        long follows

) implements SelfValidating<FollowsCommand> {

    public FollowsCommand(long user, long follows){
        this.user = user;
        this.follows = follows;
        this.selfValidate();
    }
}
