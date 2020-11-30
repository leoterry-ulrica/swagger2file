package com.dist.xdata.swagger2file.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SwaggerAPIDTO {
    private String openapi;
    private InfoDTO info;
    private List<ServerDTO> servers;
    private List<TagDTO> tags;
    private Map<String, Map<String, MethodDTO>> paths;
    private Map<String, Map<String, ObjectDTO>> components;
}
