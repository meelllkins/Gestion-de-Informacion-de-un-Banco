package app.domain.services.implementations;

import app.domain.enums.SystemRole;
import app.domain.models.LogRecord;
import app.domain.models.User;
import app.domain.services.interfaces.IAuthService;
import app.domain.services.interfaces.ILogService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bitácora de Operaciones — simula almacenamiento NoSQL (MongoDB/DynamoDB).
 * En producción: cada LogRecord sería un documento insertado en una colección.
 * La lista es INMUTABLE: solo se agregan registros, nunca se modifican ni eliminan.
 */
public class LogService implements ILogService {

    private final List<LogRecord> logStore = new ArrayList<>();
    private final IAuthService authService;
    private int nextLogId = 1;

    public LogService(IAuthService authService) {
        this.authService = authService;
    }

    @Override
    public void log(String operationType, User user, String affectedProductId,
                    Map<String, Object> detailData) {
        LogRecord record = new LogRecord();
        record.setLogId("LOG-" + String.format("%05d", nextLogId++));
        record.setOperationType(operationType);
        record.setOperationDateTime(LocalDateTime.now());
        record.setUserId(parseId(user));
        record.setUserRole(user.getSystemRole() != null ? user.getSystemRole().toString() : "SYSTEM");
        record.setAffectedProductId(affectedProductId);
        Map<String, Object> safeDetail = (detailData != null) ? new HashMap<>(detailData) : new HashMap<>();
        record.setDetailData(safeDetail);

        logStore.add(record);

        // En producción: mongoCollection.insertOne(toDocument(record));
        System.out.println("[LogService] " + record.getLogId() + " | " + operationType +
                " | Producto: " + affectedProductId +
                " | Actor: " + (user.getName() != null ? user.getName() : "SISTEMA"));
    }

    @Override
    public List<LogRecord> getAllLogs(User requestingUser) {
        authService.validateRole(requestingUser, SystemRole.INTERNAL_ANALYST);
        return Collections.unmodifiableList(logStore);
    }

    @Override
    public List<LogRecord> getLogsByProduct(String affectedProductId, User requestingUser) {
        // La validación de que el producto pertenece al cliente la hace quien llama
        return logStore.stream()
                .filter(r -> affectedProductId.equals(r.getAffectedProductId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<LogRecord> getLogsByUser(int userId, User requestingUser) {
        authService.validateRole(requestingUser, SystemRole.INTERNAL_ANALYST);
        return logStore.stream()
                .filter(r -> r.getUserId() == userId)
                .collect(Collectors.toList());
    }

    private int parseId(User user) {
        try { return Integer.parseInt(user.getIdentificationId()); }
        catch (NumberFormatException e) { return 0; }
    }
}