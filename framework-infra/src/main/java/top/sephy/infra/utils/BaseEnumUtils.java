package top.sephy.infra.utils;

import org.slf4j.Logger;

public class BaseEnumUtils {

    public static Logger log = org.slf4j.LoggerFactory.getLogger(BaseEnumUtils.class);

    /**
     * 根据状态码获取枚举（防御性方法，向后兼容）
     * <p>
     * 如果遇到未知的状态码：
     * - 记录警告日志
     * - 返回 UNKNOWN 而不是抛异常
     * - 保证旧版本代码可以正常读取新版本数据
     *
     * @param code 状态码
     * @return 对应的枚举值，未知时返回 UNKNOWN
     */
    public static <T extends Enum<T>> T fromCode(Class<T> enumClass, String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }

        for (T status : enumClass.getEnumConstants()) {
            if (status.toString().equals(code)) {
                return status;
            }
        }

        // 向后兼容：遇到未知值不抛异常，返回 UNKNOWN 并记录日志
        log.warn("遇到未知的枚举值: {}, 返回 UNKNOWN。请检查是否需要升级代码版本。", code);
        return null;
    }
}
