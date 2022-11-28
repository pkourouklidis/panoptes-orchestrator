package org.lowcomote.panoptes.orchestrator.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lowcomote.panoptes.orchestrator.api.AlgorithmExecutionResult;
import org.lowcomote.panoptes.orchestrator.repository.AlgorithmExecutionResultRepository;
import org.lowcomote.panoptes.orchestrator.repository.StateMachineRepository;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.test.StateMachineTestPlan;
import org.springframework.statemachine.test.StateMachineTestPlanBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WireMockTest(proxyMode = true)
@DataJpaTest
public class PlatformServiceTest {
	private PlatformService platformService;
	private StateMachineRepository stateMachineRepository;
	@MockBean
	private AlgorithmExecutionResultRepository algorithmExecutionResultRepository;

	@BeforeEach
	void initService() {
		stateMachineRepository = new StateMachineRepository();
		platformService = new PlatformService(stateMachineRepository, algorithmExecutionResultRepository);
	}

	@Test
	void checkPlatformUpdate() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("emptyDeployment.xmi");
		String platformXMI1 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		// Initial Platform parsing
		stubFor(post("/panoptes/default").willReturn(ok()));
		assertDoesNotThrow(() -> platformService.updatePlatform(platformXMI1));
		assertDoesNotThrow(() -> platformService.getDeployment("d1"));
		assertNotNull(platformService.getDeployment("d1"));

		inputStream = classLoader.getResourceAsStream("completeDeployment.xmi");
		String platformXMI2 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		// Platform update
		assertDoesNotThrow(() -> platformService.updatePlatform(platformXMI2));
		assertNotNull(platformService.getDeployment("d2"));
		assertNotNull(stateMachineRepository.getMachine("d2"));

		inputStream = classLoader.getResourceAsStream("completeDeploymentRenamed.xmi");
		String platformXMI3 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		// Platform update
		assertDoesNotThrow(() -> platformService.updatePlatform(platformXMI3));
		assertNotNull(platformService.getDeployment("d3"));
		assertNotNull(stateMachineRepository.getMachine("d3"));
		assertNull(platformService.getDeployment("d2"));
		assertNull(stateMachineRepository.getMachine("d2"));
	}

	@Test
	void checkStateMachine() throws Exception {
		// TODO Find a way to check the outgoing http requests for correctness
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("completeDeployment.xmi");
		String platformXMI = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		stubFor(post("/panoptes/default").willReturn(ok()));
		platformService.updatePlatform(platformXMI);
		String triggerGroupName = platformService.getDeployment("d2").getTriggerGroups().get(0).getName();

		

		Message<String> m1 = MessageBuilder.withPayload("TRIGGER").setHeader("type", "sample").setHeader("count", 1)
				.build();
		Message<String> m2 = MessageBuilder.withPayload("exec1-EXECUTIONRESULT").setHeader("level", 1)
				.setHeader("rawResult", "0.015").build();
		StateMachineTestPlan<String, String> plan = StateMachineTestPlanBuilder.<String, String>builder()
				.defaultAwaitTime(2).
				stateMachine(stateMachineRepository.getMachine("d2"))
				.step()
					.expectState("IDLE")
					.expectVariable("sample".concat(triggerGroupName), 0)
					.and()
				.step()
					.sendEvent(m1)
					.expectStates("IDLE")
					.expectVariable("sample".concat(triggerGroupName), 0)
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

	@Test
	void checkTriggerStateUpdate() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("completeDeployment2.xmi");
		String platformXMI = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		stubFor(post("/panoptes/default").willReturn(ok()));
		platformService.updatePlatform(platformXMI);
		verify(1, postRequestedFor(urlEqualTo("/panoptes/default")));
		String triggerGroupName = "t1";

		Message<String> m1 = MessageBuilder.withPayload("TRIGGER").setHeader("type", "sample").setHeader("count", 1)
				.build();
		StateMachineTestPlan<String, String> plan = StateMachineTestPlanBuilder.<String, String>builder()
				.defaultAwaitTime(2)
				.stateMachine(stateMachineRepository.getMachine("callcenter"))
				.step()
					.expectState("IDLE")
					.expectVariable("sample".concat(triggerGroupName), 0)
					.and()
				.step()
					.sendEvent(m1)
					.expectStates("IDLE")
					.expectVariable("sample".concat(triggerGroupName), 1)
					.expectExtendedStateChanged(1)
					.expectTransition(0)
					.and()
				.build();
		plan.test();
		
		platformService.updatePlatform(platformXMI);
		verify(2, postRequestedFor(urlEqualTo("/panoptes/default")));
		StateMachineTestPlan<String, String> plan2 = StateMachineTestPlanBuilder.<String, String>builder()
				.defaultAwaitTime(2)
				.stateMachine(stateMachineRepository.getMachine("callcenter"))
				.step()
					.expectState("IDLE")
					.expectVariable("sample".concat(triggerGroupName), 1)
					.and()
				.step()
					.sendEvent(m1)
					.expectStates("IDLE")
					.expectVariable("sample".concat(triggerGroupName), 0)
					.expectExtendedStateChanged(3)
					.expectTransition(1)
					.and()
				.build();
		plan2.test();
		verify(3, postRequestedFor(urlEqualTo("/panoptes/default")));
	}
	
	@Test
	void checkHoExecutionTrigger() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("completeDeployment3.xmi");
		String platformXMI = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		stubFor(post("/panoptes/default").willReturn(ok()));
		platformService.updatePlatform(platformXMI);
		verify(2, postRequestedFor(urlEqualTo("/panoptes/default")));
		List<AlgorithmExecutionResult> resultList = new ArrayList<AlgorithmExecutionResult>();
		resultList.add(new AlgorithmExecutionResult());
		when(algorithmExecutionResultRepository.findByDeploymentAndAlgorithmExecution(anyString(), anyString(), any())).thenReturn(resultList);
		
		Message<String> m1 = MessageBuilder.withPayload("exec1".concat("-EXECUTIONRESULT-NOTIFY"))
			.setHeader("level", 1)
			.setHeader("rawResult", "0.01")
			.setHeader("startDate", "blah")
			.setHeader("endDate", "blah")
			.build();
		StateMachineTestPlan<String, String> plan = StateMachineTestPlanBuilder.<String, String>builder()
				.defaultAwaitTime(2)
				.stateMachine(stateMachineRepository.getMachine("callcenter3"))
				.step()
					.sendEvent(m1)
					.expectStates("IDLE")
					.expectTransition(1)
					.and()
				.build();
		plan.test();
		verify(2, postRequestedFor(urlEqualTo("/panoptes/default")));
		
		resultList.add(new AlgorithmExecutionResult());
		StateMachineTestPlan<String, String> plan2 = StateMachineTestPlanBuilder.<String, String>builder()
				.defaultAwaitTime(2)
				.stateMachine(stateMachineRepository.getMachine("callcenter3"))
				.step()
					.sendEvent(m1)
					.expectStates("IDLE")
					.and()
				.build();
		plan2.test();
		verify(3, postRequestedFor(urlEqualTo("/panoptes/default")));
	}
}
