package com.dist.xdata.swagger2file.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MethodDTO {
    private List<String> tags;
    private String summary;
    private String operationId;
    private Map<String, JSONObject> requestBody;
    private Map<String, JSONObject> responses;
    private List<ParameterDTO> parameters;
}
