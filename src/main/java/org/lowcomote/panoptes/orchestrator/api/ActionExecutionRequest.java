package org.lowcomote.panoptes.orchestrator.api;

import java.util.HashMap;
import java.util.Map;

import panoptesDSL.ActionExecution;
import panoptesDSL.parameterValueEntry;

public class ActionExecutionRequest {
	private Map<String, String> parameters;
	private String deployment;
	private String algorithmExecution;
	private int level;
	private String rawResult;
	
	public ActionExecutionRequest(ActionExecution actionExecution, String deployment, String algorithmExecution, int level, String rawResult) {
		this.parameters = new HashMap<String, String>();
		for (parameterValueEntry entry : actionExecution.getParameterValueMap()) {
			parameters.put(entry.getKey(), entry.getValue());
		}
		this.deployment = deployment;
		this.algorithmExecution = algorithmExecution;
		this.level = level;
		this.rawResult = rawResult;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

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
}
