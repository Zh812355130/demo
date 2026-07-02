package org.huan.demo.demo.lambda;

public class Demo {

    public static boolean test(Runnable runnable) {
        runnable.run();
        System.out.println("test running");
        return false;
    }

    public static void say() {
        System.out.println("hello");
    }

    public static void main(String[] args) {
        System.out.println(test(Demo::say));
    }
}
