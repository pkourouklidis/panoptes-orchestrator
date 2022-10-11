package org.lowcomote.panoptes.orchestrator.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lowcomote.panoptes.orchestrator.repository.AlgorithmExecutionResultRepository;
import org.lowcomote.panoptes.orchestrator.repository.StateMachineRepository;
import org.mockito.Mock;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import org.springframework.web.client.RestTemplate;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PlatformServiceTest {
	private PlatformService platformService;
	@Mock
	private AlgorithmExecutionResultRepository algorithmExecutionResultRepositoryrepository;
	private StateMachineRepository stateMachineRepository = new StateMachineRepository();
	@Mock
	private RestTemplate restTemplate;
	
	@BeforeEach
	void initService(){
		platformService = new PlatformService(stateMachineRepository, algorithmExecutionResultRepositoryrepository, restTemplate);
	}
	
	@Test
	void checkPlatformUpdate() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("emptyDeployment.xmi");
        String platformXMI1 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        //Initial Platform parsing
		assertDoesNotThrow(() -> platformService.updatePlatform(platformXMI1));
		assertDoesNotThrow(() -> platformService.getDeployment("d1"));
		assertNotNull(platformService.getDeployment("d1"));
		
		inputStream = classLoader.getResourceAsStream("completeDeployment.xmi");
		String platformXMI2 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		//Platform update
		assertDoesNotThrow(() -> platformService.updatePlatform(platformXMI2));
		assertNotNull(platformService.getDeployment("d2"));
		assertNotNull(stateMachineRepository.getMachine("d2"));
		
		inputStream = classLoader.getResourceAsStream("completeDeploymentRenamed.xmi");
		String platformXMI3 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		//Platform update
		assertDoesNotThrow(() -> platformService.updatePlatform(platformXMI3));
		assertNotNull(platformService.getDeployment("d3"));
		assertNotNull(stateMachineRepository.getMachine("d3"));
		assertNull(platformService.getDeployment("d2"));
		assertNull(stateMachineRepository.getMachine("d2"));
	}
	
	@Test
	void checkStateMachine() throws Exception  {
		//TODO Find a way to check the outgoing http requests for correctness
		ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("completeDeployment.xmi");
        String platformXMI = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        platformService.updatePlatform(platformXMI);
        String triggerGroupHash = String.valueOf( platformService.getDeployment("d2").getTriggerGroups().get(0).hashCode());
       
        Message<String> m1 = MessageBuilder.withPayload("TRIGGER")
				.setHeader("type", "sample").setHeader("count", 1).build();
        Message<String> m2 = MessageBuilder.withPayload("exec1-EXECUTIONRESULT")
				.setHeader("level", 1).setHeader("rawResult", "0.015").build();
		StateMachineTestPlan<String, String> plan = 
				StateMachineTestPlanBuilder.<String, String>builder()
				.defaultAwaitTime(2)
				.stateMachine(stateMachineRepository.getMachine("d2"))
				.step()
					.expectState("IDLE")
					.expectVariable("sample".concat(triggerGroupHash), 0)
					.and()
				.step()
					.sendEvent(m1)
					.expectStates("IDLE")
					.expectVariable("sample".concat(triggerGroupHash), 0)
					.expectExtendedStateChanged(3)
					.expectTransition(1)
					.and()
				.step()
					.sendEvent(m2)
					.expectStates("IDLE")
					.expectTransition(1)
					.and()
				.build();
		plan.test();
	}
}
