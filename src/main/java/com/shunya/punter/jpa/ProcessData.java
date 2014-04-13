package com.shunya.punter.jpa;

import com.shunya.punter.utils.FieldPropertiesMap;

import javax.persistence.*;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name="PROCESS")
@TableGenerator(name="seqGen",table="ID_GEN",pkColumnName="GEN_KEY",valueColumnName="GEN_VALUE",pkColumnValue="SEQ_ID",allocationSize=1)
@XmlRootElement()
//@XmlAccessorOrder(value=XmlAccessOrder.ALPHABETICAL)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "username",
    "name",
    "description",
    "comments",
    "inputParams",
    "taskList"
})
public class ProcessData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3450975996342231267L;
	@Id
	@XmlTransient
	@GeneratedValue(strategy=GenerationType.TABLE, generator="seqGen")
	private long id;
	private String username;
	private String name;
	private String description;
    @Column(length = 500)
	private String comments;
    @Lob
	@Basic(fetch=FetchType.EAGER)
	private String inputParams;
	@OneToMany(cascade={CascadeType.REMOVE,CascadeType.PERSIST},mappedBy = "process",fetch=FetchType.EAGER)
	private List<TaskData> taskList;
	@OneToMany(cascade={CascadeType.REMOVE},mappedBy = "process",fetch=FetchType.LAZY)
	@XmlTransient
	private List<ProcessHistory> processHistoryList;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public List<TaskData> getTaskList() {
		return taskList;
	}
	public void setTaskList(List<TaskData> taskList) {
		this.taskList = taskList;
	}
	public List<ProcessHistory> getProcessHistoryList() {
		return processHistoryList;
	}
	public void setProcessHistoryList(List<ProcessHistory> processHistoryList) {
		this.processHistoryList = processHistoryList;
	}
	public FieldPropertiesMap getInputParams() throws JAXBException {
		return FieldPropertiesMap.convertXmlToObject(inputParams);
	}
	public void setInputParams(FieldPropertiesMap inputParams) throws JAXBException {
		this.inputParams = FieldPropertiesMap.convertObjectToXml(inputParams);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ProcessData))
			return false;
		ProcessData other = (ProcessData) obj;
		if (id != other.id)
			return false;
		return true;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
}
