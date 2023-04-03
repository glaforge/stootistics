package com.google.cloud.devrel.mastodon;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/bot")
public class MastodonReachBot {
    @Post("/check-notifications")
    public void checkNotifications() {
        System.out.println("[BOT] checking notifications");

    }
}
