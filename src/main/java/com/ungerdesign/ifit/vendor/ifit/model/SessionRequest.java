package com.ungerdesign.ifit.vendor.ifit.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class SessionRequest {
    @JsonProperty("grant_type")
    private String grantType;

    private String username;

    private String password;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;
}
