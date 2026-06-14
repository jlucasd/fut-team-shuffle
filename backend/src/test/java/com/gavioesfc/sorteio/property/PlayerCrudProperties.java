package com.gavioesfc.sorteio.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavioesfc.sorteio.dto.PlayerCreateRequest;
import com.gavioesfc.sorteio.dto.PlayerResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Feature: gavioes-fc-sorteio, Property 1: Player creation round-trip
// Feature: gavioes-fc-sorteio, Property 2: Invalid player data rejection
// Feature: gavioes-fc-sorteio, Property 3: Player filtering correctness
// Feature: gavioes-fc-sorteio, Property 4: Player update round-trip
// Feature: gavioes-fc-sorteio, Property 5: Player deletion removes record
// Feature: gavioes-fc-sorteio, Property 6: Status toggle is involution
@JqwikSpringSupport
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PlayerCrudProperties {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String token() {
        return "Bearer " + jwtUtil.generateToken("admin");
    }

    // --- Property 1: Player creation round-trip ---
    // **Validates: Requirements 1.1, 1.4**

    @Property(tries = 100)
    void playerCreationRoundTrip(@ForAll("validPlayerProvider") PlayerCreateRequest request) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/jogadores")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        PlayerResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), PlayerResponse.class);

        assertThat(created.id()).isNotNull();

        MvcResult getResult = mockMvc.perform(get("/api/jogadores/" + created.id())
                        .header("Authorization", token()))
                .andExpect(status().isOk())
                .andReturn();

        PlayerResponse retrieved = objectMapper.readValue(
                getResult.getResponse().getContentAsString(), PlayerResponse.class);

        assertThat(retrieved.nome()).isEqualTo(request.nome());
        assertThat(retrieved.posicao()).isEqualTo(request.posicao());
        assertThat(retrieved.nivel()).isEqualTo(request.nivel());
        assertThat(retrieved.ativo()).isEqualTo(request.ativo());
    }

    @Provide
    Arbitrary<PlayerCreateRequest> validPlayerProvider() {
        return TestArbitraries.validPlayers();
    }

    // --- Property 2: Invalid player data rejection ---
    // **Validates: Requirements 1.2, 3.3**

    @Property(tries = 100)
    void invalidPlayerDataRejection(@ForAll("invalidPlayerProvider") PlayerCreateRequest request) throws Exception {
        mockMvc.perform(post("/api/jogadores")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Provide
    Arbitrary<PlayerCreateRequest> invalidPlayerProvider() {
        return TestArbitraries.invalidPlayers();
    }

    // --- Property 3: Player filtering correctness ---
    // **Validates: Requirements 2.2, 2.3, 2.4**

    @Property(tries = 100)
    void playerFilteringCorrectness(
            @ForAll("positionFilter") Position filterPosition,
            @ForAll("booleanFilter") Boolean filterAtivo) throws Exception {

        Position[] positions = Position.values();
        for (int i = 0; i < 4; i++) {
            PlayerCreateRequest req = new PlayerCreateRequest(
                    "Filter" + i + System.nanoTime() % 10000,
                    positions[i % positions.length],
                    (i % 5) + 1,
                    i % 2 == 0
            );
            mockMvc.perform(post("/api/jogadores")
                            .header("Authorization", token())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated());
        }

        MvcResult result = mockMvc.perform(get("/api/jogadores")
                        .header("Authorization", token())
                        .param("posicao", filterPosition.name())
                        .param("ativo", filterAtivo.toString())
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        var pageResponse = objectMapper.readTree(json);
        var content = pageResponse.get("content");

        for (var playerNode : content) {
            String posicao = playerNode.get("posicao").asText();
            boolean ativo = playerNode.get("ativo").asBoolean();
            assertThat(posicao).isEqualTo(filterPosition.name());
            assertThat(ativo).isEqualTo(filterAtivo);
        }
    }

    @Provide
    Arbitrary<Position> positionFilter() {
        return Arbitraries.of(Position.values());
    }

    @Provide
    Arbitrary<Boolean> booleanFilter() {
        return Arbitraries.of(true, false);
    }

    // --- Property 4: Player update round-trip ---
    // **Validates: Requirements 3.1**

    @Property(tries = 100)
    void playerUpdateRoundTrip(
            @ForAll("validPlayerProvider") PlayerCreateRequest original,
            @ForAll("validPlayerProvider") PlayerCreateRequest updateData) throws Exception {

        MvcResult createResult = mockMvc.perform(post("/api/jogadores")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(original)))
                .andExpect(status().isCreated())
                .andReturn();

        PlayerResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), PlayerResponse.class);

        mockMvc.perform(put("/api/jogadores/" + created.id())
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk());

        MvcResult getResult = mockMvc.perform(get("/api/jogadores/" + created.id())
                        .header("Authorization", token()))
                .andExpect(status().isOk())
                .andReturn();

        PlayerResponse retrieved = objectMapper.readValue(
                getResult.getResponse().getContentAsString(), PlayerResponse.class);

        assertThat(retrieved.nome()).isEqualTo(updateData.nome());
        assertThat(retrieved.posicao()).isEqualTo(updateData.posicao());
        assertThat(retrieved.nivel()).isEqualTo(updateData.nivel());
        assertThat(retrieved.ativo()).isEqualTo(updateData.ativo());
    }

    // --- Property 5: Player deletion removes record ---
    // **Validates: Requirements 4.1**

    @Property(tries = 100)
    void playerDeletionRemovesRecord(@ForAll("validPlayerProvider") PlayerCreateRequest request) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/jogadores")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        PlayerResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), PlayerResponse.class);

        mockMvc.perform(delete("/api/jogadores/" + created.id())
                        .header("Authorization", token()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/jogadores/" + created.id())
                        .header("Authorization", token()))
                .andExpect(status().isNotFound());
    }

    // --- Property 6: Status toggle is involution ---
    // **Validates: Requirements 5.1**

    @Property(tries = 100)
    void statusToggleIsInvolution(@ForAll("validPlayerAnyStatusProvider") PlayerCreateRequest request) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/jogadores")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        PlayerResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), PlayerResponse.class);

        Boolean originalStatus = created.ativo();

        mockMvc.perform(patch("/api/jogadores/" + created.id() + "/status")
                        .header("Authorization", token()))
                .andExpect(status().isOk());

        MvcResult secondToggle = mockMvc.perform(patch("/api/jogadores/" + created.id() + "/status")
                        .header("Authorization", token()))
                .andExpect(status().isOk())
                .andReturn();

        PlayerResponse afterDoubleToggle = objectMapper.readValue(
                secondToggle.getResponse().getContentAsString(), PlayerResponse.class);

        assertThat(afterDoubleToggle.ativo()).isEqualTo(originalStatus);
    }

    @Provide
    Arbitrary<PlayerCreateRequest> validPlayerAnyStatusProvider() {
        return TestArbitraries.validPlayersAnyStatus();
    }
}
