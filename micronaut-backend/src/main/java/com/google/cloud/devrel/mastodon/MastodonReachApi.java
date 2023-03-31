package com.google.cloud.devrel.mastodon;

import com.google.cloud.devrel.mastodon.model.*;
import com.google.cloud.devrel.mastodon.model.Status;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.util.function.Function;
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
                    .flatMap(fetchStatusesAndReblogs(accountServer, accountDetails))
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
                    .flatMap(fetchStatusesAndReblogs(accountServer, accountDetails))
            );
    }

    private Function<Status, Publisher<StatusReach>> fetchStatusesAndReblogs(AccountServer accountServer, AccountDetails accountDetails) {
        return status -> reachService.getRebloggingAccounts(status, accountServer)
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
