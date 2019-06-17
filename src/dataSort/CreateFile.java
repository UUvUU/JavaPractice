package com.example.demo1.bigfile;

import java.io.*;
import java.util.Random;
import java.util.concurrent.Executors;

/**
 * Title: CreateFile <br>
 * Description: CreateFile <br>
 * Date: 2019年06月10日
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
public class CreateFile {
    public static void main(String[] args) {
        File file = new File("./src/data.log");
        Random random = new Random();
        try (BufferedWriter bo=new BufferedWriter(new FileWriter(file))){
            for (int i = 0; i < 10000800; i++) {
                bo.write(random.nextInt(99999999)+"");
                bo.newLine();
                bo.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
