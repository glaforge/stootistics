package com.google.cloud.devrel.mastodon.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;

@JsonClassDescription
public record NewStatus(
    String status,
    String visibility
) {}
