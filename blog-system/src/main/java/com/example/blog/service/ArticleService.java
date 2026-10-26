package com.example.blog.service;

import com.example.blog.dto.ArticleDTO;
import com.example.blog.entity.Article;
import com.example.blog.util.PageResult;
import com.example.blog.vo.ArticleAdminVO;
import com.example.blog.vo.ArticleBriefVO;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.ArticleManageVO;

/**
 * 文章业务层接口
 */
public interface ArticleService {

    /**
     * 分页查询已发布文章（公开列表 / 按关键词搜索）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  搜索关键词（可为空，匹配标题/摘要/正文）
     */
    PageResult<ArticleBriefVO> pagePublished(Long pageNum, Long pageSize, String keyword);

    /**
     * 管理员文章分页列表
     * <p>
     * 管理员可查看全站所有文章（含草稿），支持按关键词、状态、分类筛选。
     *
     * @param pageNum    页码
     * @param pageSize   每页大小
     * @param keyword    关键词（匹配标题、用户名、昵称），可为 null
     * @param status     状态筛选：0-草稿 1-已发布，null 为全部
     * @param categoryId 分类筛选，null 为全部
     */
    PageResult<ArticleAdminVO> pageAdminArticles(Long pageNum, Long pageSize, String keyword,
                                                  Integer status, Long categoryId);

    /**
     * 查询文章详情（浏览量 +1）
     * <p>
     * 已发布文章所有人可见；草稿仅作者本人或管理员可见，其余人按「文章不存在」处理。
     *
     * @param id          文章ID
     * @param currentUserId 当前登录用户ID（游客为 null）
     * @param currentRole   当前登录用户角色（游客为 null）
     */
    ArticleDetailVO getDetail(Long id, Long currentUserId, Integer currentRole);

    /**
     * 查询文章详情（供作者 / 管理员编辑回显，不增加浏览量）
     * <p>
     * 与 getDetail 的区别：不做浏览量自增，且仅作者本人或管理员可访问。
     *
     * @param id            文章ID
     * @param currentUserId 当前登录用户ID
     * @param currentRole   当前登录用户角色
     */
    ArticleDetailVO getDetailForEdit(Long id, Long currentUserId, Integer currentRole);

    /**
     * 分页查询当前用户自己的文章（含草稿，作者管理用）
     *
     * @param userId   当前登录用户ID
     * @param status   状态筛选：0-只看草稿 1-只看已发布，null-全部
     */
    PageResult<ArticleManageVO> pageMyArticles(Long userId, Integer status, Long pageNum, Long pageSize);

    /**
     * 新建文章（可存草稿或直接发布）
     *
     * @param userId 当前登录用户ID（作为作者）
     */
    Article addArticle(Long userId, ArticleDTO dto);

    /**
     * 修改文章（仅作者本人或管理员）
     */
    Article updateArticle(Long id, Long operatorId, Integer operatorRole, ArticleDTO dto);

    /**
     * 删除文章（逻辑删除，仅作者本人或管理员；同时解除标签关联）
     */
    void deleteArticle(Long id, Long operatorId, Integer operatorRole);

    /**
     * 发布文章（草稿 -> 已发布，仅作者本人或管理员）
     */
    void publishArticle(Long id, Long operatorId, Integer operatorRole);

    /**
     * 转为草稿（已发布 -> 草稿，仅作者本人或管理员）
     */
    void unpublishArticle(Long id, Long operatorId, Integer operatorRole);
}
