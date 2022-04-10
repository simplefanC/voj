package com.simplefanc.voj.pojo.entity.judge;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @Author: chenfan
 * @Date: 2021/5/18 17:41
 * @Description:
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RemoteJudgeAccount对象", description = "远程判题服务的账号")
public class RemoteJudgeAccount {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "远程oj名字")
    private String oj;

    @ApiModelProperty(value = "账号用户名")
    private String username;

    @ApiModelProperty(value = "账号密码")
    private String password;

    @ApiModelProperty(value = "是否可用")
    private Boolean status;

    @ApiModelProperty(value = "废弃")
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;

}