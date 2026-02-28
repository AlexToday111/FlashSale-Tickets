package com.ticketing.inventory.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Test;
import java.util.UUID;

@SpringBootTest
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testCreateReservation() throws Exception {
        String jsonRequest = "{\"userId\":\"" + UUID.randomUUID() + "\", \"eventId\":\"" + UUID.randomUUID() + "\", \"seats\":[\""+ UUID.randomUUID() + "\"]}";

        mockMvc.perform(post("/reservations")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("HELD"));
    }

    @Test
    public void testCancelReservation() throws Exception {
        UUID reservationId = UUID.randomUUID();

        mockMvc.perform(post("/reservations/" + reservationId + "/cancel"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetReservationDetails() throws Exception {
        UUID reservationId = UUID.randomUUID();

        mockMvc.perform(get("/reservations/" + reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservationId").value(reservationId.toString()))
                .andExpect(jsonPath("$.status").value("HELD"));
    }
}
