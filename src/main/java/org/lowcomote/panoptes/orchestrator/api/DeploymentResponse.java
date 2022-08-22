package org.lowcomote.panoptes.orchestrator.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import panoptesDSL.AlgorithmExecution;
import panoptesDSL.Deployment;

public class DeploymentResponse {
	private String id;
	private String modelid;
	private String displayName;
	private String status;
	private boolean healthy;
	private List<Map<Object, Object>> observations;

	public DeploymentResponse(Deployment deployment) {
		this.id = deployment.getName();
		this.modelid = deployment.getMlModel().getName();
		this.id = deployment.getName();
		this.status = "running";
		this.healthy = true;
		List<Map<Object, Object>> observationList = new ArrayList<Map<Object, Object>>();
		
		Map<Object, Object> baseAlgorithmExecutionsEntry = new HashMap<Object,Object>();
		baseAlgorithmExecutionsEntry.put("type", "BaseAlgorithmExecution");
		List<String> baseAlgorithmExecutionList = new ArrayList<String>();
		baseAlgorithmExecutionsEntry.put("executions", baseAlgorithmExecutionList);
		observationList.add(baseAlgorithmExecutionsEntry);
		
		Map<Object, Object> higherOrderAlgorithmExecutionsEntry = new HashMap<Object,Object>();
		higherOrderAlgorithmExecutionsEntry.put("type", "HigherOrderAlgorithmExecution");
		List<String> higherOrderAlgorithmExecutionList = new ArrayList<String>();
		baseAlgorithmExecutionsEntry.put("executions", higherOrderAlgorithmExecutionList);
		observationList.add(higherOrderAlgorithmExecutionsEntry);
		
		for (AlgorithmExecution execution : deployment.getAlgorithmexecutions()) {
			if (execution.eClass().getName().equals("BaseAlgorithmExecution")) {
				baseAlgorithmExecutionList.add(execution.getName());
			}
			else if (execution.eClass().getName().equals("HigherOrderAlgorithmExecution")) {
				higherOrderAlgorithmExecutionList.add(execution.getName());
			}
		}
		
		this.observations = observationList;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getModelid() {
		return modelid;
	}

	public void setModelid(String modelid) {
		this.modelid = modelid;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isHealthy() {
		return healthy;
	}

	public void setHealthy(boolean healthy) {
		this.healthy = healthy;
	}

	public List<Map<Object, Object>> getObservations() {
		return observations;
	}

	public void setObservations(List<Map<Object, Object>> observations) {
		this.observations = observations;
	}
}
