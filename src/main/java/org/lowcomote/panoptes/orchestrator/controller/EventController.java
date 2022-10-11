package org.lowcomote.panoptes.orchestrator.controller;

import java.nio.charset.StandardCharsets;

import org.lowcomote.panoptes.orchestrator.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.cloudevents.CloudEvent;

@RestController
public class EventController {
	@Autowired
	private EventService eventService;
	private Logger logger = LoggerFactory.getLogger(EventController.class);
	
	@PostMapping(value = "/api/v1/events")
	void ingestEvent(@RequestBody CloudEvent event) throws Exception {
		logger.info("Received Cloud Event of type: " + event.getType() + " with data: " + new String(event.getData().toBytes(), StandardCharsets.UTF_8));
		eventService.ingestEvent(event);
	}

}
