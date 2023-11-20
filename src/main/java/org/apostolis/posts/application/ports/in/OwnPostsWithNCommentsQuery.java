package org.apostolis.posts.application.ports.in;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apostolis.common.validation.SelfValidating;

public record OwnPostsWithNCommentsQuery(
        long user,
        @Positive
        int commentsNum,
        @PositiveOrZero
        int pageNum,
        @Positive
        int pageSize
)  implements SelfValidating<OwnPostsWithNCommentsQuery>{
    public OwnPostsWithNCommentsQuery(long user, int commentsNum, int pageNum, int pageSize){
        this.user = user;
        this.commentsNum = commentsNum;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.selfValidate();
    }
}
