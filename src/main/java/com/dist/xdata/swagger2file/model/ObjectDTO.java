package com.dist.xdata.swagger2file.model;

import lombok.Data;

import java.util.Map;

@Data
public class ObjectDTO {
    private String title;
    private String type;
    private Map<String, DataTypeDTO> properties;
}
