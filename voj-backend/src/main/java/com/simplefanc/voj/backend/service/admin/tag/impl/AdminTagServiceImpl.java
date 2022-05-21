package com.simplefanc.voj.backend.service.admin.tag.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.problem.TagEntityService;
import com.simplefanc.voj.backend.service.admin.tag.AdminTagService;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 17:47
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private final TagEntityService tagEntityService;

    public Tag addProblem(Tag tag) {
        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("name", tag.getName()).eq("oj", tag.getOj());
        Tag existTag = tagEntityService.getOne(tagQueryWrapper, false);

        if (existTag != null) {
            throw new StatusFailException("该标签名称已存在！请勿重复添加！");
        }

        boolean isOk = tagEntityService.save(tag);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        return tag;
    }

    public void updateTag(Tag tag) {
        boolean isOk = tagEntityService.updateById(tag);
        if (!isOk) {
            throw new StatusFailException("更新失败");
        }
    }

    public void deleteTag(Long tid) {
        boolean isOk = tagEntityService.removeById(tid);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }

}