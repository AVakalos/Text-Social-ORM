package org.apostolis.users.application.ports.out;

import org.apostolis.exception.DatabaseException;

import java.util.ArrayList;

public interface FollowsRepository {
    void saveFollow(int user) throws DatabaseException;
    void deleteFollow(int user) throws DatabaseException;

    ArrayList<String> getFollowers(int user) throws DatabaseException;
    ArrayList<String> getUsersToFollow(int user) throws DatabaseException;
}
