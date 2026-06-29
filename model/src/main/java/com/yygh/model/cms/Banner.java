package com.yygh.model.cms;

import com.yygh.model.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 首页Banner实体
 * </p>
 *
 * @author XXJ
 * @since 2019-11-08
 */
@Data
@Schema(description = "首页Banner实体")
@TableName("banner")
public class Banner extends BaseEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Schema(description = "标题")
	@TableField("title")
	private String title;

	@Schema(description = "图片地址")
	@TableField("image_url")
	private String imageUrl;

	@Schema(description = "链接地址")
	@TableField("link_url")
	private String linkUrl;

	@Schema(description = "排序")
	@TableField("sort")
	private Integer sort;

}

