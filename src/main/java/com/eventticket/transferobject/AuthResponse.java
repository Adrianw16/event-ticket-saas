package com.eventticket.transferobject;

public class AuthResponse {
    private Long userId;
    private Long orgId;
    private String email;
    private String token;

    public AuthResponse(Long userId, Long orgId, String email, String token) {
        this.userId = userId;
        this.orgId = orgId;
        this.email = email;
        this.token = token;
    }

    public Long getUserId() {return userId;}
    public Long getOrgId() {return orgId;}
    public String getEmail() {return email;}
    public String getToken() {return token;}

}
