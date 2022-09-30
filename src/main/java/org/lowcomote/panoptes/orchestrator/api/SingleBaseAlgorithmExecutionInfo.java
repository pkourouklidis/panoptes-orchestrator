package org.lowcomote.panoptes.orchestrator.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import panoptesDSL.BaseAlgorithmExecution;
import panoptesDSL.ModelIO;

public class SingleBaseAlgorithmExecutionInfo {
	private String algorithm;
	private List<String> historicFeatures = new ArrayList<String>();
	private List<String> liveFeatures = new ArrayList<String>();
	private Date timestamp;
	private String rawValue;
	private int level;
	
	public SingleBaseAlgorithmExecutionInfo(BaseAlgorithmExecution execution, AlgorithmExecutionResult result) {
		this.algorithm = execution.getAlgorithm().getName();
		this.timestamp = result.getDate();
		this.rawValue = result.getRawResult();
		this.level = result.getLevel();
		for(ModelIO io : execution.getHistoricIOValues()) {
			this.historicFeatures.add(io.getName());
		}
		for(ModelIO io : execution.getCurrentIOValues()) {
			this.liveFeatures.add(io.getName());
		}
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public List<String> getHistoricFeatures() {
		return historicFeatures;
	}

	public void setHistoricFeatures(List<String> historicFeatures) {
		this.historicFeatures = historicFeatures;
	}

	public List<String> getLiveFeatures() {
		return liveFeatures;
	}

	public void setLiveFeatures(List<String> liveFeatures) {
		this.liveFeatures = liveFeatures;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getRawValue() {
		return rawValue;
	}

	public void setRawValue(String rawValue) {
		this.rawValue = rawValue;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	

}
