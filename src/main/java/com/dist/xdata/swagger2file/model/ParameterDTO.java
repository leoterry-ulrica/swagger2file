package com.dist.xdata.swagger2file.model;

import lombok.Data;

import java.util.Map;

@Data
public class ParameterDTO {
    private String name;
    private String in;
    private String description;
    private boolean required;
    private String style;
    private Map<String, String> schema;
}
