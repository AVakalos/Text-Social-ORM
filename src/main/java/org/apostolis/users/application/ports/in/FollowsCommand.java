package org.apostolis.users.application.ports.in;

import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;

public record FollowsCommand(

        @Positive
        Long user_id,
        @Positive
        Long follows

) implements SelfValidating<FollowsCommand> {

    public FollowsCommand(Long user_id, Long follows){
        this.user_id = user_id;
        this.follows = follows;
        this.selfValidate();
    }
}
