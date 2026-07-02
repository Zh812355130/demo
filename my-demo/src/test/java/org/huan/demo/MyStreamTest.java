package org.huan.demo;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
public class MyStreamTest {
    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

    static class MyQueue<E> extends PriorityQueue<E> {
        private final int max;

        public MyQueue(Comparator<? super E> comparator, int max) {
            super(comparator);
            this.max = max;
        }

        @Override
        public boolean offer(E e) {
            boolean offer = super.offer(e);
            if (this.size() > max) {
                this.poll();
            }
            return offer;
        }
    }

    static String firstCategory(String[] array) {
        String c = array[5];
        int idx = c.indexOf(".");
        return c.substring(0, idx);
    }

    static String priceRange(Double price) {
        if (price < 100) {
            return "[0,100)";
        } else if (price >= 100 && price < 500) {
            return "[100,500)";
        } else if (price >= 500 && price < 1000) {
            return "[500,1000)";
        } else {
            return "[1000,∞)";
        }
    }

    static String ageRange(String[] arr) {
        int age = Double.valueOf(arr[9]).intValue();
        if (age < 18) {
            return "[0,18)";
        } else if (age < 30) {
            return "[18,30)";
        } else if (age < 50) {
            return "[30,50)";
        } else {
            return "[50,∞)";
        }
    }


    //data.txt
    // no,event_time,order_id,product_id,category_id,
    // category_code,brand,price,user_id,age,sex,local
    public static void main(String[] args) throws IOException {
        log.info("------开始执行------");
        //runAsync 无返回值  supplyAsync 有返回值 thenAccept-消费结果  thenApply-转换结果
        CompletableFuture.supplyAsync(MyStreamTest::monthlySalesReport)
                .thenAccept(map -> {
                    for (Map.Entry<YearMonth, Long> e : map.entrySet()) {
                        log.info(e.toString());
                    }
                });
        log.info("------执行其他操作------");
        System.in.read();
    }

    public static TreeMap<YearMonth, Long> monthlySalesReport() {
        try (Stream<String> lines = Files.lines(Path.of("./data.txt"))) {
            TreeMap<YearMonth, Long> collect = lines.skip(1)
                    .map(line -> line.split(","))
                    .collect(groupingBy(array ->
                                    YearMonth.from(df.parse(array[1])),
                            TreeMap::new, counting()));
            return collect;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
