package org.apostolis.posts.application.ports.in;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apostolis.common.validation.SelfValidating;

public record PostViewsQuery(
        int user,
        @PositiveOrZero
        int pageNum,
        @Positive
        int pageSize) implements SelfValidating<PostViewsQuery> {

    public PostViewsQuery(int user, int pageNum, int pageSize){
        this.user = user;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.selfValidate();
    }
}