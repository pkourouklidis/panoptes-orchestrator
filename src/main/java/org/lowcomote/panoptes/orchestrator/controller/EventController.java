package org.lowcomote.panoptes.orchestrator.controller;

import org.lowcomote.panoptes.orchestrator.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.cloudevents.CloudEvent;

@RestController
public class EventController {
	@Autowired
	private EventService eventService;
	
	@PostMapping(value = "/api/v1/events")
	void ingestEvent(@RequestBody CloudEvent event) throws Exception {
		eventService.ingestEvent(event);
	}

}
