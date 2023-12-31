package org.lowcomote.panoptes.orchestrator.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.lowcomote.panoptes.orchestrator.api.ActionExecutionRequest;
import org.lowcomote.panoptes.orchestrator.api.AlgorithmCreationRequest;
import org.lowcomote.panoptes.orchestrator.api.BaseAlgorithmExecutionRequest;
import org.lowcomote.panoptes.orchestrator.api.ConciseAlgorithmExecutionResults;
import org.lowcomote.panoptes.orchestrator.api.HigherOrderAlgorithmExecutionRequest;
import org.lowcomote.panoptes.orchestrator.api.AlgorithmExecutionResult;
import org.lowcomote.panoptes.orchestrator.api.BaseAlgorithmExecutionInfo;
import org.lowcomote.panoptes.orchestrator.api.SingleBaseAlgorithmExecutionInfo;
import org.lowcomote.panoptes.orchestrator.repository.AlgorithmExecutionResultRepository;
import org.lowcomote.panoptes.orchestrator.repository.StateMachineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.jackson.JsonFormat;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.http.HttpMessageFactory;
import io.cloudevents.http.impl.HttpMessageWriter;
import panoptesDSL.Algorithm;
import panoptesDSL.AlgorithmExecution;
import panoptesDSL.BaseAlgorithm;
import panoptesDSL.BaseAlgorithmExecution;
import panoptesDSL.CompositeTrigger;
import panoptesDSL.Deployment;
import panoptesDSL.HigherOrderAlgorithm;
import panoptesDSL.HigherOrderAlgorithmExecution;
import panoptesDSL.Model;
import panoptesDSL.PanoptesDSLPackage;
import panoptesDSL.Platform;
import panoptesDSL.TriggerGroup;
import panoptesDSL.actionExecutionEntry;

@Service
public class PlatformService {
	private Platform currentPlatform;
	private final ResourceSet resourceSet;
	private final StateMachineRepository stateMachineRepository;
	private final AlgorithmExecutionResultRepository algorithmExecutionResultRepository;
	private Logger logger;
	private String brokerURL;
	private RestTemplate restTemplate;

	public PlatformService(StateMachineRepository stateMachineRepository,
			AlgorithmExecutionResultRepository algorithmExecutionResultRepository) {
		this.stateMachineRepository = stateMachineRepository;
		this.algorithmExecutionResultRepository = algorithmExecutionResultRepository;
		this.restTemplate = new RestTemplate();
		this.resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(PanoptesDSLPackage.eNS_URI, PanoptesDSLPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		this.logger = LoggerFactory.getLogger(PlatformService.class);
		this.brokerURL = "http://broker-ingress.knative-eventing.svc.cluster.local/panoptes/default";
		}

	public Deployment getDeployment(String name) {
		if (currentPlatform != null) {
			for (Deployment d : currentPlatform.getDeployments()) {
				if (d.getName().equals(name)) {
					return d;
				}
			}
		}
		return null;
	}

	public List<Deployment> getDeployments() {
		if (currentPlatform != null) {
			return currentPlatform.getDeployments();
		}
		return new ArrayList<Deployment>();
	}

	public List<Model> getModels() {
		if (currentPlatform != null) {
			return currentPlatform.getMlModels();
		}
		return new ArrayList<Model>();
	}

	public Optional<ConciseAlgorithmExecutionResults> getSpecificExecutionResults(String algorithmExecutionName, int count) {
		boolean found = false;
		outerloop:
		for (Deployment d : currentPlatform.getDeployments()) {
			for (AlgorithmExecution a : d.getAlgorithmexecutions())
				if (a.getName().equals(algorithmExecutionName)) {
					found = true;
					break outerloop;
				}
		}
		
		if (!found) {
			return Optional.empty();
		}
		
		Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "endDate"));
		List<AlgorithmExecutionResult> results = algorithmExecutionResultRepository
				.findByAlgorithmExecution(algorithmExecutionName, pageable);
		return Optional.of(new ConciseAlgorithmExecutionResults(results));
	}
	
	public BaseAlgorithmExecutionInfo getSpecificExecutionInfoAndResults(String deploymentName, String algorithmExecutionName,
			String executionType, Integer count) {
		Deployment deployment = getDeployment(deploymentName);
		if (deployment != null) {
			if (executionType.equals("baseAlgorithmExecutions")) {
				BaseAlgorithmExecution algorithmExecution = null;
				for (AlgorithmExecution execution : deployment.getAlgorithmexecutions()) {
					if (execution.getName().equals(algorithmExecutionName)
							&& execution.eClass().getName().equals("BaseAlgorithmExecution")) {
						algorithmExecution = (BaseAlgorithmExecution) execution;
						break;
					}
				}
				if (algorithmExecution != null) {
					return getSpecificBaseAlgorihtmExecutionResults(deployment, algorithmExecution, count);
				}

			}
		}
		return null;
	}

	public BaseAlgorithmExecutionInfo getSpecificBaseAlgorihtmExecutionResults(Deployment deployment,
			BaseAlgorithmExecution algorithmExecution, Integer count) {
		Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "endDate"));
		List<AlgorithmExecutionResult> results = algorithmExecutionResultRepository
				.findByDeploymentAndAlgorithmExecution(deployment.getName(), algorithmExecution.getName(), pageable);
		return new BaseAlgorithmExecutionInfo(algorithmExecution, results);
	}

	public List<SingleBaseAlgorithmExecutionInfo> getExecutionResultsByType(String deploymentName, String executionType,
			Integer count) {
		Deployment deployment = getDeployment(deploymentName);
		if (deployment != null) {
			if (executionType.equals("baseAlgorithmExecutions")) {
				Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "endDate"));
				List<AlgorithmExecutionResult> results = algorithmExecutionResultRepository
						.findByDeploymentAndExecutionType(deploymentName, "baseAlgorithmExecution", pageable);
				List<SingleBaseAlgorithmExecutionInfo> finalResult = new ArrayList<SingleBaseAlgorithmExecutionInfo>();
				for (AlgorithmExecutionResult result : results) {
					BaseAlgorithmExecution executionDefinition = null;
					for (AlgorithmExecution execution : deployment.getAlgorithmexecutions()) {
						if (execution.getName().equals(result.getAlgorithmExecution())
								&& execution.eClass().getName().equals("BaseAlgorithmExecution")) {
							executionDefinition = (BaseAlgorithmExecution) execution;
						}
					}
					if (executionDefinition != null) {
						finalResult.add(new SingleBaseAlgorithmExecutionInfo(executionDefinition, result));
					}
				}
				return finalResult;
			}
		}
		return null;
	}

	synchronized public void updatePlatform(String platformXMI) throws Exception {
		Platform newPlatform = parsePlatform(platformXMI);
		Map<String, StateMachine<String, String>> stateMachineCache = stateMachineRepository.getMachines();
		stateMachineRepository.clear();

		for (Deployment d : newPlatform.getDeployments()) {
			if (d.getTriggerGroups().size() > 0) {
				StateMachine<String, String> sm = buildStateMachine(d, stateMachineCache);
				stateMachineRepository.addMachine(d.getName(), sm);
			}
		}
		
		ObjectMapper objectMapper = new ObjectMapper();
		for(Algorithm a : newPlatform.getAlgorithms()) {
				logger.info("Triggering algorithm creation: " + a.getName());
				a.getCodebase();
				a.getName();
				AlgorithmCreationRequest requestObject = new AlgorithmCreationRequest(a);
				String runtimeName;
				if( a.eClass().getClassifierID()==PanoptesDSLPackage.BASE_ALGORITHM) {
					runtimeName = ((BaseAlgorithm) a).getRuntime().getName();
				}
				else {
					runtimeName = ((HigherOrderAlgorithm) a).getRuntime().getName();
				}
				
				CloudEvent event = CloudEventBuilder.v1().withId(UUID.randomUUID().toString())
						.withType("org.lowcomote.panoptes.algorithm.create")
						.withSource(java.net.URI.create("panoptes.orchestrator"))
						.withData(objectMapper.writeValueAsBytes(requestObject))
						.withSubject(runtimeName).build();
				sendEvent(event);
		}

		this.currentPlatform = newPlatform;
	}

	private Platform parsePlatform(String platformXMI) throws IOException {
		Resource resource = this.resourceSet.createResource(URI.createURI("newResource.xmi"));
		resource.load(new ByteArrayInputStream(platformXMI.getBytes()), null);
		return (Platform) resource.getContents().get(0);
	}

	private StateMachine<String, String> buildStateMachine(Deployment newDeployment,
			Map<String, StateMachine<String, String>> stateMachineCache) throws Exception {
		
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates().withStates().initial("IDLE");
		builder.configureConfiguration()
			.withConfiguration()
			.beanFactory(new StaticListableBeanFactory())
			.machineId(newDeployment.getName())
			.autoStartup(true);
		
		// Add one transition per trigger group that sends algorithm execution requests
		for (TriggerGroup triggerGroup : newDeployment.getTriggerGroups()) {
			builder.configureTransitions()
				.withInternal()
				.source("IDLE")
				.event("TRIGGER")
				.guard(buildGuard(triggerGroup))
				.action(buildBaseAlgorithmExecutionTrigger(triggerGroup));
		}

		// Add one transition per algorithm execution that reads the result the
		// execution and sends an action execution request accordingly
		for (AlgorithmExecution execution : newDeployment.getAlgorithmexecutions()) {
			builder.configureTransitions()
				.withInternal()
				.source("IDLE")
				.event(execution.getName().concat("-EXECUTIONRESULT"))
				.action(buildActionExecutionTrigger(execution));
			
			if (execution.eClass().getName().equals("HigherOrderAlgorithmExecution")) {
				HigherOrderAlgorithmExecution HoExecution = (HigherOrderAlgorithmExecution) execution;
				builder.configureTransitions()
					.withInternal()
					.source("IDLE")
					.event(HoExecution.getAlgorithmExecution().getName().concat("-EXECUTIONRESULT-NOTIFY"))
					.action(buildHigherOrderAlgorithmExecutionTrigger(HoExecution));
			}
		}
		
		StateMachine<String, String> sm = builder.build();
		
		// Initialise individual triggergroup states
		for (TriggerGroup triggerGroup : newDeployment.getTriggerGroups()) {
			String triggerGroupName = triggerGroup.getName();
			int sampleInitial = 0;
			int predictionInitial = 0;
			int labelInitial = 0;
			boolean timerInitial = false;
			String lastTriggerInitial = java.time.Instant.now().toString();
			if (stateMachineCache.containsKey(newDeployment.getName())) {
				sampleInitial = (int) stateMachineCache.get(newDeployment.getName()).getExtendedState().getVariables()
						.getOrDefault("sample".concat(triggerGroupName), 0);
				predictionInitial = (int) stateMachineCache.get(newDeployment.getName()).getExtendedState()
						.getVariables().getOrDefault("prediction".concat(triggerGroupName), 0);
				labelInitial = (int) stateMachineCache.get(newDeployment.getName()).getExtendedState().getVariables()
						.getOrDefault("label".concat(triggerGroupName), 0);
				timerInitial = (boolean) stateMachineCache.get(newDeployment.getName()).getExtendedState()
						.getVariables().getOrDefault("timer".concat(triggerGroupName), false);
				lastTriggerInitial = (String) stateMachineCache.get(newDeployment.getName()).getExtendedState()
						.getVariables()
						.getOrDefault("lastTrigger".concat(triggerGroupName), java.time.Instant.now().toString());
			}

			sm.getExtendedState().getVariables().put("sample".concat(triggerGroupName), sampleInitial);
			sm.getExtendedState().getVariables().put("prediction".concat(triggerGroupName), predictionInitial);
			sm.getExtendedState().getVariables().put("label".concat(triggerGroupName), labelInitial);
			sm.getExtendedState().getVariables().put("timer".concat(triggerGroupName), timerInitial);
			sm.getExtendedState().getVariables().put("lastTrigger".concat(triggerGroupName), lastTriggerInitial);
		}
		
		for (AlgorithmExecution execution : newDeployment.getAlgorithmexecutions()) {
			if (execution.eClass().getName().equals("HigherOrderAlgorithmExecution")) {
				String lastTriggerInitial = java.time.Instant.now().toString();
				if (stateMachineCache.containsKey(newDeployment.getName())) {
					lastTriggerInitial = (String) stateMachineCache.get(newDeployment.getName()).getExtendedState()
							.getVariables()
							.getOrDefault("lastTrigger".concat(execution.getName()), java.time.Instant.now().toString());
				}
				sm.getExtendedState().getVariables().put("lastTrigger".concat(execution.getName()), lastTriggerInitial);
			}
		}
		
		return sm;
	}

	private Guard<String, String> buildGuard(TriggerGroup triggerGroup) {
		final String triggerGroupName = triggerGroup.getName();
		return new Guard<String, String>() {
			@Override
			synchronized public boolean evaluate(StateContext<String, String> context) {
				int count;
				// increment relevant counter
				switch ((String) context.getMessage().getHeaders().get("type")) {
				case "sample":
					count = (int) context.getMessage().getHeaders().get("count");
					context.getExtendedState().getVariables().put("sample".concat(triggerGroupName),
							(int) context.getExtendedState().getVariables().get("sample".concat(triggerGroupName))
									+ count);
					break;
				case "prediction":
					count = (int) context.getMessage().getHeaders().get("count");
					context.getExtendedState().getVariables().put("prediction".concat(triggerGroupName),
							(int) context.getExtendedState().getVariables().get("prediction".concat(triggerGroupName))
									+ count);
					break;
				case "label":
					count = (int) context.getMessage().getHeaders().get("count");
					context.getExtendedState().getVariables().put("label".concat(triggerGroupName),
							(int) context.getExtendedState().getVariables().get("label".concat(triggerGroupName))
									+ count);
					break;
				case "timer":
					context.getExtendedState().getVariables().put("timer".concat(triggerGroupName), true);
					break;
				}
				
				boolean trigger = false;
				for (CompositeTrigger compositeTrigger : triggerGroup.getCompositeTriggers()) {
					// Within individual CompositeTriggers, even if one of the simple triggers is
					// not satisfied, we cannot trigger
					if (compositeTrigger.getTt() != null) {
						if ((boolean) context.getExtendedState().getVariables()
								.get("timer".concat(triggerGroupName)) == false) {
							continue;
						}
					}

					if (compositeTrigger.getSt() != null) {
						if ((int) context.getExtendedState().getVariables()
								.get("sample".concat(triggerGroupName)) < compositeTrigger.getSt().getFrequency()) {
							continue;
						}
					}

					if (compositeTrigger.getPt() != null) {
						if ((int) context.getExtendedState().getVariables()
								.get("prediction".concat(triggerGroupName)) < compositeTrigger.getPt().getFrequency()) {
							continue;
						}
					}

					if (compositeTrigger.getLt() != null) {
						if ((int) context.getExtendedState().getVariables()
								.get("label".concat(triggerGroupName)) < compositeTrigger.getLt().getFrequency()) {
							continue;
						}
					}
					trigger = true;
					break;
				}
				
				if (trigger) {
					// reset counters
					context.getExtendedState().getVariables().put("sample".concat(triggerGroupName), 0);
					context.getExtendedState().getVariables().put("prediction".concat(triggerGroupName), 0);
					context.getExtendedState().getVariables().put("label".concat(triggerGroupName), 0);
					context.getExtendedState().getVariables().put("timer".concat(triggerGroupName), false);
					return true;
				}
				else {
					return false;
				}

			}
		};
	}

	private Action<String, String> buildBaseAlgorithmExecutionTrigger(TriggerGroup triggerGroup) {
		final String triggerGroupName = triggerGroup.getName();
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context){
				ObjectMapper objectMapper = new ObjectMapper();
				String now = java.time.Instant.now().toString();
				for (BaseAlgorithmExecution baseAlgorithmExecution : triggerGroup.getTargets()) {
					try {
						logger.info("Triggering base algorithm execution: " + baseAlgorithmExecution.getName());
						BaseAlgorithmExecutionRequest requestObject = new BaseAlgorithmExecutionRequest(baseAlgorithmExecution,
								context.getExtendedState().get("lastTrigger".concat(triggerGroupName), String.class),
								now);
						CloudEvent event = CloudEventBuilder.v1().withId(UUID.randomUUID().toString())
								.withType("org.lowcomote.panoptes.baseAlgorithmExecution.trigger")
								.withSource(java.net.URI.create("panoptes.orchestrator"))
								.withData(objectMapper.writeValueAsBytes(requestObject))
								.withSubject(baseAlgorithmExecution.getAlgorithm().getRuntime().getName())
								.build();
						sendEvent(event);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				context.getExtendedState().getVariables().put("lastTrigger".concat(triggerGroupName), now);
			}
		};
	}
	
	private Action<String, String> buildHigherOrderAlgorithmExecutionTrigger(HigherOrderAlgorithmExecution execution) {
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context){
				String deploymentName = ((Deployment)execution.eContainer()).getName();
				String observedExecutionName = execution.getAlgorithmExecution().getName();
				Pageable pageable = PageRequest.of(0, execution.getMaxDataPoints(), Sort.by(Sort.Direction.DESC, "endDate"));
				List<AlgorithmExecutionResult> results = algorithmExecutionResultRepository
						.findByDeploymentAndAlgorithmExecution(deploymentName, observedExecutionName, pageable);
				
				if (results.size() >= execution.getMinDataPoints()) {
					ObjectMapper objectMapper = new ObjectMapper();
					String now = java.time.Instant.now().toString();
					logger.info("Triggering Higher order algorithm execution: " + execution.getName());
					HigherOrderAlgorithmExecutionRequest requestObject = new HigherOrderAlgorithmExecutionRequest(execution, results.size(),
							context.getExtendedState().get("lastTrigger".concat(execution.getName()), String.class),
							now);
						try {
						CloudEvent event = CloudEventBuilder.v1().withId(UUID.randomUUID().toString())
								.withType("org.lowcomote.panoptes.higherOrderAlgorithmExecution.trigger")
								.withSource(java.net.URI.create("panoptes.orchestrator"))
								.withData(objectMapper.writeValueAsBytes(requestObject))
								.withSubject(execution.getAlgorithm().getRuntime().getName())
								.build();
					
							sendEvent(event);
						} catch (IOException e) {
							e.printStackTrace();
						}
					context.getExtendedState().getVariables().put("lastTrigger".concat(execution.getName()), now);
				}
			}
		};
	}

	private Action<String, String> buildActionExecutionTrigger(AlgorithmExecution execution) {
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context) {
				try {
					panoptesDSL.ActionExecution actionExecutionToTrigger = null;
					int level = (int) context.getMessageHeader("level");
					String deployment = ((Deployment) execution.eContainer()).getName();
					String rawResult = (String) context.getMessageHeader("rawResult");
					String startDate = (String) context.getMessageHeader("startDate");
					String endDate = (String) context.getMessageHeader("endDate");
					String algorithmExecution = execution.getName();
					for (actionExecutionEntry entry : execution.getActionExecutionMap()) {
						if (entry.getKey() == level) {
							actionExecutionToTrigger = entry.getValue();
							break;
						}
					}
					if (actionExecutionToTrigger != null) {
						logger.info("Triggering action execution: " + actionExecutionToTrigger.getName());
						ActionExecutionRequest requestObject = new ActionExecutionRequest(actionExecutionToTrigger,
								deployment, algorithmExecution, level, rawResult, startDate, endDate);
						ObjectMapper objectMapper = new ObjectMapper();
						CloudEvent event = CloudEventBuilder.v1().withId(UUID.randomUUID().toString())
								.withType("org.lowcomote.panoptes.actionExecution.trigger")
								.withSource(java.net.URI.create("panoptes.orchestrator"))
								.withData(objectMapper.writeValueAsBytes(requestObject))
								.withSubject(actionExecutionToTrigger.getAction().getName()).build();
						sendEvent(event);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	private void sendEvent(CloudEvent event) throws IOException {
		HttpHeaders headers = new HttpHeaders();
		HttpMessageWriter messageWriter = createMessageWriter(headers);
		messageWriter.writeStructured(event, JsonFormat.CONTENT_TYPE);
	}

	private HttpMessageWriter createMessageWriter(HttpHeaders headers) {
		return HttpMessageFactory.createWriter(headers::set, body -> {
			HttpEntity<byte[]> request = new HttpEntity<byte[]>(body, headers);
			restTemplate.postForObject(brokerURL, request, String.class);
		});
	}
}
