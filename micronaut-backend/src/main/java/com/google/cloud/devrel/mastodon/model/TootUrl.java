package com.google.cloud.devrel.mastodon.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;

import javax.validation.constraints.NotNull;

@JsonClassDescription
public record TootUrl(
        @NotNull String url
) {
    public String user() {
        int atIdx = url.indexOf('@') + 1;
        int slashIdx = url.indexOf('/', atIdx);
        return url.substring(atIdx, slashIdx);
    }

    public String server() {
        if (url.startsWith("https://")) {
            return url.substring(8, url.indexOf('/', 8));
        } else {
            return url.substring(0, url.indexOf('/'));
        }
    }

    public String tootID() {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
