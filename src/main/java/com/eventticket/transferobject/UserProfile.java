package com.eventticket.transferobject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserProfile {
    private Long userId;
    private String email;
    private Long orgId;
    private String role;

}
