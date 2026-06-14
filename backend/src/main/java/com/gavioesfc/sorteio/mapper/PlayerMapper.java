package com.gavioesfc.sorteio.mapper;

import com.gavioesfc.sorteio.dto.PlayerCreateRequest;
import com.gavioesfc.sorteio.dto.PlayerResponse;
import com.gavioesfc.sorteio.dto.PlayerUpdateRequest;
import com.gavioesfc.sorteio.model.entity.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Player toEntity(PlayerCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(PlayerUpdateRequest request, @MappingTarget Player player);

    PlayerResponse toResponse(Player player);
}
