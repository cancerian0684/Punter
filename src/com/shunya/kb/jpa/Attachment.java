package com.sapient.kb.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@Table(name="ATTACHMENT")
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=1)
@SecondaryTable(name = "ATTACHMENT_LOB", pkJoinColumns = {
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID") 
})
public class Attachment implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seqGen")
	private long id;
	private long accessCount;
	private float priority;
	private boolean active;
	private String ext;
	private long length;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateCreated;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAccessed;
	private String title;
	private String category;
	private String tag;
	private String author;
	private String comments;
	@Lob
	@Basic(fetch = FetchType.EAGER)
	@Column(columnDefinition="blob(10M)",table = "ATTACHMENT_LOB")
	private byte[] content;
	@ManyToOne
	private Document document;
	@Version
	@Column(name = "OPT_LOCK")
	private Long version;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getAccessCount() {
		return accessCount;
	}
	public void setAccessCount(long accessCount) {
		this.accessCount = accessCount;
	}
	public float getPriority() {
		return priority;
	}
	public void setPriority(float priority) {
		this.priority = priority;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getDateAccessed() {
		return dateAccessed;
	}
	public void setDateAccessed(Date dateAccessed) {
		this.dateAccessed = dateAccessed;
	}
	public long getLength() {
		return length;
	}
	public void setLength(long length) {
		this.length = length;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}
	
}