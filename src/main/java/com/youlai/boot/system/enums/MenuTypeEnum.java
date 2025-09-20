package com.youlai.boot.system.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.youlai.boot.common.base.IBaseEnum;
import com.youlai.boot.system.model.entity.Menu;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 菜单类型枚举
 *
 * @author Ray.Hao
 * @since 2022/4/23 9:36
 */
@Getter
public enum MenuTypeEnum implements IBaseEnum<Integer> {

    NULL(0, null),
    MENU(1, "菜单"),
    CATALOG(2, "目录"),
    EXTLINK(3, "外链"),
    BUTTON(4, "按钮");

    //  Mybatis-Plus 提供注解表示插入数据库时插入该值
    @EnumValue
    private final Integer value;

    // @JsonValue //  表示对枚举序列化时返回此字段
    private final String label;

    /**
     * 按钮子类型枚举
     */
    public enum ButtonSubType {
        NORMAL("normal", "普通按钮"),
        DRILL_DOWN("drill", "下钻页面");

        private final String value;
        private final String label;

        ButtonSubType(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }

    MenuTypeEnum(Integer value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 判断是否为下钻按钮
     * 下钻按钮：type = 4（按钮）且 component 不为空
     *
     * @param menu 菜单对象
     * @return true-下钻按钮，false-普通按钮或其他类型
     */
    public static boolean isDrillDownButton(Menu menu) {
        if (menu == null) {
            return false;
        }
        return BUTTON.getValue().equals(menu.getType())
                && StringUtils.isNotBlank(menu.getRouteName());
    }

    /**
     * 根据菜单对象判断按钮子类型
     *
     * @param menu 菜单对象
     * @return 按钮子类型，如果不是按钮则返回null
     */
    public static ButtonSubType getButtonSubType(Menu menu) {
        if (menu == null || !BUTTON.getValue().equals(menu.getType())) {
            return ButtonSubType.NORMAL;
        }
        return isDrillDownButton(menu) ? ButtonSubType.DRILL_DOWN : ButtonSubType.NORMAL;
    }

}
