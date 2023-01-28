package org.lowcomote.panoptes.orchestrator.api;

import java.util.HashMap;
import java.util.Map;

import panoptesDSL.Deployment;
import panoptesDSL.HigherOrderAlgorithmExecution;
import panoptesDSL.parameterValueEntry;

public class HigherOrderAlgorithmExecutionRequest {
	private int windowSize;
	private String higherOrderAlgorithmName;
	private String observedAlgorithmExecutionName;
	private String higherOrderAlgorithmExecutionName;
	private String startDate;
	private String endDate;
	private Map<String, String> parameters;
	private String deploymentName;

	public HigherOrderAlgorithmExecutionRequest(HigherOrderAlgorithmExecution execution,
			int windowSize, String startDate, String endDate) {
		this.windowSize = windowSize;
		this.higherOrderAlgorithmExecutionName = execution.getName();
		this.higherOrderAlgorithmName = execution.getAlgorithm().getName();
		this.observedAlgorithmExecutionName = execution.getAlgorithmExecution().getName();
		this.deploymentName = ((Deployment)execution.eContainer()).getName();
		this.startDate = startDate;
		this.endDate = endDate;
		this.parameters = new HashMap<String, String>();
		for (parameterValueEntry entry : execution.getParameterValueMap()) {
			parameters.put(entry.getKey(), entry.getValue());
		}
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public String getHigherOrderalgorithmName() {
		return higherOrderAlgorithmName;
	}

	public void setHigherOrderalgorithmName(String higherOrderalgorithmName) {
		higherOrderAlgorithmName = higherOrderalgorithmName;
	}

	public String getObservedAlgorithmExecutionName() {
		return observedAlgorithmExecutionName;
	}

	public void setObservedAlgorithmExecutionName(String observedAlgorithmExecutionName) {
		this.observedAlgorithmExecutionName = observedAlgorithmExecutionName;
	}

	public String getHigherOrderAlgorithmExecutionName() {
		return higherOrderAlgorithmExecutionName;
	}

	public void setHigherOrderAlgorithmExecutionName(String higherOrderAlgorithmExecutionName) {
		this.higherOrderAlgorithmExecutionName = higherOrderAlgorithmExecutionName;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}
}
