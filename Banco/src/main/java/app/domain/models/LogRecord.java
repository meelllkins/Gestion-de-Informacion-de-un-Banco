package app.domain.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
public class LogRecord {
    private String logId;
    private String operationType;
    private LocalDateTime operationDateTime;  // LocalDateTime, no LocalDate
    private int userId;
    private String userRole;
    private String affectedProductId;
    private Map<String, Object> detailData;   // ← este es el cambio
}