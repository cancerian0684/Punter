package org.shunya.kb.model;

import javax.persistence.*;

@Entity
@Table(name="ACCESS_COUNTER", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="COUNTER_ID",allocationSize=10)
public class AccessCounter {
    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator="seqGen")
    private long id;
    private long documentId;
    private long counter;

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

    public long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(long documentId) {
        this.documentId = documentId;
    }
}
