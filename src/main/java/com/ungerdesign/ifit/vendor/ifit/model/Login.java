package com.ungerdesign.ifit.vendor.ifit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class Login {
    private String email;
    @ToString.Exclude
    private String password;
    private Boolean rememberMe;
}
