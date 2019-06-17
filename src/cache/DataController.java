package com.example.demo1.cache;

import com.example.demo1.cache.config.TimeConfig;
import com.example.demo1.cache.model.ResultBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Title: DataController <br>
 * Description: DataController <br>
 * Date: 2019年06月17日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
@RestController
public class DataController {



    @GetMapping("/test/s")
    public ResultBean getS(){

        return TimeConfig.mapData.get("String");
    }
    @GetMapping("test/i")
    public ResultBean getI(){

        return TimeConfig.mapData.get("int");
    }
}
