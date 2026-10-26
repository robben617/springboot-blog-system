package com.example.blog.service;

import com.example.blog.dto.ChangePasswordDTO;
import com.example.blog.dto.UpdateProfileDTO;
import com.example.blog.util.PageResult;
import com.example.blog.vo.UserVO;
import com.example.blog.vo.UserStatsVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户资料与用户管理业务层接口
 * <p>
 * 说明：本接口只承载「个人资料维护」与「管理员用户管理」两类新增能力，
 * 注册、登录、退出等已有能力保留在原有 UserService / UserController 中，不做改动。
 */
public interface UserManageService {

    /**
     * 修改当前登录用户的个人资料（只更新传入的字段）
     *
     * @param userId 当前登录用户ID
     * @param dto    待修改的字段
     * @return 修改后的用户信息（不含密码、盐值）
     */
    UserVO updateProfile(Long userId, UpdateProfileDTO dto);

    /**
     * 修改当前登录用户的密码（需校验原密码）
     *
     * @param userId 当前登录用户ID
     * @param dto    原密码 + 新密码
     */
    void changePassword(Long userId, ChangePasswordDTO dto);

    /**
     * 上传头像图片，返回可直接访问的相对路径
     *
     * @param file 头像图片（不超过2MB，仅支持 jpg/jpeg/png/gif/webp）
     * @return 访问路径，形如 /upload/yyyy/MM/dd/xxx.jpg
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 分页查询用户列表（管理员）
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param keyword  关键词，按用户名或昵称模糊匹配（可为空）
     * @param status   账号状态筛选：0-禁用 1-启用，null 为全部
     */
    PageResult<UserVO> pageUsers(Long pageNum, Long pageSize, String keyword, Integer status);

    /**
     * 修改指定用户的账号状态（管理员）
     *
     * @param id     目标用户ID
     * @param status 账号状态：0-禁用 1-启用
     * @return 修改后的用户信息
     */
    UserVO updateStatus(Long id, Integer status);

    /**
     * 删除指定用户（管理员）
     * <p>
     * 规则：级联删除该用户发布的全部文章、草稿与评论；管理员账号不可被删除。
     * 用户与文章、评论均为逻辑删除；文章-标签中间表关联为物理删除。
     *
     * @param id 目标用户ID
     */
    void deleteUser(Long id);

    /**
     * 将指定用户的密码重置为系统初始密码（管理员）
     * <p>
     * 规则：系统不提供自助密码找回功能，忘记密码时由管理员在后台重置为初始密码。
     *
     * @param id 目标用户ID
     */
    void resetPassword(Long id);

    /**
     * 查询当前登录用户的创作统计（个人中心展示）
     *
     * @param userId 当前登录用户ID
     * @return 文章数、评论数与累计浏览量
     */
    UserStatsVO getUserStats(Long userId);
}
