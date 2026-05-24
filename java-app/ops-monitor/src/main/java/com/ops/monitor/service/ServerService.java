package com.ops.monitor.service;

import com.ops.monitor.dto.ServerDto;
import com.ops.monitor.entity.Server;
import com.ops.monitor.repository.ServerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServerService {

    private final ServerRepository serverRepository;

    public ServerService(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    public List<ServerDto> getAllServers() {
        return serverRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ServerDto createServer(ServerDto dto) {
        Server server = new Server();
        server.setName(dto.getName());
        server.setHost(dto.getHost());
        server.setOs(dto.getOs());
        server.setStatus(dto.getStatus() != null ? dto.getStatus() : "ONLINE");
        return toDto(serverRepository.save(server));
    }

    public void deleteServer(Long id) {
        serverRepository.deleteById(id);
    }

    private ServerDto toDto(Server server) {
        return new ServerDto(
                server.getId(),
                server.getName(),
                server.getHost(),
                server.getOs(),
                server.getStatus()
        );
    }
}
