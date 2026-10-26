package com.example.blog.service.impl;

import com.example.blog.common.Constants;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.AdminStatsService;
import com.example.blog.vo.AdminStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 后台管理统计业务层实现
 * <p>
 * 只做只读聚合统计，复用已有 Mapper：
 * 单表计数交给 MyBatis-Plus（逻辑删除条件自动拼接），
 * 浏览量求和与按状态计数走 ArticleMapper 的手写 SQL。
 */
@Service
@RequiredArgsConstructor
public class AdminStatsServiceImpl implements AdminStatsService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;

    @Override
    public AdminStatsVO getStats() {
        AdminStatsVO vo = new AdminStatsVO();
        vo.setArticleCount(articleMapper.selectCount(null));
        vo.setPublishedCount(articleMapper.countByStatus(Constants.ARTICLE_PUBLISHED));
        vo.setDraftCount(articleMapper.countByStatus(Constants.ARTICLE_DRAFT));
        vo.setUserCount(userMapper.selectCount(null));
        vo.setCommentCount(commentMapper.selectCount(null));
        vo.setTotalViews(articleMapper.sumViewCount());
        return vo;
    }
}
