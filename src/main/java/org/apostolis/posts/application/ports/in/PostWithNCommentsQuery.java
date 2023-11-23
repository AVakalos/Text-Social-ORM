package org.apostolis.posts.application.ports.in;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apostolis.common.validation.SelfValidating;

public record PostWithNCommentsQuery(
        @Positive
        long post_id,

        @PositiveOrZero
        int comments_num) implements SelfValidating<PostViewsQuery> {

    public PostWithNCommentsQuery(long post_id, int comments_num){
        this.post_id = post_id;
        this.comments_num = comments_num;
        this.selfValidate();
    }
}
