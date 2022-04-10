package com.simplefanc.voj.pojo.dto;

import lombok.Data;
import lombok.experimental.Accessors;
import com.simplefanc.voj.pojo.entity.discussion.Reply;

/**
 * @Author: chenfan
 * @Date: 2021/6/24 17:00
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ReplyDto {

    private Reply reply;

    private Integer did;

    private Integer quoteId;

    private String quoteType;
}