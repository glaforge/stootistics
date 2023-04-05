package com.google.cloud.devrel.mastodon.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Nullable;

import java.time.Instant;

@JsonClassDescription
public record Notification(
    String id,
    String type,
    @JsonProperty("created_at")
    Instant createdAt,
    AccountDetails account,
    @Nullable Status status
) { }
