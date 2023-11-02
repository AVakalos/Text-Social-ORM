package org.apostolis.comments.application.ports.in;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;
import org.apostolis.common.validation.StringEnumeration;
import org.apostolis.users.domain.Role;


public record CreateCommentCommand(
        @Positive
        int post,
        @NotNull
        @NotBlank
        String text,
        @StringEnumeration(enumClass = Role.class)
        String role

) implements SelfValidating<CreateCommentCommand> {

    @JsonCreator
    public CreateCommentCommand(@JsonProperty("post") int post, @JsonProperty("text") String text, @JsonProperty("role") String role){
        this.post = post;
        this.text = text;
        this.role = role;
        this.selfValidate();
    }
}
