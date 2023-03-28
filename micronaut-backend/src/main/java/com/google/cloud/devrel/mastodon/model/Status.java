package com.google.cloud.devrel.mastodon.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.time.Instant;

@JsonClassDescription
public record Status(
    String id,
    URL url,
    @JsonProperty("reblogs_count")
    int reblogCount,
    @JsonProperty("favourites_count")
    int favouriteCount,
    String content,
    @JsonProperty("created_at")
    Instant createdAt
) { }
