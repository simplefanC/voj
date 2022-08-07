package com.simplefanc.voj.backend.service.admin.tag.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.simplefanc.voj.backend.common.exception.StatusFailException;
import com.simplefanc.voj.backend.dao.problem.TagClassificationEntityService;
import com.simplefanc.voj.backend.dao.problem.TagEntityService;
import com.simplefanc.voj.backend.service.admin.tag.AdminTagService;
import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import com.simplefanc.voj.common.pojo.entity.problem.TagClassification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 17:47
 * @Description:
 */
@Service
@RequiredArgsConstructor
public class AdminTagServiceImpl implements AdminTagService {

    private final TagEntityService tagEntityService;
    private final TagClassificationEntityService tagClassificationEntityService;

    @Override
    public Tag addTag(Tag tag) {
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

    @Override
    public void updateTag(Tag tag) {
        boolean isOk = tagEntityService.updateById(tag);
        if (!isOk) {
            throw new StatusFailException("更新失败");
        }
    }

    @Override
    public void deleteTag(Long tid) {
        boolean isOk = tagEntityService.removeById(tid);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }

    @Override
    public List<TagClassification> getTagClassification(String oj) {
        oj = oj.toUpperCase();
        if ("ALL".equals(oj)) {
            return tagClassificationEntityService.list();
        } else {
            QueryWrapper<TagClassification> tagClassificationQueryWrapper = new QueryWrapper<>();
            tagClassificationQueryWrapper.eq("oj", oj).orderByAsc("`rank`");
            return tagClassificationEntityService.list(tagClassificationQueryWrapper);
        }
    }

    @Override
    public TagClassification addTagClassification(TagClassification tagClassification) throws StatusFailException {
        QueryWrapper<TagClassification> tagClassificationQueryWrapper = new QueryWrapper<>();
        tagClassificationQueryWrapper.eq("name", tagClassification.getName())
                .eq("oj", tagClassification.getOj());
        TagClassification existTagClassification = tagClassificationEntityService.getOne(tagClassificationQueryWrapper, false);

        if (existTagClassification != null) {
            throw new StatusFailException("该标签分类名称已存在！请勿重复！");
        }
        boolean isOk = tagClassificationEntityService.save(tagClassification);
        if (!isOk) {
            throw new StatusFailException("添加失败");
        }
        return tagClassification;
    }

    @Override
    public void updateTagClassification(TagClassification tagClassification) throws StatusFailException {
        boolean isOk = tagClassificationEntityService.updateById(tagClassification);
        if (!isOk) {
            throw new StatusFailException("更新失败");
        }
    }

    @Override
    public void deleteTagClassification(Long tcid) throws StatusFailException {
        boolean isOk = tagClassificationEntityService.removeById(tcid);
        if (!isOk) {
            throw new StatusFailException("删除失败");
        }
    }

}