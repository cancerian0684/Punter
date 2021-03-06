package org.shunya.kb.model;

import javax.persistence.*;

@Entity
@Table(name="SYNONYM_WORD", uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=10)
@Cacheable
public class SynonymWord {
    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator="seqGen")
    private long id;
    private String words;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }
}
