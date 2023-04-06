package com.google.cloud.devrel.mastodon;

import com.google.cloud.devrel.mastodon.model.*;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;

@Controller("/api/reach")
public class MastodonReachApi {

    @Inject
    private final MastodonReachService reachService;

    public MastodonReachApi(MastodonReachService reachService) {
        this.reachService = reachService;
    }

    @Post("/")
    @Consumes("application/json")
    @Produces("application/json")
    public Flux<StatusReach> accountReach(@Body AccountServer accountServer) {
        return reachService.getAccountReach(accountServer);
    }

    @Post("/toot")
    @Consumes("application/json")
    @Produces("application/json")
    public Flux<StatusReach> tootReach(@Body TootUrl tootUrl) {
        AccountServer accountServer = new AccountServer("@" + tootUrl.user() + "@" + tootUrl.server());
        return reachService.getTootReach(tootUrl, accountServer);
    }
}
