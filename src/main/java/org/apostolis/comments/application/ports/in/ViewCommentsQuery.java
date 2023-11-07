package org.apostolis.comments.application.ports.in;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apostolis.common.validation.SelfValidating;

public record ViewCommentsQuery(
        int user,
        @PositiveOrZero
        int pageNum,
        @Positive
        int pageSize) implements SelfValidating<ViewCommentsQuery> {
    public ViewCommentsQuery(int user, int pageNum, int pageSize){
        this.user = user;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.selfValidate();
    }
}
