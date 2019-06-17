package com.example.demo1.cache.config;

import com.example.demo1.cache.model.ResultBean;
import com.example.demo1.cache.service.DataService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Title: TimeConfig <br>
 * Description: TimeConfig <br>
 * Date: 2019年06月17日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
@Configuration
@Data
public class TimeConfig {

    public static Map<String, ResultBean> mapData = new HashMap<>();

    @Autowired
    DataService dataService;

    @PostConstruct
    public void initData(){
        TimeUtil.TaskBuilder.builder()
                .build()
                .add(10,()->mapData.put("String",dataService.getStringData()))
                .add(10,()->mapData.put("int",dataService.getIntData())).taskRun();
    }



}
