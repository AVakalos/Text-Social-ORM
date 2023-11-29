package org.apostolis.posts.application.service;

import org.apostolis.AppConfig;
import org.apostolis.posts.adapter.out.persistence.PostId;
import org.apostolis.posts.application.ports.in.*;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.users.adapter.out.persistence.UserId;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


// User's custom url business logic
public class LinkService implements ManageLinkUseCase {

    private final PostRepository postRepository;
    private final PostViewsUseCase postViewsService;


    public LinkService(PostRepository postRepository, PostViewsUseCase postViewsService) {
        this.postRepository = postRepository;
        this.postViewsService = postViewsService;
    }

    @Override
    public String createLink(CreateLinkCommand createLinkCommand) {
        Long user = createLinkCommand.user();
        Long post = createLinkCommand.post_id();

        if(postRepository.isMyPost(new UserId(user), new PostId(post))){
            // register the link to prevent data leaks via url manipulation
            postRepository.registerLink(new PostId(post));
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

        String[] split =  decoded.split(",");
        long post_id = Long.parseLong(split[1]);
        if(postRepository.checkLink(new PostId(post_id))){
            PostWithNCommentsQuery query = new PostWithNCommentsQuery(post_id,100);
            return postViewsService.getPostWithNLatestComments(query);
        }else{
            throw new IllegalArgumentException("The link is invalid");
        }
    }
}
