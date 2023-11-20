package org.apostolis.comments.application.ports.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;
import org.apostolis.common.validation.StringEnumeration;
import org.apostolis.users.domain.Role;

public record CreateCommentCommand(
        long user,
        @Positive
        long post,
        @NotNull
        @NotBlank
        String text,
        @StringEnumeration(enumClass = Role.class)
        String role

) implements SelfValidating<CreateCommentCommand> {

    public CreateCommentCommand(long user, long post, String text, String role){
        this.user = user;
        this.post = post;
        this.text = text;
        this.role = role;
        this.selfValidate();
    }
}
