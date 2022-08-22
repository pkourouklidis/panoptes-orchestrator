package org.lowcomote.panoptes.orchestrator.api;

import java.util.stream.Collectors;

import panoptesDSL.BaseAlgorithmExecution;
import panoptesDSL.Deployment;

public class AlgorithmExecutionRequest {
	private String modelName;
	private String deploymentName;
	private String historicalFeatures;
	private String liveFeatures;
	private String baseAlgorithmExecutionName;
	private String startDate;
	private String algorithmName;

	public AlgorithmExecutionRequest(BaseAlgorithmExecution baseAlgorithmExecution, String lastTrigger) {
		this.modelName = ((Deployment) baseAlgorithmExecution.eContainer()).getMlModel().getName();
		this.deploymentName = ((Deployment) baseAlgorithmExecution.eContainer()).getName();
		this.historicalFeatures = String.join(",", baseAlgorithmExecution.getHistoricIOValues().stream()
				.map(io -> io.getName()).collect(Collectors.toList()));
		this.liveFeatures = String.join(",", baseAlgorithmExecution.getCurrentIOValues().stream()
				.map(io -> io.getName()).collect(Collectors.toList()));
		this.baseAlgorithmExecutionName = baseAlgorithmExecution.getName();
		this.startDate = lastTrigger;
		this.algorithmName = baseAlgorithmExecution.getAlgorithm().getName();
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

	public String getAlgorithmName() {
		return algorithmName;
	}

	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}
}
