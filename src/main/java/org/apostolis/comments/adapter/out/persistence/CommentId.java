package org.apostolis.comments.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

// Wrapper for comment id
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class CommentId implements Serializable {
    private Long comment_id;

    public String toString(){
        return "Comment"+comment_id;
    }
}
