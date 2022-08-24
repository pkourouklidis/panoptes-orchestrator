package org.lowcomote.panoptes.orchestrator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.lowcomote.panoptes.orchestrator.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EventController.class)
public class EventControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private EventService eventService;
	
	@Test
	public void checkCloudEventDeserialization() throws Exception {
		this.mockMvc.perform(post("/api/v1/events")
			.content("{"
					+ "\"specversion\" : \"1.0\","
					+ "\"id\" : \"random\","
					+ "\"type\" : \"com.example.someevent\","
					+ "\"source\" : \"/mycontext\","
					+ "\"datacontenttype\" : \"application/json\","
					+ "\"data\" : {\r\n"
					+ "        \"appinfoA\" : \"abc\",\r\n"
					+ "        \"appinfoB\" : 123,\r\n"
					+ "        \"appinfoC\" : true\r\n"
					+ "    }"
					+ "}")
			.header("Content-Type", "application/cloudevents+json; charset=UTF-8"))
			.andExpect(status().isOk());
	}
}
