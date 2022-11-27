package com.simplefanc.voj.backend.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simplefanc.voj.backend.pojo.vo.AnnouncementVO;
import com.simplefanc.voj.backend.service.admin.announcement.AdminAnnouncementService;
import com.simplefanc.voj.common.pojo.entity.common.Announcement;
import com.simplefanc.voj.common.result.CommonResult;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: chenfan
 * @Date: 2021/12/10 19:53
 * @Description:
 */
@RestController
@RequiresAuthentication
@RequiredArgsConstructor
public class AnnouncementController {

    private final AdminAnnouncementService adminAnnouncementService;

    @GetMapping("/api/admin/announcement")
    @RequiresPermissions("announcement_admin")
    public CommonResult<IPage<AnnouncementVO>> getAnnouncementList(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "currentPage", required = false) Integer currentPage) {
        return CommonResult.successResponse(adminAnnouncementService.getAnnouncementList(limit, currentPage));
    }

    @DeleteMapping("/api/admin/announcement")
    @RequiresPermissions("announcement_admin")
    public CommonResult<Void> deleteAnnouncement(@RequestParam("aid") Long aid) {
        adminAnnouncementService.deleteAnnouncement(aid);
        return CommonResult.successResponse();
    }

    @PostMapping("/api/admin/announcement")
    @RequiresRoles("root") // 只有超级管理员能操作
    @RequiresPermissions("announcement_admin")
    public CommonResult<Void> addAnnouncement(@RequestBody Announcement announcement) {
        adminAnnouncementService.addAnnouncement(announcement);
        return CommonResult.successResponse();
    }

    @PutMapping("/api/admin/announcement")
    @RequiresPermissions("announcement_admin")
    public CommonResult<Void> updateAnnouncement(@RequestBody Announcement announcement) {
        adminAnnouncementService.updateAnnouncement(announcement);
        return CommonResult.successResponse();
    }

}