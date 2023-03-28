package com.google.cloud.devrel.mastodon.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;

import javax.validation.constraints.NotNull;

@JsonClassDescription
public record AccountServer(@NotNull String account) {
    public String user() {
        return account.substring(1, account.lastIndexOf('@'));
    }

    public String server() {
        return account.substring(account.lastIndexOf('@') + 1);
    }
}
