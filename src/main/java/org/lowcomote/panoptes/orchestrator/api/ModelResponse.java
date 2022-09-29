package org.lowcomote.panoptes.orchestrator.api;

import panoptesDSL.Model;

public class ModelResponse {
	private String id;
	private String displayName;

	public ModelResponse(Model model) {
		this.id = model.getName();
		this.displayName = model.getName();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
