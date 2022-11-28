package org.lowcomote.panoptes.orchestrator.api;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import panoptesDSL.BaseAlgorithmExecution;
import panoptesDSL.Deployment;
import panoptesDSL.parameterValueEntry;

public class BaseAlgorithmExecutionRequest {
	private String modelName;
	private String deploymentName;
	private String historicalFeatures;
	private String liveFeatures;
	private String baseAlgorithmExecutionName;
	private String startDate;
	private String endDate;
	private String algorithmName;
	private Map<String, String> parameters;

	public BaseAlgorithmExecutionRequest(BaseAlgorithmExecution baseAlgorithmExecution, String startDate, String endDate) {
		this.modelName = ((Deployment) baseAlgorithmExecution.eContainer()).getMlModel().getName();
		this.deploymentName = ((Deployment) baseAlgorithmExecution.eContainer()).getName();
		this.historicalFeatures = String.join(",", baseAlgorithmExecution.getHistoricIOValues().stream()
				.map(io -> io.getName()).collect(Collectors.toList()));
		this.liveFeatures = String.join(",", baseAlgorithmExecution.getCurrentIOValues().stream()
				.map(io -> io.getName()).collect(Collectors.toList()));
		this.baseAlgorithmExecutionName = baseAlgorithmExecution.getName();
		this.startDate = startDate;
		this.endDate = endDate;
		this.algorithmName = baseAlgorithmExecution.getAlgorithm().getName();
		this.parameters = new HashMap<String, String>();
		for (parameterValueEntry entry : baseAlgorithmExecution.getParameterValueMap()) {
			parameters.put(entry.getKey(), entry.getValue());
		}
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

	public String getHistoricalFeatures() {
		return historicalFeatures;
	}

	public void setHistoricalFeatures(String historicalFeatures) {
		this.historicalFeatures = historicalFeatures;
	}

	public String getLiveFeatures() {
		return liveFeatures;
	}

	public void setLiveFeatures(String liveFeatures) {
		this.liveFeatures = liveFeatures;
	}

	public String getBaseAlgorithmExecutionName() {
		return baseAlgorithmExecutionName;
	}

	public void setBaseAlgorithmExecutionName(String baseAlgorithmExecutionName) {
		this.baseAlgorithmExecutionName = baseAlgorithmExecutionName;
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

	public String getAlgorithmName() {
		return algorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}
}
