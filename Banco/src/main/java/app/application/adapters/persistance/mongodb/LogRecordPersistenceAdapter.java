package app.application.adapters.persistance.mongodb;

import app.application.adapters.persistance.mongodb.documents.LogRecordDocument;
import app.application.adapters.persistance.mongodb.repositories.LogRecordMongoRepository;
import app.domain.models.LogRecord;
import app.domain.ports.ILogPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogRecordPersistenceAdapter implements ILogPort {

    private final LogRecordMongoRepository repository;

    public LogRecordPersistenceAdapter(LogRecordMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(LogRecord record) {
        LogRecordDocument document = toDocument(record);
        repository.save(document);
    }

    @Override
    public List<LogRecord> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<LogRecord> findByAffectedProductId(String affectedProductId) {
        return repository.findByAffectedProductId(affectedProductId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<LogRecord> findByUserId(int userId) {
        return repository.findByUserId(String.valueOf(userId)).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<LogRecord> findByOperationType(String operationType) {
        return repository.findByOperationType(operationType).stream()
                .map(this::toDomain).toList();
    }

    private LogRecordDocument toDocument(LogRecord record) {
        LogRecordDocument document = new LogRecordDocument();
        document.setOperationType(record.getOperationType());
        document.setOperationDateTime(record.getOperationDateTime());
        document.setUserId(record.getUserId());
        document.setUserRole(record.getUserRole());
        document.setAffectedProductId(record.getAffectedProductId());
        document.setDetailData(record.getDetailData());
        return document;
    }

    private LogRecord toDomain(LogRecordDocument document) {
        LogRecord record = new LogRecord();
        record.setLogId(document.getId());
        record.setOperationType(document.getOperationType());
        record.setOperationDateTime(document.getOperationDateTime());
        record.setUserId(document.getUserId());
        record.setUserRole(document.getUserRole());
        record.setAffectedProductId(document.getAffectedProductId());
        record.setDetailData(document.getDetailData());
        return record;
    }
}