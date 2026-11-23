package com.proyecto.inventario.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.inventario.agent.*;
import com.proyecto.inventario.skills.InventorySkill;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents/{profile}")
@RequiredArgsConstructor
public class AgentController {
    public record Message(String mensaje) {}
    private final AgentFactory factory;
    private final AgentService service;

    @GetMapping("/skills")
    public List<InventorySkill.Descriptor> skills(@PathVariable String profile,
            @RequestHeader(value="X-Agent-Key", required=false) String key) {
        return factory.open(profile,key).skills().values().stream().map(InventorySkill::descriptor)
                .sorted(java.util.Comparator.comparing(InventorySkill.Descriptor::name)).toList();
    }

    @PostMapping("/chat")
    public AgentService.Reply chat(@PathVariable String profile,
            @RequestHeader(value="X-Agent-Key", required=false) String key, @RequestBody Message body) {
        return service.chat(profile,key,body.mensaje());
    }

    @PostMapping("/skills/{name}")
    public AgentService.Reply execute(@PathVariable String profile, @PathVariable String name,
            @RequestHeader(value="X-Agent-Key", required=false) String key, @RequestBody JsonNode input) {
        return service.execute(profile,key,name,input);
    }
}
