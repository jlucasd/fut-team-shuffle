package com.gavioesfc.sorteio.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gavioesfc.sorteio.config.EquipeBackfill;
import com.gavioesfc.sorteio.dto.DrawRequest;
import com.gavioesfc.sorteio.dto.PlayerCreateRequest;
import com.gavioesfc.sorteio.model.entity.Draw;
import com.gavioesfc.sorteio.model.entity.Player;
import com.gavioesfc.sorteio.model.enums.Position;
import com.gavioesfc.sorteio.repository.DrawRepository;
import com.gavioesfc.sorteio.repository.PlayerRepository;
import com.gavioesfc.sorteio.security.JwtUtil;
import com.gavioesfc.sorteio.service.EquipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MultiEquipeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private DrawRepository drawRepository;

    @Autowired
    private EquipeBackfill equipeBackfill;

    @Autowired
    private EquipeService equipeService;

    private String token() {
        return "Bearer " + jwtUtil.generateToken("admin");
    }

    private long createEquipe(String nome) throws Exception {
        String body = mockMvc.perform(post("/api/equipes")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"" + nome + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    private long createPlayer(String nome, Long equipeId) throws Exception {
        PlayerCreateRequest req = new PlayerCreateRequest(nome, Position.MEIO, 3, true, null, equipeId);
        String body = mockMvc.perform(post("/api/jogadores")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    void legacyRowsWithoutEquipeAreMigratedToTheDefaultEquipe() {
        Player legacyPlayer = playerRepository.save(Player.builder()
                .nome("Legado").posicao(Position.ZAGUEIRO).nivel(2).ativo(true).build());
        Draw legacyDraw = drawRepository.save(Draw.builder().mediaAmarelo(2.0).mediaPreto(2.0).equilibrado(true).build());

        equipeBackfill.run(null);
        equipeBackfill.run(null); // idempotent

        Long defaultId = equipeService.getOrCreateDefault().getId();
        assertThat(playerRepository.findById(legacyPlayer.getId()).orElseThrow().getEquipe().getId()).isEqualTo(defaultId);
        assertThat(drawRepository.findById(legacyDraw.getId()).orElseThrow().getEquipe().getId()).isEqualTo(defaultId);
    }

    @Test
    void playersAndHistoryAreScopedByEquipe() throws Exception {
        long veteranos = createEquipe("Veteranos");
        long p1 = createPlayer("Vet A", veteranos);
        long p2 = createPlayer("Vet B", veteranos);
        createPlayer("Sem time informado", null);

        String list = mockMvc.perform(get("/api/jogadores")
                        .header("Authorization", token())
                        .param("equipeId", String.valueOf(veteranos))
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode content = objectMapper.readTree(list).get("content");
        assertThat(content).hasSize(2);
        content.forEach(p -> assertThat(p.get("equipeNome").asText()).isEqualTo("Veteranos"));

        mockMvc.perform(post("/api/sorteio")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DrawRequest(List.of(p1, p2), veteranos))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.equipeId").value(veteranos));

        mockMvc.perform(get("/api/sorteio/historico")
                        .header("Authorization", token())
                        .param("equipeId", String.valueOf(veteranos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void drawRejectsPlayersFromDifferentEquipes() throws Exception {
        long a = createEquipe("Time A");
        long b = createEquipe("Time B");
        long pa = createPlayer("Jogador A", a);
        long pb = createPlayer("Jogador B", b);

        mockMvc.perform(post("/api/sorteio")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DrawRequest(List.of(pa, pb)))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void equipeWithPlayersCannotBeDeletedAndNamesAreUnique() throws Exception {
        long equipe = createEquipe("Com Jogadores");
        createPlayer("Fulano", equipe);

        mockMvc.perform(delete("/api/equipes/" + equipe).header("Authorization", token()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/equipes")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"com jogadores\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cronTriggerRequiresSecret() throws Exception {
        mockMvc.perform(post("/api/notificacoes/aniversarios/disparar"))
                .andExpect(status().isForbidden());
    }
}
