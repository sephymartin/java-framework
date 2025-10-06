package top.sephy.infra.mybatis.exception;

import top.sephy.infra.exception.SystemException;

public class DataUpdateLimitationException extends SystemException {

    public DataUpdateLimitationException(String message) {
        super(message);
    }

    public static DataUpdateLimitationException DEFAULT =
        new DataUpdateLimitationException("本次操作 因超过系统安全阈值 被拦截，如需继续，请联系管理员!");
}
