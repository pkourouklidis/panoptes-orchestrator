package org.lowcomote.panoptes.orchestrator.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.lowcomote.panoptes.orchestrator.api.ActionExecutionRequest;
import org.lowcomote.panoptes.orchestrator.api.AlgorithmExecutionRequest;
import org.lowcomote.panoptes.orchestrator.api.AlgorithmExecutionResult;
import org.lowcomote.panoptes.orchestrator.api.BaseAlgorithmExecutionInfo;
import org.lowcomote.panoptes.orchestrator.api.SingleBaseAlgorithmExecutionInfo;
import org.lowcomote.panoptes.orchestrator.repository.AlgorithmExecutionResultRepository;
import org.lowcomote.panoptes.orchestrator.repository.StateMachineRepository;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.StateMachineBuilder.Builder;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.jackson.JsonFormat;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.http.HttpMessageFactory;
import io.cloudevents.http.impl.HttpMessageWriter;
import panoptesDSL.AlgorithmExecution;
import panoptesDSL.BaseAlgorithmExecution;
import panoptesDSL.CompositeTrigger;
import panoptesDSL.Deployment;
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

	public PlatformService(StateMachineRepository stateMachineRepository, AlgorithmExecutionResultRepository algorithmExecutionResultRepository) {
		this.stateMachineRepository = stateMachineRepository;
		this.algorithmExecutionResultRepository = algorithmExecutionResultRepository;
		this.resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(PanoptesDSLPackage.eNS_URI, PanoptesDSLPackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
	}
	
	public Deployment getDeployment(String name) {
		if (currentPlatform!=null) {
			for( Deployment d : currentPlatform.getDeployments()) {
				if(d.getName().equals(name)) {
					return d;
				}
			}
		}
		return null;
	}
	
	public List<Deployment> getDeployments() {
		if (currentPlatform!=null) {
			return currentPlatform.getDeployments();
		}
		return new ArrayList<Deployment>();
	}
	
	public List<Model> getModels() {
		if (currentPlatform!=null) {
			return currentPlatform.getMlModels();
		}
		return new ArrayList<Model>();
	}
	
	public BaseAlgorithmExecutionInfo getSpecificExecutionResults(String deploymentName, String algorithmExecutionName, String executionType, Integer count){
		Deployment deployment = getDeployment(deploymentName);
		if (deployment != null) {
			if (executionType.equals("baseAlgorithmExecutions")){
				BaseAlgorithmExecution algorithmExecution = null;
				for (AlgorithmExecution execution : deployment.getAlgorithmexecutions()) {
					if (execution.getName().equals(algorithmExecutionName) && execution.eClass().getName().equals("BaseAlgorithmExecution")) {
						algorithmExecution = (BaseAlgorithmExecution)execution;
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
	
	public BaseAlgorithmExecutionInfo getSpecificBaseAlgorihtmExecutionResults(Deployment deployment, BaseAlgorithmExecution algorithmExecution, Integer count){
		Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "date"));
		List<AlgorithmExecutionResult> results = algorithmExecutionResultRepository.findByDeploymentAndAlgorithmExecution(deployment.getName(), algorithmExecution.getName(), pageable);
		return new BaseAlgorithmExecutionInfo(algorithmExecution, results);
	}
	
	public List<SingleBaseAlgorithmExecutionInfo> getExecutionResultsByType(String deploymentName, String executionType, Integer count) {
		Deployment deployment = getDeployment(deploymentName);
		if (deployment != null) {
			if (executionType.equals("baseAlgorithmExecutions")){
				Pageable pageable = PageRequest.of(0, count, Sort.by(Sort.Direction.DESC, "date"));
				List<AlgorithmExecutionResult> results = algorithmExecutionResultRepository.findByDeploymentAndExecutionType(deploymentName, "baseAlgorithmExecution", pageable);
				List<SingleBaseAlgorithmExecutionInfo> finalResult = new ArrayList<SingleBaseAlgorithmExecutionInfo>();
				for (AlgorithmExecutionResult result : results) {
					BaseAlgorithmExecution executionDefinition = null;
					for (AlgorithmExecution execution : deployment.getAlgorithmexecutions()) {
						if (execution.getName().equals(result.getAlgorithmExecution()) && execution.eClass().getName().equals("BaseAlgorithmExecution")) {
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
	
	public void updatePlatform(String platformXMI) throws Exception {
		Platform newPlatform = parsePlatform(platformXMI);
		stateMachineRepository.clear();
		for (Deployment d : newPlatform.getDeployments()) {
			if (d.getTriggerGroups().size() > 0) {
				StateMachine<String, String> sm = buildStateMachine(d);
				stateMachineRepository.addMachine(d.getName(), sm);
			}
		}
		this.currentPlatform = newPlatform;
	}

	private Platform parsePlatform(String platformXMI) throws IOException {
		Resource resource = this.resourceSet.createResource(URI.createURI("newResource.xmi"));
		resource.load(new ByteArrayInputStream(platformXMI.getBytes()), null);
		return (Platform) resource.getContents().get(0);
	}

	private StateMachine<String, String> buildStateMachine(Deployment deployment) throws Exception {
		Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureStates().withStates().initial("IDLE");
		builder.configureConfiguration().withConfiguration().beanFactory(new StaticListableBeanFactory())
				.machineId(deployment.getName()).autoStartup(true);
		//Add one transition per trigger group that sends algorithm execution requests
		for (TriggerGroup triggerGroup : deployment.getTriggerGroups()) {
			builder.configureTransitions()
				.withInternal()
				.source("IDLE")
				.event("TRIGGER")
				.guard(buildGuard(triggerGroup))
				.action(buildAlgorithmExecutionTrigger(triggerGroup));
		}
		
		//Add one transition per algorithm execution that reads the result the execution and sends an action execution request accordingly 
		for (AlgorithmExecution execution : deployment.getAlgorithmexecutions()) {
			if (execution.eClass().getName().equals("BaseAlgorithmExecution")) {
				builder.configureTransitions()
				.withInternal()
				.source("IDLE")
				.event(execution.getName().concat("-EXECUTIONRESULT"))
				.action(buildActionExecutionTrigger((BaseAlgorithmExecution)execution));
			}
		}
		StateMachine<String, String> sm = builder.build();
		// Initialise individual triggergroup states
		for (TriggerGroup triggerGroup : deployment.getTriggerGroups()) {
			String triggerGroupHash = String.valueOf(triggerGroup.hashCode());
			sm.getExtendedState().getVariables().put("sample".concat(triggerGroupHash), 0);
			sm.getExtendedState().getVariables().put("prediction".concat(triggerGroupHash), 0);
			sm.getExtendedState().getVariables().put("label".concat(triggerGroupHash), 0);
			sm.getExtendedState().getVariables().put("timer".concat(triggerGroupHash), false);
			sm.getExtendedState().getVariables().put("lastTrigger".concat(triggerGroupHash),
				java.time.Instant.now().toString());
		}
		return sm;
	}

	private Guard<String, String> buildGuard(TriggerGroup triggerGroup) {
		final String triggerGroupHash = String.valueOf(triggerGroup.hashCode());
		return new Guard<String, String>() {
			@Override
			synchronized public boolean evaluate(StateContext<String, String> context) {
				int count;
				// increment relevant counter
				switch ((String) context.getMessage().getHeaders().get("type")) {
				case "sample":
					count = (int) context.getMessage().getHeaders().get("count");
					context.getExtendedState().getVariables().put("sample".concat(triggerGroupHash), (int) context
							.getExtendedState().getVariables().get("sample".concat(triggerGroupHash)) + count);
					break;
				case "prediction":
					count = (int) context.getMessage().getHeaders().get("count");
					context.getExtendedState().getVariables().put("prediction".concat(triggerGroupHash),
							(int) context.getExtendedState().getVariables()
									.get("prediction".concat(triggerGroupHash)) + count);
					break;
				case "label":
					count = (int) context.getMessage().getHeaders().get("count");
					context.getExtendedState().getVariables().put("label".concat(triggerGroupHash), (int) context
							.getExtendedState().getVariables().get("label".concat(triggerGroupHash)) + count);
					break;
				case "timer":
					context.getExtendedState().getVariables().put("timer".concat(triggerGroupHash), true);
					break;
				}

				for (CompositeTrigger compositeTrigger : triggerGroup.getCompositeTriggers()) {

					if (compositeTrigger.getTt() != null) {
						if ((boolean) context.getExtendedState().getVariables()
								.get("timer".concat(triggerGroupHash)) == false) {
							return false;
						}
					}

					if (compositeTrigger.getSt() != null) {
						if ((int) context.getExtendedState().getVariables()
								.get("sample".concat(triggerGroupHash)) < compositeTrigger.getSt().getFrequency()) {
							return false;
						}
					}

					if (compositeTrigger.getPt() != null) {
						if ((int) context.getExtendedState().getVariables().get(
								"prediction".concat(triggerGroupHash)) < compositeTrigger.getPt().getFrequency()) {
							return false;
						}
					}

					if (compositeTrigger.getLt() != null) {
						if ((int) context.getExtendedState().getVariables()
								.get("label".concat(triggerGroupHash)) < compositeTrigger.getLt().getFrequency()) {
							return false;
						}
					}
				}

				// reset counters
				context.getExtendedState().getVariables().put("sample".concat(triggerGroupHash), 0);
				context.getExtendedState().getVariables().put("prediction".concat(triggerGroupHash), 0);
				context.getExtendedState().getVariables().put("label".concat(triggerGroupHash), 0);
				context.getExtendedState().getVariables().put("timer".concat(triggerGroupHash), false);
				return true;
			}
		};
	}

	private Action<String, String> buildAlgorithmExecutionTrigger(TriggerGroup triggerGroup) {
		final String triggerGroupHash = String.valueOf(triggerGroup.hashCode());
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context) {
				ObjectMapper objectMapper = new ObjectMapper();
				for (BaseAlgorithmExecution baseAlgorithmExecution : triggerGroup.getTargets()) {
					try {
						AlgorithmExecutionRequest requestObject = new AlgorithmExecutionRequest(baseAlgorithmExecution,
								context.getExtendedState().get("lastTrigger".concat(triggerGroupHash), String.class));
						CloudEvent event = CloudEventBuilder.v1()
								.withType("org.lowcomote.panoptes.baseAlgorithmExecution.trigger")
								.withSource(java.net.URI.create("panoptes.orchestrator"))
								.withData(objectMapper.writeValueAsBytes(requestObject))
								.withSubject(baseAlgorithmExecution.getAlgorithm().getRuntime().getName()).build();
						URL url = new URL("http://broker-ingress.knative-eventing.svc.cluster.local/panoptes/default");
						HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
						httpUrlConnection.setRequestMethod("POST");
						httpUrlConnection.setDoOutput(true);
						httpUrlConnection.setDoInput(false);
						HttpMessageWriter messageWriter = createMessageWriter(httpUrlConnection);
						messageWriter.writeStructured(event, JsonFormat.CONTENT_TYPE);
					} catch (IOException e) {
						e.printStackTrace();
					}
					context.getExtendedState().getVariables().put("lastTrigger".concat(triggerGroupHash),
							java.time.Instant.now().toString());
				}
			}
		};
	}

	private Action<String, String> buildActionExecutionTrigger(BaseAlgorithmExecution execution) {
		return new Action<String, String>() {
			@Override
			public void execute(StateContext<String, String> context) {
				panoptesDSL.ActionExecution actionExecutionToTrigger=null;
				int level =  (int)context.getMessageHeader("level");
				String deployment = ((Deployment)execution.eContainer()).getName();
				String rawResult = (String)context.getMessageHeader("rawResult");
				String algorithmExecution = execution.getName();
				for (actionExecutionEntry entry : execution.getActionExecutionMap()) {
					if (entry.getKey()==level) {
						actionExecutionToTrigger = entry.getValue();
						break;
					}
				}
				if (actionExecutionToTrigger!=null) {
					ActionExecutionRequest requestObject = new ActionExecutionRequest(actionExecutionToTrigger, deployment, algorithmExecution, level, rawResult );
					ObjectMapper objectMapper = new ObjectMapper();
					try {
						CloudEvent event = CloudEventBuilder.v1()
								.withType("org.lowcomote.panoptes.actionExecution.trigger")
								.withSource(java.net.URI.create("panoptes.orchestrator"))
								.withData(objectMapper.writeValueAsBytes(requestObject))
								.withSubject(actionExecutionToTrigger.getAction().getName()).build();
						URL url = new URL("http://broker-ingress.knative-eventing.svc.cluster.local/panoptes/default");
						HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
						httpUrlConnection.setRequestMethod("POST");
						httpUrlConnection.setDoOutput(true);
						httpUrlConnection.setDoInput(false);
						HttpMessageWriter messageWriter = createMessageWriter(httpUrlConnection);
						messageWriter.writeStructured(event, JsonFormat.CONTENT_TYPE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
	}
	
	private HttpMessageWriter createMessageWriter(HttpURLConnection httpUrlConnection) {
		return HttpMessageFactory.createWriter(httpUrlConnection::setRequestProperty, body -> {
			try {
				if (body != null) {
					httpUrlConnection.setRequestProperty("content-length", String.valueOf(body.length));
					try (OutputStream outputStream = httpUrlConnection.getOutputStream()) {
						outputStream.write(body);
					}
				} else {
					httpUrlConnection.setRequestProperty("content-length", "0");
				}
			} catch (IOException t) {
				throw new UncheckedIOException(t);
			}
		});
	}
}
