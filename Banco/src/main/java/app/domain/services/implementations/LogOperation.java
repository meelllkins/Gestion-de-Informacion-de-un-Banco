package app.domain.services.implementations;

import app.domain.models.LogRecord;
import app.domain.models.User;
import app.domain.ports.ILogPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class LogOperation {

    private final ILogPort logPort;

    public LogOperation(ILogPort logPort) {
        this.logPort = logPort;
    }

    public void log(String operationType, User user,
                    String affectedProductId, Map<String, Object> detailData) {
        LogRecord record = new LogRecord();
        record.setLogId(UUID.randomUUID().toString());
        record.setOperationType(operationType);
        record.setOperationDateTime(LocalDateTime.now());
        record.setUserId(user.getIdentificationId());
        record.setUserRole(user.getSystemRole().toString());
        record.setAffectedProductId(affectedProductId);
        record.setDetailData(detailData);

        logPort.save(record);
    }
}