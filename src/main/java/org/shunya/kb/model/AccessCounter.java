package org.shunya.kb.model;

import javax.persistence.*;

@Entity
@Table(name="ACCESS_COUNTER", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="COUNTER_ID",allocationSize=10)
public class AccessCounter {
    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator="seqGen")
    private long id;
    private long counter;

    private long entityId;
    private String entityName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
