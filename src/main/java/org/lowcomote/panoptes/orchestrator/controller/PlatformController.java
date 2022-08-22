package org.lowcomote.panoptes.orchestrator.controller;

import java.io.IOException;

import org.lowcomote.panoptes.orchestrator.api.DeploymentResponse;
import org.lowcomote.panoptes.orchestrator.service.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import panoptesDSL.Deployment;

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
			throw new ResponseStatusException(
					  HttpStatus.NOT_FOUND,"Deployment not found"
					);
		}
		return new DeploymentResponse(deployment);
	}
}
