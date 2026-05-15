package com.eventticket.transferobject;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

//@Data auto generates getters/setters/toString/equals/hashcode
@Data
public class RegisterRequest {
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Organization name is required")
    private String organizationName;
}
