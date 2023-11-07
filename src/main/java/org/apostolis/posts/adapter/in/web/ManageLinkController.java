package org.apostolis.posts.adapter.in.web;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.apostolis.App;
import org.apostolis.posts.application.ports.in.CreateLinkCommand;
import org.apostolis.posts.application.ports.in.ManageLinkUseCase;
import org.apostolis.posts.application.service.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManageLinkController {
    private final ManageLinkUseCase linkService;

    private static final Logger logger = LoggerFactory.getLogger(ManageLinkController.class);
    public ManageLinkController(ManageLinkUseCase linkService) {
        this.linkService = linkService;
    }

    public void createLink(Context ctx){
        int post = ctx.pathParamAsClass("post", Integer.class).get();
        CreateLinkCommand createLinkCommand = new CreateLinkCommand(App.currentUserId.get(),post);
        String generated_url = linkService.createLink(createLinkCommand);
        ctx.result(generated_url);
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
