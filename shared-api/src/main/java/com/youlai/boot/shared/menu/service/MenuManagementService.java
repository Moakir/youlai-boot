package com.youlai.boot.shared.menu.service;

import com.youlai.boot.common.model.entity.GenConfig;

/**
 * 菜单管理服务接口
 * 用于代码生成模块创建菜单
 *
 * @author Ray
 * @since 2.10.0
 */
public interface MenuManagementService {

    /**
     * 为代码生成添加菜单
     *
     * @param parentMenuId 父菜单ID
     * @param genConfig    代码生成配置
     */
    void addMenuForCodegen(Long parentMenuId, GenConfig genConfig);
}
