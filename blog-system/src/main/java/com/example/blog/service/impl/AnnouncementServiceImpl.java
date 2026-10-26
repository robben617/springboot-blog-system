package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.Constants;
import com.example.blog.dto.AnnouncementDTO;
import com.example.blog.entity.Announcement;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.AnnouncementMapper;
import com.example.blog.service.AnnouncementService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 公告业务层实现
 */
@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    /** 标题长度上限（与 announcement.title VARCHAR(200) 一致） */
    private static final int MAX_TITLE_LENGTH = 200;

    /** 最新公告默认条数 */
    private static final int DEFAULT_LATEST_LIMIT = 1;

    /** 最新公告最大条数（防止一次拉取过多） */
    private static final int MAX_LATEST_LIMIT = 50;

    @Override
    public List<Announcement> listAll() {
        return list(new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreateTime)
                .orderByDesc(Announcement::getId));
    }

    @Override
    public List<Announcement> listLatest(Integer limit) {
        int size = limit == null || limit <= 0 ? DEFAULT_LATEST_LIMIT : limit;
        if (size > MAX_LATEST_LIMIT) {
            size = MAX_LATEST_LIMIT;
        }
        return list(new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreateTime)
                .orderByDesc(Announcement::getId)
                .last("LIMIT " + size));
    }

    @Override
    public Announcement addAnnouncement(AnnouncementDTO dto) {
        String title = checkTitle(dto == null ? null : dto.getTitle());
        String content = checkContent(dto == null ? null : dto.getContent());

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        save(announcement);
        return announcement;
    }

    @Override
    public Announcement updateAnnouncement(Long id, AnnouncementDTO dto) {
        Announcement exist = getById(id);
        if (exist == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "公告不存在");
        }
        exist.setTitle(checkTitle(dto == null ? null : dto.getTitle()));
        exist.setContent(checkContent(dto == null ? null : dto.getContent()));
        updateById(exist);
        return exist;
    }

    @Override
    public void deleteAnnouncement(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "公告不存在");
        }
        removeById(id);
    }

    /**
     * 校验并清洗标题
     */
    private String checkTitle(String rawTitle) {
        String title = rawTitle == null ? "" : rawTitle.trim();
        if (title.isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "公告标题不能为空");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "公告标题不能超过 " + MAX_TITLE_LENGTH + " 个字符");
        }
        return title;
    }

    /**
     * 校验内容
     */
    private String checkContent(String rawContent) {
        if (rawContent == null || rawContent.trim().isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "公告内容不能为空");
        }
        return rawContent;
    }
}
