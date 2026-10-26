package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.Constants;
import com.example.blog.dto.FriendLinkDTO;
import com.example.blog.entity.FriendLink;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.FriendLinkMapper;
import com.example.blog.service.FriendLinkService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 友情链接业务层实现
 */
@Service
public class FriendLinkServiceImpl extends ServiceImpl<FriendLinkMapper, FriendLink> implements FriendLinkService {

    /** 网站名称长度上限（与 friend_link.name VARCHAR(50) 一致） */
    private static final int MAX_NAME_LENGTH = 50;

    /** URL 长度上限（与 friend_link.url VARCHAR(255) 一致） */
    private static final int MAX_URL_LENGTH = 255;

    @Override
    public List<FriendLink> listAll() {
        return list(new LambdaQueryWrapper<FriendLink>()
                .orderByAsc(FriendLink::getSort)
                .orderByAsc(FriendLink::getId));
    }

    @Override
    public FriendLink addFriendLink(FriendLinkDTO dto) {
        FriendLink friendLink = new FriendLink();
        friendLink.setName(checkName(dto == null ? null : dto.getName()));
        friendLink.setUrl(checkUrl(dto == null ? null : dto.getUrl()));
        friendLink.setSort(dto == null || dto.getSort() == null ? 0 : dto.getSort());
        save(friendLink);
        return friendLink;
    }

    @Override
    public FriendLink updateFriendLink(Long id, FriendLinkDTO dto) {
        FriendLink exist = getById(id);
        if (exist == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "友链不存在");
        }
        exist.setName(checkName(dto == null ? null : dto.getName()));
        exist.setUrl(checkUrl(dto == null ? null : dto.getUrl()));
        if (dto != null && dto.getSort() != null) {
            exist.setSort(dto.getSort());
        }
        updateById(exist);
        return exist;
    }

    @Override
    public void deleteFriendLink(Long id) {
        if (getById(id) == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "友链不存在");
        }
        removeById(id);
    }

    /**
     * 校验并清洗网站名称
     */
    private String checkName(String rawName) {
        String name = rawName == null ? "" : rawName.trim();
        if (name.isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "网站名称不能为空");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "网站名称不能超过 " + MAX_NAME_LENGTH + " 个字符");
        }
        return name;
    }

    /**
     * 校验网站地址：非空、长度合法、必须以 http:// 或 https:// 开头
     */
    private String checkUrl(String rawUrl) {
        String url = rawUrl == null ? "" : rawUrl.trim();
        if (url.isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "网站地址不能为空");
        }
        if (url.length() > MAX_URL_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "网站地址不能超过 " + MAX_URL_LENGTH + " 个字符");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "网站地址必须以 http:// 或 https:// 开头");
        }
        return url;
    }
}
