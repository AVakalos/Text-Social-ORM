package org.apostolis.comments.application.ports.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;
import org.apostolis.common.validation.StringEnumeration;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.Role;
import org.apostolis.users.domain.UserId;

public record CreateCommentCommand(
        @Valid
        UserId user,
        @Valid
        PostId post,
        @NotNull
        @NotBlank
        String text,
        @StringEnumeration(enumClass = Role.class)
        String role

) implements SelfValidating<CreateCommentCommand> {

    public CreateCommentCommand(UserId user, PostId post, String text, String role){
        this.user = user;
        this.post = post;
        this.text = text;
        this.role = role;
        this.selfValidate();
    }
}
