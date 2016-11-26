package org.shunya.kb.model;

public class AccessEvent {
    private String entityName;
    private long entityId;

    public AccessEvent() {
    }

    public AccessEvent(String entityName, long entityId) {
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }
}
