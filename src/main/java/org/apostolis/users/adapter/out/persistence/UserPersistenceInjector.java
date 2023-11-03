package org.apostolis.users.adapter.out.persistence;

import org.apostolis.common.DbUtils;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.application.ports.out.UserRepository;

public class UserPersistenceInjector {
    private final DbUtils dbUtils;


    public UserPersistenceInjector(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    public UserRepository getUserRepository(){
        return new UserRepositoryImpl(dbUtils);
    }

    public FollowsRepository getFollowsRepository(){
        return new FollowsRepositoryImpl(dbUtils);
    }
}
