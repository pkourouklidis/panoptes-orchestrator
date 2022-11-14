package org.lowcomote.panoptes.orchestrator.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import panoptesDSL.BaseAlgorithmExecution;
import panoptesDSL.ModelIO;

public class BaseAlgorithmExecutionInfo {
	private String algorithm;
	private List<String> historicFeatures = new ArrayList<String>();
	private List<String> liveFeatures = new ArrayList<String>();
	private List<Map<Object, Object>> observations = new ArrayList<Map<Object, Object>>();
	
	public BaseAlgorithmExecutionInfo(BaseAlgorithmExecution execution, List<AlgorithmExecutionResult> results) {
		this.algorithm = execution.getAlgorithm().getName();
		for (ModelIO io : execution.getCurrentIOValues()) {
			this.liveFeatures.add(io.getName());
		}
		for (ModelIO io : execution.getHistoricIOValues()) {
			this.historicFeatures.add(io.getName());
		}
		for (AlgorithmExecutionResult result : results) {
			Map<Object, Object> observation = new HashMap<Object, Object>();
			observation.put("timestamp", result.getEndDate());
			observation.put("discretisedValue", result.getLevel());
			observation.put("rawValue", result.getRawResult());
			observation.put("UnacceptableShift", result.getLevel()>0);
			observations.add(observation);
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

	public List<Map<Object, Object>> getObservations() {
		return observations;
	}

	public void setObservations(List<Map<Object, Object>> observations) {
		this.observations = observations;
	}

}
