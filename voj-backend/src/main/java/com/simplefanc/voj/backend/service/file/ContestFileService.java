package com.simplefanc.voj.backend.service.file;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: chenfan
 * @Date: 2022/3/10 14:27
 * @Description:
 */
public interface ContestFileService {

    void downloadContestRank(Long cid, Boolean forceRefresh, Boolean removeStar, HttpServletResponse response)
            throws IOException;

    void downloadContestAcSubmission(Long cid, Boolean excludeAdmin, Boolean allStatus, String splitType, HttpServletResponse response);

    void downloadContestPrintText(Long id, HttpServletResponse response);

}