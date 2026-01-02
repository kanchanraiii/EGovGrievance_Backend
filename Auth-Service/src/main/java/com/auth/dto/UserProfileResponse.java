package com.auth.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfileResponse {
    String userId;
    String email;
    String fullName;
    String phone;
    String role;
}
