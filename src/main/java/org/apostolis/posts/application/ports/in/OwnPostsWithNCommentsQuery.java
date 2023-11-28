package org.apostolis.posts.application.ports.in;

import jakarta.validation.constraints.Positive;
import org.apostolis.common.PageRequest;
import org.apostolis.common.validation.SelfValidating;

public record OwnPostsWithNCommentsQuery(
        Long user,
        @Positive
        int commentsNum,
        PageRequest pageRequest)  implements SelfValidating<OwnPostsWithNCommentsQuery>{
    public OwnPostsWithNCommentsQuery(Long user, int commentsNum, PageRequest pageRequest){
        this.user = user;
        this.commentsNum = commentsNum;
        this.pageRequest = pageRequest;
        this.selfValidate();
    }
}
