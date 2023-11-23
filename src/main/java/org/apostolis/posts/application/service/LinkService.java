package org.apostolis.posts.application.service;

import org.apostolis.AppConfig;
import org.apostolis.posts.application.ports.in.*;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.users.adapter.out.persistence.UserEntity;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LinkService implements ManageLinkUseCase {

    private final PostRepository postRepository;

    private final PostViewsUseCase postViewsService;


    public LinkService(PostRepository postRepository, PostViewsUseCase postViewsService) {
        this.postRepository = postRepository;
        this.postViewsService = postViewsService;
    }

    @Override
    public String createLink(CreateLinkCommand createLinkCommand) {
        long user = createLinkCommand.user();
        long post = createLinkCommand.post_id();

//        ArrayList<Integer> currentUser = new ArrayList<>(List.of(createLinkCommand.user()));
//        Set<Integer> user_post_ids = postRepository.getPostsGivenUsersIds(
//                currentUser,0,Integer.MAX_VALUE).get(user).keySet();

        if(postRepository.isMyPost(user, post)){
            // register the link to prevent data leaks via url manipulation
            postRepository.registerLink(post);
        }else{
            throw new IllegalArgumentException("You cannot create shareable link for a post of another user");
        }
        String description = user+","+post;

        String host = AppConfig.readProperties().getProperty("host");
        String port = AppConfig.readProperties().getProperty("port");

        return "http://"+host+":"+port+"/"+ URLEncoder.encode(description, StandardCharsets.UTF_8);
    }

    @Override
    public List<Object> decodeLink(String url) {
        String host = AppConfig.readProperties().getProperty("host");
        String port = AppConfig.readProperties().getProperty("port");
        String encoded = url.replace("http://"+host+":"+port+"/","");
        String decoded = URLDecoder.decode(encoded, StandardCharsets.UTF_8);

        String[] splitted =  decoded.split(",");
//        long user_id = Long.parseLong(splitted[0]);
        long post_id = Long.parseLong(splitted[1]);
        if(postRepository.checkLink(post_id)){
            PostWithNCommentsQuery query = new PostWithNCommentsQuery(post_id,100);
            return postViewsService.getPostWithNLatestComments(query);
        }else{
            throw new IllegalArgumentException("The link is invalid");
        }
    }
}
