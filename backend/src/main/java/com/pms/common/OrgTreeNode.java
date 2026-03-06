package com.pms.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 组织树节点：公司 -> 部门 -> 角色 -> 用户（同一用户可在多角色下出现） */
@Data
public class OrgTreeNode {
    /** 节点类型：company / dept / role / user */
    private String type;
    private Long id;
    private String label;
    /** el-tree 的 node-key 需要全局唯一 */
    private String nodeKey;
    private List<OrgTreeNode> children;

    public OrgTreeNode() {
        this.children = new ArrayList<>();
    }

    public OrgTreeNode(String type, Long id, String label) {
        this.type = type;
        this.id = id;
        this.label = label;
        this.nodeKey = type + "_" + id;
        this.children = new ArrayList<>();
    }
}

