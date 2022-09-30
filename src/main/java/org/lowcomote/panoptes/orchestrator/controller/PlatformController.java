package org.lowcomote.panoptes.orchestrator.controller;

import java.util.ArrayList;
import java.util.List;

import org.lowcomote.panoptes.orchestrator.api.BaseAlgorithmExecutionInfo;
import org.lowcomote.panoptes.orchestrator.api.DeploymentResponse;
import org.lowcomote.panoptes.orchestrator.api.ModelResponse;
import org.lowcomote.panoptes.orchestrator.api.SingleBaseAlgorithmExecutionInfo;
import org.lowcomote.panoptes.orchestrator.service.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import panoptesDSL.Deployment;
import panoptesDSL.Model;

@RestController
public class PlatformController {
	@Autowired
	private PlatformService platformService;

	@PostMapping(value = "/api/v1/platform", consumes = "text/plain")
	public void updatePlatform(@RequestBody String platformXMI) throws Exception {
		System.out.println(platformXMI);
		platformService.updatePlatform(platformXMI);
	}

	@GetMapping(value = "/api/v1/deployments/{name}", produces = "application/json")
	public DeploymentResponse getDeployment(@PathVariable String name) {
		Deployment deployment = platformService.getDeployment(name);
		if (deployment == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Deployment not found");
		}
		return new DeploymentResponse(deployment);
	}

	@GetMapping(value = "/api/v1/deployments", produces = "application/json")
	public List<DeploymentResponse> getDeployments() {
		List<Deployment> deployments = platformService.getDeployments();
		List<DeploymentResponse> response = new ArrayList<DeploymentResponse>();
		for (Deployment d : deployments) {
			response.add(new DeploymentResponse(d));
		}
		return response;
	}

	@GetMapping(value = "/api/v1/deployments/{deploymentName}/{executionType}/{executionName}", produces = "application/json")
	public BaseAlgorithmExecutionInfo getSpecificExecutionResults(@PathVariable String deploymentName,
			@PathVariable String executionType, @PathVariable String executionName,
			@RequestParam(required = false) Integer count) {
		if (count == null) {
			count = 1;
		}
		BaseAlgorithmExecutionInfo response = platformService.getSpecificExecutionResults(deploymentName, executionName,
				executionType, count);
		if (response == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}
		return response;
	}
	
	@GetMapping(value = "/api/v1/deployments/{deploymentName}/{executionType}", produces = "application/json")
	public List<SingleBaseAlgorithmExecutionInfo> getSpecificExecutionResults(@PathVariable String deploymentName,
			@PathVariable String executionType,@RequestParam(required = false) Integer count) {
		if (count == null) {
			count = 1;
		}
		List<SingleBaseAlgorithmExecutionInfo> response = platformService.getExecutionResultsByType(deploymentName,
				executionType, count);
		if (response == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}
		return response;
	}
	
	@GetMapping(value = "/api/v1/models", produces = "application/json")
	public List<ModelResponse> getModels() {
		List<Model> models = platformService.getModels();
		List<ModelResponse> response = new ArrayList<ModelResponse>();
		for (Model m : models) {
			response.add(new ModelResponse(m));
		}
		return response;
	}
}
