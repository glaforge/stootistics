package com.google.cloud.devrel.mastodon;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.cloud.devrel.mastodon.model.*;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.reactor.http.client.ReactorStreamingHttpClient;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class MastodonReachService {

    @Inject
    private ReactorStreamingHttpClient client;

    @Value("${mastodon.server}")
    private String mastodonServer;

    @Value("${mastodon.token}")
    private String bearerToken;

    public Mono<AccountDetails> getAccountDetails(AccountServer accountServer) {
        System.out.println("Get account details for " + accountServer.account());

        URI uri = UriBuilder
                .of("/api/v1/accounts/lookup")
                .scheme("https")
                .host(accountServer.server())
                .queryParam("acct", accountServer.account())
                .build();

        HttpRequest<?> request = HttpRequest
                .GET(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .contentType(MediaType.APPLICATION_JSON_TYPE);

        return client.retrieve(request, AccountDetails.class).single();
    }

    public Flux<Status> getStatuses(AccountDetails accountDetails, AccountServer accountServer) {
        System.out.println("Retrieving statuses");

        URI uri = UriBuilder
                .of("/api/v1/accounts/{id}/statuses")
                .scheme("https")
                .host(accountServer.server())
                .queryParam("limit", 80)    // TODO: implement pagination
                .queryParam("exclude_replies", true)
                .queryParam("exclude_reblogs", true)
                .expand(Map.of("id", accountDetails.id()));

        HttpRequest<?> request = HttpRequest
                .GET(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .contentType(MediaType.APPLICATION_JSON_TYPE);

        return client.jsonStream(request, Argument.of(Status.class));
    }
    public Flux<Status> getStatus(AccountDetails accountDetails, AccountServer accountServer, String tootId) {
        System.out.println("Retrieving single status");

        URI uri = UriBuilder
                .of("/api/v1/statuses/{tootId}")
                .scheme("https")
                .host(accountServer.server())
                .expand(Map.of(
                        "accountId", accountDetails.id(),
                        "tootId", tootId
                ));

        HttpRequest<?> request = HttpRequest
                .GET(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .contentType(MediaType.APPLICATION_JSON_TYPE);

        return client.retrieve(request, Argument.of(Status.class));
    }

    public Flux<AccountDetails> getRebloggingAccounts(Status status, AccountServer accountServer) {
        System.out.println("Get reach for status " + status.id());

        URI uri = UriBuilder
                .of("/api/v1/statuses/{statusId}/reblogged_by")
                .scheme("https")
                .host(accountServer.server())
                .queryParam("limit", 80)    // TODO: implement pagination
                .expand(Map.of("statusId", status.id()));

        HttpRequest<?> request = HttpRequest
                .GET(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .contentType(MediaType.APPLICATION_JSON_TYPE);

        return client.jsonStream(request, Argument.of(AccountDetails.class));
    }

    public Flux<StatusReach> getAccountReach(AccountServer accountServer) {
        return getAccountDetails(accountServer)
            .flatMapMany(accountDetails ->
                getStatuses(accountDetails, accountServer)
                    .flatMap(status -> getStatusReach(accountServer, accountDetails, status))
            );
    }

    public Flux<StatusReach> getTootReach(TootUrl tootUrl, AccountServer accountServer) {
        return getAccountDetails(accountServer)
            .flatMapMany(accountDetails ->
                getStatus(accountDetails, accountServer, tootUrl.tootID())
                    .flatMap(status -> getStatusReach(accountServer, accountDetails, status))
            );
    }

    private Mono<StatusReach> getStatusReach(AccountServer accountServer, AccountDetails accountDetails, Status status) {
        return getRebloggingAccounts(status, accountServer)
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

    public Flux<Notification> getNotifications() {
        System.out.println("Check notifications");

        URI uri = UriBuilder
            .of("/api/v1/notifications")
            .scheme("https")
            .host(mastodonServer)
            .queryParam("types[]", "mention")
            .build();

        HttpRequest<?> request = HttpRequest
            .GET(uri)
            .bearerAuth(bearerToken)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .contentType(MediaType.APPLICATION_JSON_TYPE);

        return client.jsonStream(request, Argument.of(Notification.class));
    }

    public Flux<String> dismissNotification(String id) {
        System.out.println("Dismiss notification: " + id);

        URI uri = UriBuilder
            .of("/api/v1/notifications/{id}/dismiss")
            .scheme("https")
            .host(mastodonServer)
            .expand(Map.of("id", id));

        HttpRequest<?> request = HttpRequest
            .POST(uri, null)
            .bearerAuth(bearerToken)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .contentType(MediaType.APPLICATION_JSON_TYPE);

        return client.retrieve(request);
    }

    public void dismissAllNotifications() {
        System.out.println("Dismiss ALL notification");

        URI uri = UriBuilder
            .of("/api/v1/notifications/clear")
            .scheme("https")
            .host(mastodonServer)
            .build();

        HttpRequest<?> request = HttpRequest
            .POST(uri, null)
            .bearerAuth(bearerToken)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .contentType(MediaType.APPLICATION_JSON_TYPE);

        client.toBlocking().retrieve(request);
    }

    public void postReply(String message) {
        System.out.println("Post reply");

        NewStatus status = new NewStatus(message, "unlisted");

        URI uri = UriBuilder
            .of("/api/v1/statuses")
            .scheme("https")
            .host(mastodonServer)
            .build();

        HttpRequest<?> request = HttpRequest
            .POST(uri, status)
            .bearerAuth(bearerToken)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED);

        client.toBlocking().retrieve(request);
    }


}
