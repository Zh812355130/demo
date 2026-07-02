package org.huan.demo;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public class SimpleStream<T> {


    private final Collection<T> collection;

    public SimpleStream(Collection<T> collection) {
        this.collection = collection;
    }

    public static <T> SimpleStream<T> of(Collection<T> collection) {
        return new SimpleStream<>(collection);
    }

    public SimpleStream<T> filter(Predicate<T> predicate) {
        List<T> result = new ArrayList<>();
        for (T t : collection) {
            if (predicate.test(t)) {
                result.add(t);
            }
        }
        return new SimpleStream<>(result);
    }

    public <R> SimpleStream<R> map(Function<T, R> function) {
        List<R> result = new ArrayList<>();
        for (T t : collection) {
            R apply = function.apply(t);
            result.add(apply);
        }
        return new SimpleStream<>(result);
    }

    public void forEach(Consumer<T> consumer) {
        for (T t : collection) {
            consumer.accept(t);
        }
    }

    //化简
    public T reduce(T init, BinaryOperator<T> operator) {
        T pre = init;
        for (T t : collection) {
            pre = operator.apply(pre, t);
        }
        return pre;
    }

    //收集
    public <K> K collect(Supplier<K> supplier, BiConsumer<K, T> consumer) {
        K c = supplier.get();
        for (T t : collection) {
            consumer.accept(c, t);
        }
        return c;
    }

    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4, 5);
        SimpleStream.of(list)
                .filter(x -> (x & 1) == 1)
                .map(x -> x * x)
                .forEach(System.out::println);
        System.out.println("--------------reduce-----------");
        System.out.println(SimpleStream.of(list).reduce(0, Integer::sum));
        System.out.println(SimpleStream.of(list).reduce(Integer.MAX_VALUE, Integer::min));
        System.out.println(SimpleStream.of(list).reduce(Integer.MIN_VALUE, Integer::max));
        System.out.println("--------------collect-----------");
        list = List.of(1, 2, 3, 4, 5, 1, 2, 3);
        HashSet<Integer> collect = SimpleStream.of(list)
                .collect(HashSet::new, HashSet::add);
        System.out.println(collect);
        StringBuilder sb = SimpleStream.of(list)
                .collect(StringBuilder::new, StringBuilder::append);
        System.out.println(sb);
        StringJoiner sj = SimpleStream.of(list)
                .map(String::valueOf)
                .collect(() -> new StringJoiner("-"), StringJoiner::add);
        System.out.println(sj);
        Map<Integer, AtomicInteger> map= SimpleStream
                                        .of(list)
                                        .collect(HashMap::new,
                                                (m, t) -> m.computeIfAbsent(t, x -> new AtomicInteger()).getAndIncrement());
        System.out.println(map);
    }

}
