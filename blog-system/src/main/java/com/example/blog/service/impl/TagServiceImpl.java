package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.Constants;
import com.example.blog.dto.TagDTO;
import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.service.TagService;
import com.example.blog.util.PageResult;
import com.example.blog.util.PageUtils;
import com.example.blog.vo.ArticleBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 标签业务层实现
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    /** 标签名称长度上限（与 tag.name VARCHAR(50) 一致） */
    private static final int MAX_NAME_LENGTH = 50;

    private final ArticleTagMapper articleTagMapper;

    @Override
    public List<Tag> listAll() {
        return list(new LambdaQueryWrapper<Tag>().orderByAsc(Tag::getName));
    }

    @Override
    public Tag addTag(TagDTO dto) {
        String name = checkName(dto == null ? null : dto.getName());

        // tag.name 有唯一索引 uk_tag_name，提前拦截重名避免抛出 SQL 异常
        if (existsByName(name, null)) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setName(name);
        save(tag);
        return tag;
    }

    @Override
    public Tag updateTag(Long id, TagDTO dto) {
        Tag exist = getById(id);
        if (exist == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "标签不存在");
        }
        String name = checkName(dto == null ? null : dto.getName());
        if (existsByName(name, id)) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "标签名称已存在");
        }

        exist.setName(name);
        updateById(exist);
        return exist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Long id) {
        Tag exist = getById(id);
        if (exist == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "标签不存在");
        }

        // 业务规则：删除标签时自动解除该标签与所有文章的关联，文章本身不受影响
        // 先解绑再删标签，两步必须同事务，避免解绑成功而删除失败造成脏数据
        articleTagMapper.deleteByTagId(id);
        removeById(id);
    }

    @Override
    public PageResult<ArticleBriefVO> pageArticlesByTag(Long tagId, Long pageNum, Long pageSize) {
        if (tagId == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "标签ID不能为空");
        }
        if (getById(tagId) == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "标签不存在");
        }

        Page<ArticleBriefVO> page = new Page<>(
                PageUtils.normalizePageNum(pageNum),
                PageUtils.normalizePageSize(pageSize));
        IPage<ArticleBriefVO> result = baseMapper.selectArticlePage(page, tagId);
        return PageUtils.convert(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceArticleTags(Long articleId, List<Long> tagIds) {
        if (articleId == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "文章ID不能为空");
        }
        // 先清空旧关联，再写入新关联（全量覆盖式更新）
        articleTagMapper.deleteByArticleId(articleId);

        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            if (tagId == null) {
                continue;
            }
            if (getById(tagId) == null) {
                throw new BusinessException(Constants.CODE_BAD_REQUEST, "标签不存在：id=" + tagId);
            }
            articleTagMapper.insert(articleId, tagId);
        }
    }

    @Override
    public List<Long> listTagIdsByArticleId(Long articleId) {
        if (articleId == null) {
            return Collections.emptyList();
        }
        return articleTagMapper.selectTagIdsByArticleId(articleId);
    }

    /**
     * 校验并清洗标签名称
     */
    private String checkName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "标签名称不能为空");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "标签名称不能超过 " + MAX_NAME_LENGTH + " 个字符");
        }
        return name;
    }

    /**
     * 判断标签名称是否已存在
     *
     * @param excludeId 修改场景下排除自身ID，传 null 表示新增
     */
    private boolean existsByName(String name, Long excludeId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, name);
        if (excludeId != null) {
            wrapper.ne(Tag::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
