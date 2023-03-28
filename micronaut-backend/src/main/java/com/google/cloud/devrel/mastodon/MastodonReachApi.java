package com.google.cloud.devrel.mastodon;

import com.google.cloud.devrel.mastodon.model.AccountServer;
import com.google.cloud.devrel.mastodon.model.Status;
import com.google.cloud.devrel.mastodon.model.StatusReach;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

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
    public List<StatusReach> index(@Body AccountServer accountServer) {
        var accountDetails = reachService.getAccountDetails(accountServer);
        System.out.println("accountDetails = " + accountDetails);

        var statuses = reachService.getStatuses(accountDetails, accountServer);
        System.out.println("statuses = " + statuses);

        var listOfStatusReach = statuses.stream().map((Status status) -> {
            var rebloggingAccounts = reachService.getRebloggingAccounts(status, accountServer);
            int reblogs = rebloggingAccounts.stream()
                    .map((accDetails) -> accDetails.followersCount())
                    .reduce(0, Integer::sum);
            var rebloggedBy = rebloggingAccounts.stream()
                    .map((accDetails) -> accDetails.displayName() + " (" + accDetails.account() + ")")
                    .collect(Collectors.toList());
            return new StatusReach(status, reblogs, accountDetails.followersCount(), status.favouriteCount(), rebloggedBy);
        }).toList();
        System.out.println("listOfStatusReach = " + listOfStatusReach);

        return listOfStatusReach;
    }
}
