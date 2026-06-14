package com.gavioesfc.sorteio.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavioesfc.sorteio.dto.*;
import com.gavioesfc.sorteio.model.enums.Position;
import com.gavioesfc.sorteio.security.JwtUtil;
import net.jqwik.api.*;
import net.jqwik.spring.JqwikSpringSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Feature: gavioes-fc-sorteio, Property 12: Draw retrieval round-trip
// Feature: gavioes-fc-sorteio, Property 13: Manual edit recalculation correctness
@JqwikSpringSupport
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DrawIntegrationProperties {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String token() {
        return "Bearer " + jwtUtil.generateToken("admin");
    }

    // --- Property 12: Draw retrieval round-trip ---
    // **Validates: Requirements 7.2**

    @Property(tries = 100)
    void drawRetrievalRoundTrip(@ForAll("playerCount") int numPlayers) throws Exception {
        List<Long> playerIds = createPlayers(numPlayers);

        DrawRequest drawRequest = new DrawRequest(playerIds);
        MvcResult drawResult = mockMvc.perform(post("/api/sorteio")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drawRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        DrawResponse created = objectMapper.readValue(
                drawResult.getResponse().getContentAsString(), DrawResponse.class);

        MvcResult getResult = mockMvc.perform(get("/api/sorteio/" + created.id())
                        .header("Authorization", token()))
                .andExpect(status().isOk())
                .andReturn();

        DrawResponse retrieved = objectMapper.readValue(
                getResult.getResponse().getContentAsString(), DrawResponse.class);

        assertThat(retrieved.id()).isEqualTo(created.id());
        assertThat(retrieved.mediaAmarelo()).isEqualTo(created.mediaAmarelo());
        assertThat(retrieved.mediaPreto()).isEqualTo(created.mediaPreto());
        assertThat(retrieved.equilibrado()).isEqualTo(created.equilibrado());

        assertThat(retrieved.timeAmarelo()).hasSameSizeAs(created.timeAmarelo());
        assertThat(retrieved.timePreto()).hasSameSizeAs(created.timePreto());

        List<Long> createdAmareloIds = created.timeAmarelo().stream().map(PlayerResponse::id).sorted().toList();
        List<Long> retrievedAmareloIds = retrieved.timeAmarelo().stream().map(PlayerResponse::id).sorted().toList();
        assertThat(retrievedAmareloIds).isEqualTo(createdAmareloIds);

        List<Long> createdPretoIds = created.timePreto().stream().map(PlayerResponse::id).sorted().toList();
        List<Long> retrievedPretoIds = retrieved.timePreto().stream().map(PlayerResponse::id).sorted().toList();
        assertThat(retrievedPretoIds).isEqualTo(createdPretoIds);
    }

    @Provide
    Arbitrary<Integer> playerCount() {
        return Arbitraries.integers().between(2, 10);
    }

    // --- Property 13: Manual edit recalculation correctness ---
    // **Validates: Requirements 8.2, 8.3**

    @Property(tries = 100)
    void manualEditRecalculationCorrectness(@ForAll("evenPlayerCount") int numPlayers) throws Exception {
        List<Long> playerIds = createPlayers(numPlayers);

        DrawRequest drawRequest = new DrawRequest(playerIds);
        MvcResult drawResult = mockMvc.perform(post("/api/sorteio")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(drawRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        DrawResponse created = objectMapper.readValue(
                drawResult.getResponse().getContentAsString(), DrawResponse.class);

        // Swap teams
        List<Long> newAmareloIds = created.timePreto().stream().map(PlayerResponse::id).toList();
        List<Long> newPretoIds = created.timeAmarelo().stream().map(PlayerResponse::id).toList();

        TeamEditRequest editRequest = new TeamEditRequest(newAmareloIds, newPretoIds, null);

        MvcResult editResult = mockMvc.perform(put("/api/sorteio/" + created.id() + "/times")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest)))
                .andExpect(status().isOk())
                .andReturn();

        DrawResponse edited = objectMapper.readValue(
                editResult.getResponse().getContentAsString(), DrawResponse.class);

        // Verify averages are recalculated correctly
        double expectedAvgAmarelo = edited.timeAmarelo().stream()
                .mapToInt(PlayerResponse::nivel)
                .average()
                .orElse(0.0);
        double expectedAvgPreto = edited.timePreto().stream()
                .mapToInt(PlayerResponse::nivel)
                .average()
                .orElse(0.0);

        assertThat(edited.mediaAmarelo()).isCloseTo(expectedAvgAmarelo, org.assertj.core.data.Offset.offset(0.001));
        assertThat(edited.mediaPreto()).isCloseTo(expectedAvgPreto, org.assertj.core.data.Offset.offset(0.001));

        // Verify equilibrado flag consistency
        double diff = Math.abs(edited.mediaAmarelo() - edited.mediaPreto());
        if (diff <= 0.5) {
            assertThat(edited.equilibrado()).isTrue();
        } else {
            assertThat(edited.equilibrado()).isFalse();
        }
    }

    @Provide
    Arbitrary<Integer> evenPlayerCount() {
        return Arbitraries.integers().between(2, 5).map(n -> n * 2);
    }

    // --- Helper methods ---

    private List<Long> createPlayers(int count) throws Exception {
        List<Long> ids = new ArrayList<>();
        Position[] positions = Position.values();

        for (int i = 0; i < count; i++) {
            PlayerCreateRequest req = new PlayerCreateRequest(
                    "DrawP" + i + "_" + System.nanoTime() % 10000,
                    positions[i % positions.length],
                    (i % 5) + 1,
                    true
            );

            MvcResult result = mockMvc.perform(post("/api/jogadores")
                            .header("Authorization", token())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andReturn();

            PlayerResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), PlayerResponse.class);
            ids.add(response.id());
        }

        return ids;
    }
}
