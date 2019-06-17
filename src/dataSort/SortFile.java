package com.example.demo1.bigfile;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Title: SortFile <br>
 * Description: SortFile <br>
 * Date: 2019年06月10日
 * <p>
 * 1.大文件切割成小文件并排序
 * 2.多个小文件合并同时排序
 *
 * @author 徐奥
 * @version 1.0.0
 * @since jdk8
 */
public class SortFile {
    public static List<File> margeList = new ArrayList<>();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        File file = new File("./src/data.log");
        split2Sort(file);
        marge();
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000 + "s");
    }
    /**
     * 合并主流程
     */
    private static void marge() {
        ThreadPoolExecutor singleThreadPool = new ThreadPoolExecutor(32, 32, 10, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(5), new ThreadPoolExecutor.CallerRunsPolicy());
        int y = 0;
        File[] files = new File("./src/sort").listFiles();
        File[] files2 = new File("./src/marge").listFiles();
        while (files2.length >= 2 || margeList.size() >= 2 || files.length > 0) {
            if (margeList.size() >= 2) {
                for (int i = 0; i < margeList.size(); ) {
                    //存放排序文件集合
                    List<File> list = new ArrayList<>();
                    //四个一组放入合并
                    a: for (int j = 0; j < 4; j++) {
                        if (i < margeList.size()) {
                            list.add(margeList.get(i));
                            i++;
                        } else {
                            break;
                        }
                    }
                    //两个以上进行合并排序
                    if (list.size() >= 2) {
                        for (File file : list) {
                            margeList.remove(file);
                        }
                        y++;
                        int finalY = y;
                        singleThreadPool.execute(() -> {
                            margeFile(list, finalY);
                        });
                    }
                }
            }
            files2 = new File("./src/marge").listFiles();
            files = new File("./src/sort").listFiles();
        }

        singleThreadPool.shutdown();
    }


    /**
     * 合并文件
     *
     * @param files 需要合并的文件集合
     * @param y     要合成的文件名
     */
    private static void margeFile(List<File> files, int y) {
        File t = new File("./src/marge/data.log" + y);
        System.out.println(files);
        int j = 0;
        Map<String, BufferedReader> brMap = new HashMap<>(files.size());
        for (File file : files) {
            try {
                brMap.put((j++) + "", new BufferedReader(new FileReader(file)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(t))) {
            //初始化
            ArrayList<String> list = new ArrayList<>();
            for (String integer : brMap.keySet()) {
                String s = brMap.get(integer).readLine();
                //key:value   key是reader的序号 value是读取到当前的值
                list.add(integer + ":" + s);
            }
            while (!CollectionUtils.isEmpty(brMap)) {
                sortMargeList(list);
                String[] split = list.get(0).split(":");
                bw.write(split[1]);
                bw.newLine();
                list.remove(0);
                String s = brMap.get(split[0]).readLine();
                if (!StringUtils.isEmpty(s)) {
                    list.add(split[0] + ":" + s);
                } else {
                    brMap.get(split[0]).close();
                    brMap.remove(split[0]);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        for (File file : files) {
            file.delete();
        }
        margeList.add(t);

    }

    /**
     * 对列表排序重写
     *
     * @param list list
     */
    private static void sortMargeList(ArrayList<String> list) {
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int i = Integer.parseInt(o1.split(":")[1]);
                int j = Integer.parseInt(o2.split(":")[1]);
                if (i < j) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
    }

    /**
     * 切割排序
     *
     * @param file 源文件
     */
    public static void split2Sort(File file) {
        int j = 0;
        int i = 0;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(32, 32, 10,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5),
                new ThreadPoolExecutor.CallerRunsPolicy());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = null;
            List<Integer> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                //一万条输出一次文件
                if (i < 10000) {
                    i++;
                    list.add(Integer.parseInt(line));
                } else {
                    List<Integer> finalList = list;
                    int finalJ = j;
                    executor.execute(() -> writeTmpFile(finalJ, finalList));
                    j++;
                    i = 0;
                    list = new ArrayList<>();
                }
            }
            //最后一次多余的输出文件
            if (!CollectionUtils.isEmpty(list)) {
                writeTmpFile(j, list);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        executor.shutdown();

    }

    /**
     * 写到零时文件(切割排序后的)
     *
     * @param j    文件名
     * @param list 内容
     */
    private static void writeTmpFile(int j, List<Integer> list) {
        Collections.sort(list);
        File file = new File("./src/sort/data" + (j++) + ".log");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Integer in : list) {
                bw.write(in + "");
                bw.newLine();
                bw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        margeList.add(file);
    }
}
