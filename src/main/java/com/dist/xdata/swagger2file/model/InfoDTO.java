package com.dist.xdata.swagger2file.model;

import lombok.Data;

@Data
public class InfoDTO {
    private String title;
    private String description;
    private ContactDTO contact;
    private String version;
}
