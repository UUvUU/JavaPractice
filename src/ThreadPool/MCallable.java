package com.example.demo1.Thread2;

/**
 * Title: MCallable <br>
 * Description: MCallable <br>
 * Date: 2019年06月14日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
public interface MCallable<V> {
    /**
     * 带返回值
     */
    V call();
}
