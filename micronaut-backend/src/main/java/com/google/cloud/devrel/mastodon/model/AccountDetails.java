package com.google.cloud.devrel.mastodon.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonClassDescription
public record AccountDetails(
        String id,
        String username,
        @JsonProperty("acct")
        String account,
        @JsonProperty("display_name")
        String displayName,
        String url,
        @JsonProperty("followers_count") int followersCount
) { }
