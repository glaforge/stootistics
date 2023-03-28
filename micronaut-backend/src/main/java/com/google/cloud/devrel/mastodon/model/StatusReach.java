package com.google.cloud.devrel.mastodon.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;

import java.util.List;

@JsonClassDescription
public record StatusReach(
        Status status,
        int reblogged,
        int followersCount,
        int favorites,
        List<String> rebloggedBy
) { }
