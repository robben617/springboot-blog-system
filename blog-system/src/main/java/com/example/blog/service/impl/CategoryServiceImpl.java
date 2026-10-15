package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.Constants;
import com.example.blog.dto.CategoryDTO;
import com.example.blog.entity.Category;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.service.CategoryService;
import com.example.blog.util.PageResult;
import com.example.blog.util.PageUtils;
import com.example.blog.vo.ArticleBriefVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类业务层实现
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    /** 分类名称长度上限（与 category.name VARCHAR(50) 一致） */
    private static final int MAX_NAME_LENGTH = 50;

    @Override
    public List<Category> listAll() {
        return list(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    @Override
    public Category addCategory(CategoryDTO dto) {
        String name = checkName(dto == null ? null : dto.getName());

        // 重名校验：提前拦截，避免直接撞数据库唯一约束抛出难懂的 SQL 异常
        if (existsByName(name, null)) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "分类名称已存在");
        }

        Category category = new Category();
        category.setName(name);
        category.setSort(dto.getSort() == null ? 0 : dto.getSort());
        save(category);
        return category;
    }

    @Override
    public Category updateCategory(Long id, CategoryDTO dto) {
        Category exist = getById(id);
        if (exist == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "分类不存在");
        }
        String name = checkName(dto == null ? null : dto.getName());
        if (existsByName(name, id)) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "分类名称已存在");
        }

        exist.setName(name);
        exist.setSort(dto.getSort() == null ? exist.getSort() : dto.getSort());
        updateById(exist);
        return exist;
    }

    @Override
    public void deleteCategory(Long id) {
        Category exist = getById(id);
        if (exist == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "分类不存在");
        }

        // 业务规则：删除分类前须确认该分类下无关联文章，有则禁止删除
        long articleCount = baseMapper.countArticles(id);
        if (articleCount > 0) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    "该分类下还有 " + articleCount + " 篇文章，无法删除；请先将文章移到其他分类或删除文章");
        }

        removeById(id);
    }

    @Override
    public PageResult<ArticleBriefVO> pageArticlesByCategory(Long categoryId, Long pageNum, Long pageSize) {
        if (categoryId == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "分类ID不能为空");
        }
        if (getById(categoryId) == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "分类不存在");
        }

        Page<ArticleBriefVO> page = new Page<>(
                PageUtils.normalizePageNum(pageNum),
                PageUtils.normalizePageSize(pageSize));
        IPage<ArticleBriefVO> result = baseMapper.selectArticlePage(page, categoryId);
        return PageUtils.convert(result);
    }

    /**
     * 校验并清洗分类名称
     */
    private String checkName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "分类名称不能为空");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "分类名称不能超过 " + MAX_NAME_LENGTH + " 个字符");
        }
        return name;
    }

    /**
     * 判断分类名称是否已存在
     *
     * @param excludeId 修改场景下排除自身ID，传 null 表示新增
     */
    private boolean existsByName(String name, Long excludeId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getName, name);
        if (excludeId != null) {
            wrapper.ne(Category::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
