package com.simplefanc.voj.backend.dao.common;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.backend.pojo.vo.ACMContestRankVo;
import com.simplefanc.voj.backend.pojo.vo.OIContestRankVo;
import com.simplefanc.voj.common.pojo.entity.common.File;

import java.util.List;

public interface FileEntityService extends IService<File> {

    int updateFileToDeleteByUidAndType(String uid, String type);

    List<File> queryDeleteAvatarList();

    List<File> queryCarouselFileList();

    List<List<String>> getContestRankExcelHead(List<String> contestProblemDisplayIdList, Boolean isACM);

    List<List<Object>> changeACMContestRankToExcelRowList(List<ACMContestRankVo> acmContestRankVoList,
                                                          List<String> contestProblemDisplayIdList, String rankShowName);

    List<List<Object>> changeOIContestRankToExcelRowList(List<OIContestRankVo> oiContestRankVoList,
                                                         List<String> contestProblemDisplayIdList, String rankShowName);

}
