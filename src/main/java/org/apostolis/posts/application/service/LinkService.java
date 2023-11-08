package org.apostolis.posts.application.service;

import io.javalin.http.BadRequestResponse;
import org.apostolis.App;
import org.apostolis.AppConfig;
import org.apostolis.posts.application.ports.in.CreateLinkCommand;
import org.apostolis.posts.application.ports.in.ManageLinkUseCase;
import org.apostolis.posts.application.ports.in.OwnPostsWithNCommentsQuery;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.out.PostRepository;

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
        int user = createLinkCommand.user();
        int post = createLinkCommand.post_id();

        ArrayList<Integer> currentUser = new ArrayList<>(List.of(createLinkCommand.user()));
        Set<Integer> user_post_ids = postRepository.getPostsGivenUsersIds(
                currentUser,0,Integer.MAX_VALUE).get(user).keySet();

        System.out.println(user_post_ids);

        if(!user_post_ids.contains(post)){
            throw new IllegalArgumentException("You cannot create shareable link for a post of another user");
        }

        // register the link to prevent data leaks via url manipulation
        postRepository.registerLink(user, post);
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
        int user_id = Integer.parseInt(splitted[0]);
        int post_id = Integer.parseInt(splitted[1]);
        if(postRepository.checkLink(user_id, post_id)){

            OwnPostsWithNCommentsQuery query = new OwnPostsWithNCommentsQuery(
                    user_id,100,0,Integer.MAX_VALUE);

            return postViewsService.getOwnPostsWithNLatestComments(query).get(post_id);
        }else{
            throw new IllegalArgumentException("The link is invalid");
        }
    }
}