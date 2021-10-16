package com.ungerdesign.ifit.vendor.ifit.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientInfo {
    private String clientId;
    private String clientSecret;
}
