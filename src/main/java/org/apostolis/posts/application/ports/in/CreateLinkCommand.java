package org.apostolis.posts.application.ports.in;

import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;

public record CreateLinkCommand(
        Long user,
        @Positive
        Long post_id) implements SelfValidating<CreateLinkCommand> {

    public CreateLinkCommand(Long user, Long post_id){
        this.user = user;
        this.post_id = post_id;
        this.selfValidate();
    }
}
