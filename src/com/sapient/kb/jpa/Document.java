package com.sapient.kb.jpa;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table(name="DOCUMENT")
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=1)
@SecondaryTable(name = "DOCUMENT_LOB", pkJoinColumns = {
@PrimaryKeyJoinColumn(name = "ID", referencedColumnName = "ID") 
}) 
public class Document implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seqGen")
	private long id;
	private long accessCount;
	private float priority;
	private boolean active;
	@Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateUpdated;
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateAccessed;
	private String title;
	private String ext="";
	private String category="/all";
	private String tag;
	private String author;
	@Lob
	@Basic(fetch = FetchType.EAGER)
	@Column(columnDefinition="blob(5M)",table = "DOCUMENT_LOB")
	private byte[] content;
	private String md5;
	@Transient
	private String plainContent="";
	@Transient
	private float score;
	@OneToMany(fetch=FetchType.EAGER)
    private Collection<Document> relatedDocs;
	@OneToMany(mappedBy="document",fetch=FetchType.EAGER,cascade=CascadeType.ALL)
    private Collection<Attachment> attachments;
	@ManyToMany(fetch=FetchType.EAGER)
	private Collection<Document> referenceDocs; 
	@ManyToMany(mappedBy = "referenceDocs",fetch=FetchType.EAGER) 
	private Collection<Document> docsReferred; 
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
	public Collection<Document> getRelatedDocs() {
		return relatedDocs;
	}
	public void setRelatedDocs(Collection<Document> relatedDocs) {
		this.relatedDocs = relatedDocs;
	}
	public Collection<Attachment> getAttachments() {
		return attachments;
	}
	public void setAttachments(Collection<Attachment> attachments) {
		this.attachments = attachments;
	}
	public String getPlainContent() {
		return plainContent;
	}
	public void setPlainContent(String plainContent) {
		this.plainContent = plainContent;
	}
	public Collection<Document> getReferenceDocs() {
		return referenceDocs;
	}
	public void setReferenceDocs(Collection<Document> referenceDocs) {
		this.referenceDocs = referenceDocs;
	}
	public Collection<Document> getDocsReferred() {
		return docsReferred;
	}
	public void setDocsReferred(Collection<Document> docsReferred) {
		this.docsReferred = docsReferred;
	}
	public Date getDateCreated() {
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	public Date getDateUpdated() {
		return dateUpdated;
	}
	public void setDateUpdated(Date dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	public Date getDateAccessed() {
		return dateAccessed;
	}
	public void setDateAccessed(Date dateAccessed) {
		this.dateAccessed = dateAccessed;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float score) {
		this.score = score;
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
	public int getLength(){
		return content.length;
	}
	
	/*@Override
	public String toString() {
		String attSize = getRelatedDocs()!=null?""+getRelatedDocs().size():"";
		String cl=getContent()!=null?""+getContent().length():"";
		return ""+getId()+" -- "+ getTitle()+ " -- "+cl+" -- "+" -- "+attSize;
		
	}*/
}
