package com.google.cloud.devrel.mastodon;

import com.google.cloud.devrel.mastodon.model.AccountDetails;
import com.google.cloud.devrel.mastodon.model.AccountServer;
import com.google.cloud.devrel.mastodon.model.Status;
import com.google.cloud.devrel.mastodon.model.StatusReach;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;

import java.util.List;
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
                                .flatMap(status -> reachService.getRebloggingAccounts(status, accountServer)
                                        .collectList().map(rebloggingAccounts -> {
                                                    int reblogs = rebloggingAccounts.stream()
                                                            .map(AccountDetails::followersCount)
                                                            .reduce(0, Integer::sum);
                                                    var rebloggedBy = rebloggingAccounts.stream()
                                                            .map((accDetails) -> accDetails.displayName() + " (" + accDetails.account() + ")")
                                                            .collect(Collectors.toList());
                                                    return new StatusReach(status, reblogs, accountDetails.followersCount(), status.favouriteCount(), rebloggedBy);
                                                }
                                        ))
                );
    }
}
