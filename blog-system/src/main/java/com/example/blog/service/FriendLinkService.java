package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.FriendLinkDTO;
import com.example.blog.entity.FriendLink;

import java.util.List;

/**
 * 友情链接业务层接口
 */
public interface FriendLinkService extends IService<FriendLink> {

    /**
     * 查询全部友链（按 sort 升序、id 升序）
     */
    List<FriendLink> listAll();

    /**
     * 新增友链（管理员）
     */
    FriendLink addFriendLink(FriendLinkDTO dto);

    /**
     * 修改友链（管理员）
     */
    FriendLink updateFriendLink(Long id, FriendLinkDTO dto);

    /**
     * 删除友链（管理员）
     */
    void deleteFriendLink(Long id);
}
