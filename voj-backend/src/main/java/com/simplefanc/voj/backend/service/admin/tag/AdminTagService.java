package com.simplefanc.voj.backend.service.admin.tag;

import com.simplefanc.voj.common.pojo.entity.problem.Tag;
import com.simplefanc.voj.common.pojo.entity.problem.TagClassification;

import java.util.List;

/**
 * @Author: chenfan
 * @Date: 2022/3/9 17:47
 * @Description:
 */

public interface AdminTagService {

    Tag addTag(Tag tag);

    void updateTag(Tag tag);

    void deleteTag(Long tid);

    List<TagClassification> getTagClassification(String oj);

    TagClassification addTagClassification(TagClassification tagClassification);

    void updateTagClassification(TagClassification tagClassification);

    void deleteTagClassification(Long tcid);
}