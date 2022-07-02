package com.simplefanc.voj.backend.controller.file;

import com.simplefanc.voj.backend.service.file.ContestFileService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author: chenfan
 * @Date: 2021/10/5 19:55
 * @Description:
 */
@Controller
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class ContestFileController {

    private final ContestFileService contestFileService;

    @GetMapping("/download-contest-rank")
    @RequiresAuthentication
    public void downloadContestRank(@RequestParam("cid") Long cid, @RequestParam("forceRefresh") Boolean forceRefresh,
                                    @RequestParam(value = "removeStar", defaultValue = "false") Boolean removeStar,
                                    HttpServletResponse response) throws IOException {
        contestFileService.downloadContestRank(cid, forceRefresh, removeStar, response);
    }

    @GetMapping("/download-contest-ac-submission")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public void downloadContestAcSubmission(@RequestParam("cid") Long cid,
                                            @RequestParam(value = "excludeAdmin", defaultValue = "false") Boolean excludeAdmin,
                                            @RequestParam(value = "splitType", defaultValue = "user") String splitType, HttpServletResponse response) {

        contestFileService.downloadContestAcSubmission(cid, excludeAdmin, splitType, response);
    }

    @GetMapping("/download-contest-print-text")
    @RequiresAuthentication
    @RequiresRoles(value = {"root", "admin", "problem_admin"}, logical = Logical.OR)
    public void downloadContestPrintText(@RequestParam("id") Long id, HttpServletResponse response) {
        contestFileService.downloadContestPrintText(id, response);
    }

}