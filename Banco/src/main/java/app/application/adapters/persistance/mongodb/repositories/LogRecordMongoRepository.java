package app.application.adapters.persistance.mongodb.repositories;

import app.application.adapters.persistance.mongodb.documents.LogRecordDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LogRecordMongoRepository extends MongoRepository<LogRecordDocument, String> {

    List<LogRecordDocument> findByAffectedProductId(String affectedProductId);

    List<LogRecordDocument> findByUserId(String userId);

    List<LogRecordDocument> findByOperationType(String operationType);
}