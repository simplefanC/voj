package com.simplefanc.voj.dao.common;

import com.baomidou.mybatisplus.extension.service.IService;
import com.simplefanc.voj.pojo.entity.common.File;
import com.simplefanc.voj.pojo.vo.ACMContestRankVo;
import com.simplefanc.voj.pojo.vo.OIContestRankVo;

import java.util.List;

public interface FileEntityService extends IService<File> {
    int updateFileToDeleteByUidAndType(String uid, String type);

    List<File> queryDeleteAvatarList();

    List<File> queryCarouselFileList();

    List<List<String>> getContestRankExcelHead(List<String> contestProblemDisplayIDList, Boolean isACM);

    List<List<Object>> changeACMContestRankToExcelRowList(List<ACMContestRankVo> acmContestRankVoList,
                                                          List<String> contestProblemDisplayIDList,
                                                          String rankShowName);

    List<List<Object>> changOIContestRankToExcelRowList(List<OIContestRankVo> oiContestRankVoList,
                                                        List<String> contestProblemDisplayIDList,
                                                        String rankShowName);
}
