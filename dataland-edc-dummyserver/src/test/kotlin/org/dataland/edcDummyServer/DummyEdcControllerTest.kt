package org.dataland.edcDummyServer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.RequestBuilder
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class DummyEdcControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper
) {

    private fun performWithBasicResultsChecks(requestBuilder: RequestBuilder): ResultActions {
        return mockMvc.perform(requestBuilder).andExpectAll(
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
        )
    }

    @Test
    fun `check if an empty string is returned by get request when the data id does not exist`() {
        performWithBasicResultsChecks(
            MockMvcRequestBuilders.get("/dataland/data/0:0")
        ).andExpect(
            MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(mapOf("dataId" to "")))
        )
    }

    @Test
    fun `check if the selected data is the same as the inserted data`() {
        val body = "{\"key\":\"value\"}"
        val request = performWithBasicResultsChecks(
            MockMvcRequestBuilders.post("/dataland/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andReturn()

        val result: Map<String, String> = objectMapper.readValue(
            request.response.contentAsString,
            object : TypeReference<Map<String, String>>() {}
        )

        val dataId = result.get("dataId")!!
        assertTrue(dataId.contains(":"))

        performWithBasicResultsChecks(
            MockMvcRequestBuilders.get("/dataland/data/$dataId")
        ).andExpect(
            MockMvcResultMatchers.content().string(objectMapper.writeValueAsString(mapOf("dataId" to body)))
        )
    }
}
