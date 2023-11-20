package org.apostolis.posts.application.ports.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apostolis.common.validation.SelfValidating;
import org.apostolis.common.validation.StringEnumeration;
import org.apostolis.users.domain.Role;

public record CreatePostCommand(
        long user,
        @NotNull
        @NotBlank
        String text,
        @StringEnumeration(enumClass = Role.class)
        String role)

        implements SelfValidating<CreatePostCommand> {

    public CreatePostCommand(long user, String text, String role) {
        this.user = user;
        this.text = text;
        this.role= role;
        this.selfValidate();
    }
}
