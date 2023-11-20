package org.apostolis.comments.application.ports.in;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apostolis.common.validation.SelfValidating;

public record ViewCommentsQuery(
        long user,
        @PositiveOrZero
        int pageNum,
        @Positive
        int pageSize) implements SelfValidating<ViewCommentsQuery> {
    public ViewCommentsQuery(long user, int pageNum, int pageSize){
        this.user = user;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.selfValidate();
    }
}
