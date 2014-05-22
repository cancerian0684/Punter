package org.shunya.punter.jpa;

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.shunya.punter.tasks.Tasks;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@TableGenerator(name = "seqGen", table = "ID_GEN", pkColumnName = "GEN_KEY", valueColumnName = "GEN_VALUE", pkColumnValue = "SEQ_ID", allocationSize = 1)
public class TaskHistory implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "seqGen")
    private long id;
    private int sequence;
    @Lob
    @Basic(fetch = FetchType.EAGER)
//	@Column(columnDefinition="blob(6M)")
    private String logs;
    private boolean status;
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    private Date finishTime;
    @ManyToOne
    private TaskData task;
    @ManyToOne
    @JsonBackReference("ProcessHistory")
    private ProcessHistory processHistory;
    //	@Basic(optional = false)
//	@Column(nullable = false, columnDefinition = "char(1) default 'A'")
    @Enumerated(EnumType.STRING)
    private RunState runState = RunState.NEW;
    @Enumerated(EnumType.STRING)
    private RunStatus runStatus = RunStatus.NOT_RUN;
    @Transient
    private Tasks tasks;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public TaskData getTask() {
        return task;
    }

    public void setTask(TaskData task) {
        this.task = task;
    }

    public ProcessHistory getProcessHistory() {
        return processHistory;
    }

    public void setProcessHistory(ProcessHistory processHistory) {
        this.processHistory = processHistory;
    }

    public RunState getRunState() {
        return runState;
    }

    public void setRunState(RunState runState) {
        this.runState = runState;
    }

    public RunStatus getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(RunStatus runStatus) {
        this.runStatus = runStatus;
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
        if (!(obj instanceof TaskHistory))
            return false;
        TaskHistory other = (TaskHistory) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "org.shunya.punter.model.TaskHistory[id=" + id + "]";
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Date finishTime) {
        this.finishTime = finishTime;
    }

    public void setTasks(Tasks tasks) {
        this.tasks = tasks;
    }

    public Tasks getTasks() {
        return tasks;
    }
}
