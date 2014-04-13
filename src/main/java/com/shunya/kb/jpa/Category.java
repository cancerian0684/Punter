package com.shunya.kb.jpa;

import javax.persistence.*;

@Entity
@Table(name="CATEGORY")
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=1)
@Cacheable
public class Category {
    @Id
    @GeneratedValue(strategy= GenerationType.TABLE, generator="seqGen")
    private long id;
    private String category;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
