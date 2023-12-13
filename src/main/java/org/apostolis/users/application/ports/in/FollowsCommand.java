package org.apostolis.users.application.ports.in;

import jakarta.validation.Valid;
import org.apostolis.common.validation.SelfValidating;
import org.apostolis.users.domain.UserId;

public record FollowsCommand(
        @Valid
        UserId user_id,
        @Valid
        UserId follows

) implements SelfValidating<FollowsCommand> {

    public FollowsCommand(UserId user_id, UserId follows){
        this.user_id = user_id;
        this.follows = follows;
        this.selfValidate();
    }
}
