package com.dist.xdata.swagger2file.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class DataTypeDTO {
    private String type;
    private String description;
    private JSONObject items;
}
