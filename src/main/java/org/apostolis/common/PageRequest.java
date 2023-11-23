package org.apostolis.common;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apostolis.common.validation.SelfValidating;

public record PageRequest(
        @PositiveOrZero
        int pageNumber,
        @Positive
        int pageSize) implements SelfValidating<PageRequest> {

    public PageRequest(int pageNumber, int pageSize){
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.selfValidate();
    }

}
