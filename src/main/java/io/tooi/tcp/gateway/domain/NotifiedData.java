package io.tooi.tcp.gateway.domain;

import lombok.Data;

/**
 * 通知数据
 *
 * @author Tooi
 * @since 2021-02-14 17:34:24
 */
@Data
public class NotifiedData<T> {

    private T data;

    public NotifiedData(T t) {
        data = t;
    }
}
