package org.lowcomote.panoptes.orchestrator.api;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class AlgorithmExecutionResult {
	@Id
    @GeneratedValue
	private int id;
	private String deployment;
	private String algorithmExecution;
	private int level;
	private String rawResult;
	private Date startDate;
	private Date endDate;
	private String executionType;

	public String getDeployment() {
		return deployment;
	}

	public void setDeployment(String deployment) {
		this.deployment = deployment;
	}

	public String getAlgorithmExecution() {
		return algorithmExecution;
	}

	public void setAlgorithmExecution(String algorithmExecution) {
		this.algorithmExecution = algorithmExecution;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getRawResult() {
		return rawResult;
	}

	public void setRawResult(String rawResult) {
		this.rawResult = rawResult;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startdate) {
		this.startDate = startdate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getExecutionType() {
		return executionType;
	}

	public void setExecutionType(String executionType) {
		this.executionType = executionType;
	}
}
