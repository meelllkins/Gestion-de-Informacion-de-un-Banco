package app.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
public class LogRecord {
    private String logId;
    private String operationType;
    private LocalDate operationDateTime;
    private int userId;
    private String userRole;
    private String affectedProductId;
    private String detailData;
}