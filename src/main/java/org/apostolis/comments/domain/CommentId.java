package org.apostolis.comments.domain;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

// Wrapper for comment id
@AllArgsConstructor
@EqualsAndHashCode
public class CommentId implements Serializable {
    @Positive
    private Long comment_id;

    public Long getValue() {
        return comment_id;
    }

    @Override
    public String toString(){
        return "Comment"+comment_id;
    }
}
