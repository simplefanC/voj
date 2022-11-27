package com.simplefanc.voj.backend.pojo.dto;

import com.simplefanc.voj.common.pojo.entity.discussion.Reply;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: chenfan
 * @Date: 2021/6/24 17:00
 * @Description:
 */
@Data
@Accessors(chain = true)
public class ReplyDTO {

    private Reply reply;

    private Integer did;

    private Integer quoteId;

    private String quoteType;

}