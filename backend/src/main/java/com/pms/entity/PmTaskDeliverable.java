package com.pms.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PmTaskDeliverable {
    private Long id;
    private Long taskId;
    /** 交付物名称 */
    private String name;
    /** 类型：文档、代码、其他 */
    private String type;
    /** 链接或路径 */
    private String url;
    /** 上传文档存储路径（相对 upload 根目录） */
    private String attachmentPath;
    /** 上传文档原始文件名 */
    private String attachmentName;
    /** 备注 */
    private String remark;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
