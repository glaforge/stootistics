package com.google.cloud.devrel.mastodon;

import com.google.cloud.devrel.mastodon.model.*;
import com.google.cloud.devrel.mastodon.model.Status;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

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
    public Flux<StatusReach> index(@Body AccountServer accountServer) {
        return reachService.getAccountDetails(accountServer)
            .flatMapMany(accountDetails ->
                reachService.getStatuses(accountDetails, accountServer)
                    .flatMap(status -> getStatusReach(accountServer, accountDetails, status))
            );
    }

    @Post("/toot")
    @Consumes("application/json")
    @Produces("application/json")
    public Flux<StatusReach> toot(@Body TootUrl tootUrl) {
        AccountServer accountServer = new AccountServer("@" + tootUrl.user() + "@" + tootUrl.server());

        return reachService.getAccountDetails(accountServer)
            .flatMapMany(accountDetails ->
                reachService.getStatus(accountDetails, accountServer, tootUrl.tootID())
                    .flatMap(status -> getStatusReach(accountServer, accountDetails, status))
            );
    }

    private Mono<StatusReach> getStatusReach(AccountServer accountServer, AccountDetails accountDetails, Status status) {
        return reachService.getRebloggingAccounts(status, accountServer)
            .collectList().map(rebloggingAccounts -> {
                    int reblogs = rebloggingAccounts.stream()
                        .map(AccountDetails::followersCount)
                        .reduce(0, Integer::sum);

                    var rebloggedBy = rebloggingAccounts.stream()
                        .map((accDetails) -> accDetails.displayName() + " (" + accDetails.account() + ")")
                        .collect(Collectors.toList());

                    return new StatusReach(status, reblogs, accountDetails.followersCount(), status.favouriteCount(), rebloggedBy);
                }
            );
    }
}
