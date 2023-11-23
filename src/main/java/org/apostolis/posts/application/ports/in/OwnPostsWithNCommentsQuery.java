package org.apostolis.posts.application.ports.in;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apostolis.common.PageRequest;
import org.apostolis.common.validation.SelfValidating;

public record OwnPostsWithNCommentsQuery(
        long user,
        @Positive
        int commentsNum,
        PageRequest pageRequest
)  implements SelfValidating<OwnPostsWithNCommentsQuery>{
    public OwnPostsWithNCommentsQuery(long user, int commentsNum, PageRequest pageRequest){
        this.user = user;
        this.commentsNum = commentsNum;
        this.pageRequest = pageRequest;
        this.selfValidate();
    }
}
