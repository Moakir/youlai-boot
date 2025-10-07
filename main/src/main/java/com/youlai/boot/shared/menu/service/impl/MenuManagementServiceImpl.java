package com.youlai.boot.shared.menu.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youlai.boot.common.model.entity.GenConfig;
import com.youlai.boot.shared.menu.service.MenuManagementService;
import com.youlai.boot.common.constant.SystemConstants;
import com.youlai.boot.system.enums.MenuTypeEnum;
import com.youlai.boot.system.model.entity.Menu;
import com.youlai.boot.system.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * 菜单管理服务实现
 *
 * @author Ray
 * @since 2.10.0
 */
@Service
@RequiredArgsConstructor
public class MenuManagementServiceImpl implements MenuManagementService {

    private final MenuService menuService;

    @Override
    public void addMenuForCodegen(Long parentMenuId, GenConfig genConfig) {
        Menu parentMenu = menuService.getById(parentMenuId);
        Assert.notNull(parentMenu, "上级菜单不存在");

        String entityName = genConfig.getEntityName();

        long count = menuService.count(new LambdaQueryWrapper<Menu>().eq(Menu::getRouteName, entityName));
        if (count > 0) {
            return;
        }

        // 获取父级菜单子菜单最大的排序
        Menu maxSortMenu = menuService.getOne(new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, parentMenuId)
                .orderByDesc(Menu::getSort)
                .last("limit 1")
        );
        int sort = 1;
        if (maxSortMenu != null) {
            sort = maxSortMenu.getSort() + 1;
        }

        Menu menu = new Menu();
        menu.setParentId(parentMenuId);
        menu.setName(genConfig.getBusinessName());
        menu.setRouteName(entityName);
        menu.setRoutePath(StrUtil.toSymbolCase(entityName, '-'));
        menu.setComponent(genConfig.getModuleName() + "/" + StrUtil.toSymbolCase(entityName, '-') + "/index");
        menu.setType(MenuTypeEnum.MENU.getValue());
        menu.setSort(sort);
        menu.setVisible(1);
        boolean result = menuService.save(menu);

        if (result) {
            // 生成treePath
            String treePath = menuService.generateMenuTreePath(parentMenuId);
            menu.setTreePath(treePath);
            menuService.updateById(menu);

            // 生成CURD按钮权限
            String permPrefix = genConfig.getModuleName() + ":" + genConfig.getTableName().replace("_", "-") + ":";
            String[] actions = {"查询", "新增", "编辑", "删除"};
            String[] perms = {"query", "add", "edit", "delete"};

            for (int i = 0; i < actions.length; i++) {
                Menu button = new Menu();
                button.setParentId(menu.getId());
                button.setType(MenuTypeEnum.BUTTON.getValue());
                button.setName(actions[i]);
                button.setPerm(permPrefix + perms[i]);
                button.setSort(i + 1);
                menuService.save(button);

                // 生成treePath
                button.setTreePath(treePath + "," + button.getId());
                menuService.updateById(button);
            }
        }
    }

}
