package org.lowcomote.panoptes.orchestrator.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.lowcomote.panoptes.orchestrator.api.AlgorithmExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
public class AlgorirhmExecutionResultRepositoryTest {

	@Autowired
	private AlgorithmExecutionResultRepository repository;
	
	@Test
	void saveThenFindByDeploymentAndAlgorithmExecution() {
		AlgorithmExecutionResult result = new AlgorithmExecutionResult();
		result.setDeployment("testDeployment");
		result.setAlgorithmExecution("testAlgorithmExecution");
		result.setDate(Date.from(java.time.Instant.now()));
		repository.save(result);
		Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "date"));
		List<AlgorithmExecutionResult> results = repository.findByDeploymentAndAlgorithmExecution("testDeployment", "testAlgorithmExecution", pageable);
		assertTrue(results.size() == 1);
		assertTrue(results.get(0).getAlgorithmExecution().equals("testAlgorithmExecution"));
	}
	
	@Test
	void saveThenFindByDeploymentAndExecutionType() {
		AlgorithmExecutionResult result = new AlgorithmExecutionResult();
		result.setDeployment("testDeployment");
		result.setAlgorithmExecution("testAlgorithmExecution");
		result.setExecutionType("baseAlgorithmExecution");
		result.setDate(Date.from(java.time.Instant.now()));
		repository.save(result);
		Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "date"));
		List<AlgorithmExecutionResult> results = repository.findByDeploymentAndExecutionType("testDeployment", "baseAlgorithmExecution", pageable);
		assertTrue(results.size() == 1);
		assertTrue(results.get(0).getAlgorithmExecution().equals("testAlgorithmExecution"));
	}
	
	@Test
	void testOrder() throws ParseException {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		AlgorithmExecutionResult oldResult = new AlgorithmExecutionResult();
		oldResult.setLevel(0);
		oldResult.setDeployment("testDeployment");
		oldResult.setAlgorithmExecution("testAlgorithmExecution");
		oldResult.setDate(df.parse("2022-01-01T00:00:00"));
		repository.save(oldResult);
		
		AlgorithmExecutionResult newResult = new AlgorithmExecutionResult();
		newResult.setLevel(1);
		newResult.setDeployment("testDeployment");
		newResult.setAlgorithmExecution("testAlgorithmExecution");
		newResult.setDate(df.parse("2022-01-02T00:00:00"));
		repository.save(newResult);
		
		Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "date"));
		List<AlgorithmExecutionResult> results = repository.findByDeploymentAndAlgorithmExecution("testDeployment", "testAlgorithmExecution", pageable);
		assertTrue(results.size() == 1);
		assertTrue(results.get(0).getLevel() == 1);//newest result retrieved
		
		pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "date"));
		results = repository.findByDeploymentAndAlgorithmExecution("testDeployment", "testAlgorithmExecution", pageable);
		assertTrue(results.size() == 2);
		assertTrue(results.get(0).getLevel() == 1);//newest result retrieved first
		assertTrue(results.get(1).getLevel() == 0);//oldest result retrieved second
	}
}
