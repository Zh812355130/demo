package org.huan.demo.demo.designPatterns;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class AbstractDocumentEx {
    /**
     * 使用动态属性，并在保持类型安全的同时实现非类型化语言的灵活性
     * 抽象文档模式 允许在对象不知道的情况下将属性附加到对象 将新属性动态地添加到对象树中
     */
    public static void main(String[] args) {
        Map<String, Object> wheelProperties = Map.of(
                Property.TYPE.toString(), "wheel",
                Property.MODEL.toString(), "15C",
                Property.PRICE.toString(), 100L);
        Map<String, Object> doorProperties = Map.of(
                Property.TYPE.toString(), "door",
                Property.MODEL.toString(), "Lambo",
                Property.PRICE.toString(), 300L);
        Map<String, Object> carProperties = Map.of(
                Property.MODEL.toString(), "300SL",
                Property.PRICE.toString(), 10000L,
                Property.PARTS.toString(),List.of(wheelProperties,doorProperties));

        Car car  = new Car(carProperties);

        System.out.println("model:"+car.getModel().orElseThrow());
        System.out.println("price:"+car.getPrice().orElseThrow());
        System.out.println("parts:");
        car.getParts().forEach(p-> {
            System.out.print(p.getType().orElse(null)+"/");
            System.out.print(p.getModel().orElse(null)+"/");
            System.out.println(p.getPrice().orElse(null));
        });

    }
}

interface Document {
    void put(String key, Object value);

    Object get(String key);

    <T> Stream<T> children(String key, Function<Map<String, Object>, T> constructor);
}

abstract class AbstractDocument implements Document {
    private final Map<String, Object> properties;

    protected AbstractDocument(Map<String, Object> properties) {
        Objects.requireNonNull(properties, "properties map is required");
        this.properties = properties;
    }

    @Override
    public void put(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Object get(String key) {
        return properties.get(key);
    }

    @Override
    public <T> Stream<T> children(String key, Function<Map<String, Object>, T> constructor) {
        return Stream.ofNullable(get(key))
                .filter(Objects::nonNull)
                .map(el -> (List<Map<String, Object>>) el)
                .findAny()
                .stream()
                .flatMap(Collection::stream)
                .map(constructor);
    }
}

enum Property {
    PARTS, TYPE, PRICE, MODEL
}

interface HasType extends Document {
    default Optional<String> getType() {
        return Optional.ofNullable((String) get(Property.TYPE.toString()));
    }
}

interface HasPrice extends Document {
    default Optional<Number> getPrice() {
        return Optional.ofNullable((Number) get(Property.PRICE.toString()));
    }
}

interface HasModel extends Document {
    default Optional<String> getModel() {
        return Optional.ofNullable((String) get(Property.MODEL.toString()));
    }
}

interface HasParts extends Document {
    default Stream<Part> getParts() {
        return children(Property.PARTS.toString(), Part::new);
    }
}

class Part extends AbstractDocument implements HasModel, HasPrice, HasType{

    protected Part(Map<String, Object> properties) {
        super(properties);
    }
}

class Car extends AbstractDocument implements HasModel, HasPrice, HasParts {

    protected Car(Map<String, Object> properties) {
        super(properties);
    }
}



