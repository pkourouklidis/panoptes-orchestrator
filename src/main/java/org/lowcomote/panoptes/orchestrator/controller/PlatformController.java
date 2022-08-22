package org.lowcomote.panoptes.orchestrator.controller;

import java.io.IOException;

import org.lowcomote.panoptes.orchestrator.service.PlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlatformController {
	@Autowired
	private PlatformService platformService;

	@PostMapping(value = "/api/v1/platform", consumes = "text/plain")
	void updatePlatform(@RequestBody String platformXMI) throws Exception {
		System.out.println(platformXMI);
		platformService.updatePlatform(platformXMI);
	}
}
