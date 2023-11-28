package org.apostolis.posts.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class PostId implements Serializable {
    private Long post_id;

    public String toString(){
        return "Post"+post_id;
    }
}
