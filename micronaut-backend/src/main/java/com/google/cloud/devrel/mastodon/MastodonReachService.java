package com.google.cloud.devrel.mastodon;

import java.net.URI;
import java.util.Map;

import com.google.cloud.devrel.mastodon.model.AccountDetails;
import com.google.cloud.devrel.mastodon.model.AccountServer;
import com.google.cloud.devrel.mastodon.model.Status;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.reactor.http.client.ReactorStreamingHttpClient;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Singleton
public class MastodonReachService {

    private final ReactorStreamingHttpClient client;

    public MastodonReachService(ReactorStreamingHttpClient clientFoAccounts) {
        this.client = clientFoAccounts;
    }

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
                .queryParam("limit", 50)    // TODO: check why it fails for some bigger lists
                .queryParam("exclude_replies", true)
                .queryParam("exclude_reblogs", true)
                .expand(Map.of("id", accountDetails.id()));

        HttpRequest<?> request = HttpRequest
                .GET(uri)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .contentType(MediaType.APPLICATION_JSON_TYPE);

        return client.jsonStream(request, Argument.of(Status.class));
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

        Flux<AccountDetails> allAccountDetails = client.jsonStream(request, Argument.of(AccountDetails.class));
//        System.out.println(status.id() + " reblogged by " + allAccountDetails.stream().map(accountDetails -> accountDetails.account()).collect(Collectors.toList()));

        return allAccountDetails;
    }
}
