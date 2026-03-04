package app.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import app.domain.enums.UserStatus;

@Setter
@Getter
@NoArgsConstructor

public class User extends Person {
    private String username; 
    private String password; 
    private UserStatus userStatus;
    private String relatedid; 
}