package org.lowcomote.panoptes.orchestrator.repository;

import java.util.List;

import org.lowcomote.panoptes.orchestrator.api.AlgorithmExecutionResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlgorithmExecutionResultRepository extends CrudRepository<AlgorithmExecutionResult, String>{
	public List<AlgorithmExecutionResult> findByAlgorithmExecution(String algorithmExecution, Pageable pageable);
	public List<AlgorithmExecutionResult> findByDeploymentAndAlgorithmExecution(String deployment, String algorithmExecution, Pageable pageable);
	public List<AlgorithmExecutionResult> findByDeploymentAndExecutionType(String deployment, String executionType, Pageable pageable);
}
