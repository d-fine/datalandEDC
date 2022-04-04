package org.dataland.edcDummyServer

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@SpringBootTest
@AutoConfigureMockMvc
class DummyEdcControllerTest(
    @Autowired var mockMvc: MockMvc
) {
    @Test
    fun `checks if data can be selected by non-existing id`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/dataland/data/0")
        ).andExpectAll(
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            MockMvcResultMatchers.content().string("")
        )
    }

    @Test
    fun `checks if data can be inserted and retrieved`() {
        val body = "{\"key\":\"value\"}"

        mockMvc.perform(
            MockMvcRequestBuilders.post("/dataland/data")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
        ).andExpectAll(
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON)
        )

        mockMvc.perform(
            MockMvcRequestBuilders.get("/dataland/data/1")
        ).andExpectAll(
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            MockMvcResultMatchers.content().string(body)
        )
    }
}
