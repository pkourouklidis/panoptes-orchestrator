package org.lowcomote.panoptes.orchestrator.api;

public class AlgorithmExecutionResult {
	private String deployment;
	private String algorithmExecution;
	private int level;
	private String rawResult;

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
