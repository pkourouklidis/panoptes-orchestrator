package org.lowcomote.panoptes.orchestrator.api;

public class CountableTrigger {
	private String triggerType;
	private String count;
	private String deployment;

	public String getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(String triggerType) {
		this.triggerType = triggerType;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	public String getDeployment() {
		return deployment;
	}

	public void setDeployment(String deployment) {
		this.deployment = deployment;
	}
}
