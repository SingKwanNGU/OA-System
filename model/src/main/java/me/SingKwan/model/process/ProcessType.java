package me.SingKwan.model.process;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.SingKwan.model.base.BaseEntity;

import java.util.List;

@Data
@ApiModel(description = "ProcessType")
@TableName("oa_process_type")
public class ProcessType extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "类型名称")
	@TableField("name")
	private String name;

	@ApiModelProperty(value = "描述")
	@TableField("description")
	private String description;

	@TableField(exist = false)
	private List<ProcessTemplate> processTemplateList;
}