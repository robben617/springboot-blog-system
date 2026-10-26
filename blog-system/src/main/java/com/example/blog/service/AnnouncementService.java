package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.AnnouncementDTO;
import com.example.blog.entity.Announcement;

import java.util.List;

/**
 * 公告业务层接口
 */
public interface AnnouncementService extends IService<Announcement> {

    /**
     * 查询全部公告（按创建时间倒序、id 倒序）
     */
    List<Announcement> listAll();

    /**
     * 查询最新若干条公告（首页展示用）
     *
     * @param limit 条数，null 或非法时取默认值
     */
    List<Announcement> listLatest(Integer limit);

    /**
     * 新增公告（管理员）
     */
    Announcement addAnnouncement(AnnouncementDTO dto);

    /**
     * 修改公告（管理员）
     */
    Announcement updateAnnouncement(Long id, AnnouncementDTO dto);

    /**
     * 删除公告（管理员）
     */
    void deleteAnnouncement(Long id);
}
