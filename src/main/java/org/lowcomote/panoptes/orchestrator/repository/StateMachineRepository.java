package org.lowcomote.panoptes.orchestrator.repository;

import java.util.HashMap;
import java.util.Map;

import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Repository;

@Repository
public class StateMachineRepository {

	private Map<String, StateMachine<String,String>> machines = new HashMap<String, StateMachine<String, String>>();
	
	public void addMachine(String machineId, StateMachine<String,String> machine) {
		machines.put(machineId, machine);
	}
	
	public StateMachine<String,String> getMachine(String machineId) {
		return machines.get(machineId);
	}
	
	public Map<String, StateMachine<String,String>> getMachines(){
		return machines;
	}
	
	public void deleteMachine(String machineId) {
		machines.remove(machineId);
	}
	
	public void clear() {
		machines = new HashMap<String, StateMachine<String, String>>();
	}
}
