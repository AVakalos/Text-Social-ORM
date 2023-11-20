package org.apostolis.posts.application.ports.in;

import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;

public record CreateLinkCommand(
        long user,
        @Positive
        int post_id) implements SelfValidating<CreateLinkCommand> {

    public CreateLinkCommand(long user, int post_id){
        this.user = user;
        this.post_id = post_id;
        this.selfValidate();
    }
}
