package com.example.demo1.cache.model;

import lombok.Data;

/**
 * Title: ResultBean <br>
 * Description: ResultBean <br>
 * Date: 2019年06月17日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
@Data
public class ResultBean<T> {

    public ResultBean() {
    }

    private ResultBean(T o) {
        this.o = o;
    }
    private T o;

    public static<T> ResultBean<T> ok(T t){
        return new ResultBean<>(t);
    }
}
