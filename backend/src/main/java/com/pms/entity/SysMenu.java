package com.pms.entity;

import lombok.Data;

import java.util.List;

@Data
public class SysMenu {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String permission;
    private Integer type;
    private Integer sortOrder;
    private String icon;
    private Integer status;
    private List<SysMenu> children;
}
