package org.lowcomote.panoptes.orchestrator.service;

import org.lowcomote.panoptes.orchestrator.api.CountableTrigger;
import org.lowcomote.panoptes.orchestrator.api.AlgorithmExecutionResult;
import org.lowcomote.panoptes.orchestrator.repository.AlgorithmExecutionResultRepository;
import org.lowcomote.panoptes.orchestrator.repository.StateMachineRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import static io.cloudevents.core.CloudEventUtils.mapData;

@Service
public class EventService {
	private StateMachineRepository stateMachineRepository;
	private AlgorithmExecutionResultRepository algorithmExecutionResultRepository;
	private ObjectMapper objectMapper;
	
	public EventService(StateMachineRepository stateMachineRepository, AlgorithmExecutionResultRepository algorithmExecutionResultRepository, ObjectMapper objectMapper) {
		this.stateMachineRepository = stateMachineRepository;
		this.algorithmExecutionResultRepository = algorithmExecutionResultRepository;
		this.objectMapper = objectMapper;
	}

	public void ingestEvent(CloudEvent event) {
		if (event.getType().equals("org.lowcomote.panoptes.baseAlgorithmExecution.result")) {
			AlgorithmExecutionResult executionResult = mapData(event,
					PojoCloudEventDataMapper.from(objectMapper, AlgorithmExecutionResult.class)).getValue();
			executionResult.setExecutionType("baseAlgorithmExecution");
			ingestAlgorithmExecutionResult(executionResult);
			algorithmExecutionResultRepository.save(executionResult);
		}
		else if (event.getType().equals("org.lowcomote.panoptes.trigger.sample")) {
			CountableTrigger trigger = mapData(event,
					PojoCloudEventDataMapper.from(objectMapper, CountableTrigger.class)).getValue();
			ingestCountableTrigger(trigger);
		}
	}

	@SuppressWarnings("deprecation")
	private void ingestAlgorithmExecutionResult(AlgorithmExecutionResult executionResult) {
		StateMachine<String, String> sm = stateMachineRepository.getMachine(executionResult.getDeployment());
		Message<String> m = MessageBuilder.withPayload(executionResult.getAlgorithmExecution().concat("-EXECUTIONRESULT"))
				.setHeader("level", executionResult.getLevel()).setHeader("rawResult", executionResult.getRawResult()).build();
		sm.sendEvent(m);
	}
	
	@SuppressWarnings("deprecation")
	private void ingestCountableTrigger(CountableTrigger trigger) {
		StateMachine<String, String> sm = stateMachineRepository.getMachine(trigger.getDeployment());
		Message<String> m = MessageBuilder.withPayload("TRIGGER")
				.setHeader("type", trigger.getTriggerType()).setHeader("count", trigger.getCount()).build();
		sm.sendEvent(m);
	}
}
