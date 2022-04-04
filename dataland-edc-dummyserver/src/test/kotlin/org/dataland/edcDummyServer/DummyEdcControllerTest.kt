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
    fun `check if an empty string is by get request returned when the data id does not exist`() {
        mockMvc.perform(
            MockMvcRequestBuilders.get("/dataland/data/0")
        ).andExpectAll(
            MockMvcResultMatchers.status().isOk,
            MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON),
            MockMvcResultMatchers.content().string("")
        )
    }

    @Test
    fun `check if the selected data is the same as the inserted data`() {
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

    @Test
    fun `abcd`() {

    }

    @Test
    fun `check abcd`() {

    }

    @Test
    fun abcdef() {

    }
}
