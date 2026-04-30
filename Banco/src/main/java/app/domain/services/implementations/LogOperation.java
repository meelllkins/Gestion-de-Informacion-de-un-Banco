package app.domain.services.implementations;

import app.domain.models.LogRecord;
import app.domain.models.User;
import app.domain.models.enums.SystemRole;
import app.domain.ports.ILogPort;
import app.domain.services.interfaces.ILogService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class LogOperation implements ILogService {

    private final ILogPort logPort;

    public LogOperation(ILogPort logPort) {
        this.logPort = logPort;
    }

    @Override
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

    @Override
    public List<LogRecord> getAllLogs(User requestingUser) {
        validateAnalyst(requestingUser);
        return logPort.findAll();
    }

    @Override
    public List<LogRecord> getLogsByProduct(String affectedProductId, User requestingUser) {
        return logPort.findByAffectedProductId(affectedProductId);
    }

    @Override
    public List<LogRecord> getLogsByUser(String userId, User requestingUser) {
        validateAnalyst(requestingUser);
        return logPort.findByUserId(Integer.parseInt(userId));
    }

    private void validateAnalyst(User user) {
        if (user.getSystemRole() != SystemRole.INTERNAL_ANALYST) {
            throw new SecurityException("Solo el Analista Interno puede consultar la bitácora completa.");
        }
    }
}