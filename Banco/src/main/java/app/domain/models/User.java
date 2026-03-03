package app.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;
import app.domain.enums.SystemRole;
import app.domain.enums.UserStatus;

@Setter
@Getter
@NoArgsConstructor

public abstract class User {
    private String userId;
    private String relatedId;
    private String name;
    private String identificationId;
    private String email;
    private String phone;
    private Date birthDate;
    private String address;
    private UserStatus userStatus;
    private SystemRole systemRole;
}