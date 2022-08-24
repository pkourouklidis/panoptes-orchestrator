package org.lowcomote.panoptes.orchestrator.repository;

import java.util.List;

import org.lowcomote.panoptes.orchestrator.api.AlgorithmExecutionResult;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlgorithmExecutionResultRepository extends CrudRepository<AlgorithmExecutionResult, String>{
	public List<AlgorithmExecutionResult> findByDeploymentAndAlgorithmExecution(String deployment, String algorihtmExecution);
}
