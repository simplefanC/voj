package com.simplefanc.voj.pojo.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:12
 * @Description:
 */
@Data
public class CommentListVo {

    private IPage<CommentVo> commentList;

    private HashMap<Integer, Boolean> commentLikeMap;
}