package app.application.adapters.persistance.mongodb.documents;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "log_records")
@Getter
@Setter
@NoArgsConstructor
public class LogRecordDocument {

    @Id
    private String id;

    private String operationType;
    private LocalDateTime operationDateTime;
    private String userId;
    private String userRole;
    private String affectedProductId;
    private Map<String, Object> detailData;
}