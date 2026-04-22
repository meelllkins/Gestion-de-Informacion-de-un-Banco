package app.domain.models;

import app.domain.models.enums.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor

public class User extends Person {
    private String username; 
    private String password; 
    private UserStatus userStatus;
    private String relatedid; 
}