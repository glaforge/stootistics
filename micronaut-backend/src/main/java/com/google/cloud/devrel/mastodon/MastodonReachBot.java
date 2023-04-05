package com.google.cloud.devrel.mastodon;

import com.google.cloud.devrel.mastodon.model.AccountServer;
import com.google.cloud.devrel.mastodon.model.StatusReach;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller("/bot")
public class MastodonReachBot {

    @Inject
    MastodonReachService reachService;

    @Post("/check-notifications")
    public Flux<StatusReach> replyToNotif() {
        Flux<StatusReach> notificationsForStatusReach = reachService.getNotifications().flatMap(notification -> {
            String accountName = "@" + notification.account().account();

            Mono<StatusReach> maxStatusReach = reachService.getAccountReach(new AccountServer(accountName))
                .reduce((left, right) -> left.reblogged() > right.reblogged() ? left : right)
                .flatMap(statusReach -> {

                    StringBuilder messageBuilder = new StringBuilder(accountName + "\nThe following post had a potential reach of " +
                        (statusReach.reblogged() + statusReach.followersCount()) + " viewers. \n");
                    if (statusReach.rebloggedBy().size() > 0)
                        messageBuilder.append("It was shared by " + statusReach.rebloggedBy().size() + " persons. \n");
                    if (statusReach.favorites() > 0)
                        messageBuilder.append("It was favorited " + statusReach.favorites() + " times. \n");
                    messageBuilder.append("\n" + statusReach.status().url());

                    String msg = messageBuilder.toString();
                    System.out.println("message = " + msg);

                    reachService.postReply(msg);
                    reachService.dismissNotification(notification.id()).subscribe();

                    return Mono.just(statusReach);
                });

            return maxStatusReach;
        });
        //reachService.dismissAllNotifications();

        return notificationsForStatusReach;
    }

    @Post("/dismiss-all-notifications")
    public void dismissAllNotifications() {
        reachService.dismissAllNotifications();
    }
}
