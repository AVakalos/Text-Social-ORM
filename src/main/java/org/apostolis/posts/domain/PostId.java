package org.apostolis.posts.domain;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

// Wrapper for post id

@AllArgsConstructor
@EqualsAndHashCode
public class PostId implements Serializable {
    @Positive
    private final Long post_id;

    public Long getValue() {
        return post_id;
    }

    public String toString(){
        return "Post"+post_id;
    }
}
