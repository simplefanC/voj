package com.simplefanc.voj.backend.pojo.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.HashMap;

/**
 * @Author: chenfan
 * @Date: 2022/3/11 16:12
 * @Description:
 */
@Data
public class CommentListVO {

    private IPage<CommentVO> commentList;

    private HashMap<Integer, Boolean> commentLikeMap;

}