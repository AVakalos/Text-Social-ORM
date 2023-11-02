package org.apostolis.posts.application.ports.in;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apostolis.common.validation.SelfValidating;
import org.apostolis.common.validation.StringEnumeration;
import org.apostolis.users.domain.Role;

public record CreatePostCommand(
        @NotNull
        @NotBlank
        String text,
        @StringEnumeration(enumClass = Role.class)
        String role)

        implements SelfValidating<CreatePostCommand> {

    @JsonCreator
    public CreatePostCommand(@JsonProperty("text") String text, @JsonProperty("role") String role) {
        this.text = text;
        this.role= role;
        this.selfValidate();
    }
}
