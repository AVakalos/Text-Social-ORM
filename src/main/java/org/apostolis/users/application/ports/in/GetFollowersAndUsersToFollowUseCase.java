package org.apostolis.users.application.ports.in;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.apostolis.common.validation.SelfValidating;

import java.util.ArrayList;

public interface GetFollowersAndUsersToFollowUseCase {
    ArrayList<String> getFollowers(GetFollowsQuery query);
    ArrayList<String> getUsersToFollow(GetFollowsQuery query);

    record GetFollowsQuery(@NotNull @Positive int user) implements SelfValidating<GetFollowsQuery> {
        public GetFollowsQuery(int user){
            this.user = user;
            this.selfValidate();
        }
    }
}
