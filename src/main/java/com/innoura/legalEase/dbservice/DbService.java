package com.innoura.legalEase.dbservice;

import com.innoura.legalEase.entity.CaseDetail;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DbService {

    private final MongoTemplate mongoTemplate;

    public DbService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get collection name from class.
     * First checks @Document annotation for collection name,
     * otherwise converts class simple name to camelCase plural format.
     */
    private <T> String getCollectionName(Class<T> clazz) {
        Document documentAnnotation = clazz.getAnnotation(Document.class);
        if (documentAnnotation != null && !documentAnnotation.collection().isEmpty()) {
            return documentAnnotation.collection();
        }
        
        // Convert class simple name to camelCase plural
        String simpleName = clazz.getSimpleName();
        return simpleName;

    }

    public <T> T save(T entity, String collectionName) {
        return mongoTemplate.save(entity, collectionName);
    }

    public <T> T save(T entity) {
        @SuppressWarnings("unchecked")
        Class<T> clazz = (Class<T>) entity.getClass();
        return mongoTemplate.save(entity, getCollectionName(clazz));
    }

    public <T> List<T> find(Query query, Class<T> clazz, String collectionName) {
        return mongoTemplate.find(query, clazz, collectionName);
    }

    public <T> List<T> find(Query query, Class<T> clazz) {
        return mongoTemplate.find(query, clazz, getCollectionName(clazz));
    }

    public <T> List<T> findAll(Class<T> clazz, String collectionName) {
        return mongoTemplate.findAll(clazz, collectionName);
    }

    public <T> List<T> findAll(Class<T> clazz) {
        return mongoTemplate.findAll(clazz, getCollectionName(clazz));
    }

    public <T> T findOne(Query query, Class<T> clazz, String collectionName) {
        return mongoTemplate.findOne(query, clazz, collectionName);
    }

    public <T> T findOne(Query query, Class<T> clazz) {
        return mongoTemplate.findOne(query, clazz, getCollectionName(clazz));
    }

    public <T> T findById(String id, Class<T> clazz) {
        // Use FieldNameConstants to avoid hardcoding field names
        String idFieldName;
        if (clazz == CaseDetail.class) {
            idFieldName = CaseDetail.Fields.caseId;
        } else {
            // Default to "id" for other entities, or you can extend this logic
            idFieldName = "id";
        }
        Query query = new Query(Criteria.where(idFieldName).is(id));
        return mongoTemplate.findOne(query, clazz, getCollectionName(clazz));
    }
}

