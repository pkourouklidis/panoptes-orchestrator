package org.lowcomote.panoptes.orchestrator.api;

import java.util.ArrayList;
import java.util.List;

public class ConciseAlgorithmExecutionResults {
	private List<Integer> level;
	private List<String> raw;
	
	public ConciseAlgorithmExecutionResults(List<AlgorithmExecutionResult> algorithmExecutionResults) {
		level = new ArrayList<Integer>();
		raw = new ArrayList<String>();
		
		for (AlgorithmExecutionResult r : algorithmExecutionResults) {
			this.level.add(r.getLevel());
			this.raw.add(r.getRawResult());
		}
	}
	
	public List<Integer> getLevel() {
		return level;
	}
	
	public void setLevel(List<Integer> level) {
		this.level = level;
	}
	
	public List<String> getRaw() {
		return raw;
	}
	
	public void setRaw(List<String> raw) {
		this.raw = raw;
	}
}
