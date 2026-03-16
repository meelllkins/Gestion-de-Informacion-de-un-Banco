package app.domain.models;
import java.time.LocalDate;

import app.domain.enums.SystemRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Setter 
@Getter
@NoArgsConstructor

public abstract class Person {
    private String name;
    private String identificationId;
    private String email;
    private String phone;
    private LocalDate birthDate; //L
    private String address;
    private SystemRole systemRole;
}
