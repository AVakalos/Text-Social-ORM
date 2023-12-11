package org.apostolis.posts.domain;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

// Wrapper for post id
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class PostId implements Serializable {
    @Positive
    private Long post_id;
    public String toString(){
        return "Post"+post_id;
    }
}
