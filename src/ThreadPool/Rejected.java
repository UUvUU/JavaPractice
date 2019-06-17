package com.example.demo1.Thread2;

import java.util.concurrent.ExecutorService;

/**
 * Title: Rejected <br>
 * Description: Rejected <br>
 * Date: 2019年06月12日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
public interface Rejected {
    public void rejectedExecution(Runnable r, ExecutorService executor);
}
