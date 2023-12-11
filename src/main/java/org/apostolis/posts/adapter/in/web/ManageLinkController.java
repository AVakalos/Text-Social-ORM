package org.apostolis.posts.adapter.in.web;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.apostolis.App;
import org.apostolis.posts.application.ports.in.CreateLinkCommand;
import org.apostolis.posts.application.ports.in.ManageLinkUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

// Handles the user's created custom url encode and decode http requests
public class ManageLinkController {
    private final ManageLinkUseCase linkService;

    private static final Logger logger = LoggerFactory.getLogger(ManageLinkController.class);
    public ManageLinkController(ManageLinkUseCase linkService) {
        this.linkService = linkService;
    }

    public void createLink(Context ctx) throws Exception {
        Long post;
        try {
            post = ctx.pathParamAsClass("post", Long.class).get();
        }catch(Exception k){
            throw new BadRequestResponse("post parameter must be an integer");
        }
        CreateLinkCommand createLinkCommand = new CreateLinkCommand(App.currentUserId.get().getUser_id(),post);
        Map<String, Object> response = new HashMap<>();
        response.put("url",linkService.createLink(createLinkCommand));
        ctx.json(response);
    }

    public void decodeLink(Context ctx){
        try{
            ctx.json(linkService.decodeLink(ctx.pathParam("url")));
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new NotFoundResponse(e.getMessage());
        }
    }
}
