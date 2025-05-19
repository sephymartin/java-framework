package top.sephy.infra.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CommonResult<T> {

    private static CommonResult<Void> SUCCESS = success(null);

    public static final int CODE_SUCCESS = 0;

    public static final int CODE_ERROR = 1;

    private int code;

    private String error;

    private String message;

    private long timestamp;

    private T data;

    private CommonResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static CommonResult<Void> success() {
        return SUCCESS;
    }

    public static <E> CommonResult<E> success(E data) {
        return new CommonResult<>(CODE_SUCCESS, null, data);
    }

    public static <E> CommonResult<E> error(String error, String message) {
        CommonResult<E> result = new CommonResult<>(CODE_ERROR, message, null);
        result.setError(error);
        return result;

    }
}
