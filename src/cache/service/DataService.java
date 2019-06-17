package com.example.demo1.cache.service;

import com.example.demo1.cache.model.ResultBean;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Title: DataService <br>
 * Description: DataService <br>
 * Date: 2019年06月17日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
@Service
public class DataService {

    public ResultBean<String> getStringData(){

        return ResultBean.ok("ssss"+new Random().nextInt(999));

    }

    public ResultBean<Integer> getIntData(){

        return ResultBean.ok(new Random().nextInt(999));

    }
}
