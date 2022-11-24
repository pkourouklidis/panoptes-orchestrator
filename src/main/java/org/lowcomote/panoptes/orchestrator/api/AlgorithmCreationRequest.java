package org.lowcomote.panoptes.orchestrator.api;

import panoptesDSL.Algorithm;

public class AlgorithmCreationRequest {
	private String name;
	private String codebase;

	public AlgorithmCreationRequest(Algorithm a) {
		this.name = a.getName();
		this.codebase = a.getCodebase();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCodebase() {
		return codebase;
	}

	public void setCodebase(String codebase) {
		this.codebase = codebase;
	}
}
