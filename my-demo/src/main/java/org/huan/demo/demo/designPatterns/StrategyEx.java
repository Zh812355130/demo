package org.huan.demo.demo.designPatterns;

public class StrategyEx {
    /**
     * 策略模式：运行时选择最匹配的算法
     */
    public static void main(String[] args) {
        //更换不同策略
        DragonSlayer dragonSlayer = new DragonSlayer(new MelleStrategy());
        dragonSlayer.goToBattle();
        dragonSlayer.changeStrategy(new ProjectileStrategy());
        dragonSlayer.goToBattle();
        dragonSlayer.changeStrategy(new SpellStrategy());
        dragonSlayer.goToBattle();
    }
}

class DragonSlayer{
    private DragonSlayingStrategy strategy;

    public DragonSlayer(DragonSlayingStrategy strategy) {
        this.strategy = strategy;
    }

    public void changeStrategy(DragonSlayingStrategy strategy){
        this.strategy = strategy;
    }
    public void goToBattle(){
        strategy.execute();
    }

}



@FunctionalInterface
interface DragonSlayingStrategy{
    void execute();
}

class MelleStrategy implements DragonSlayingStrategy{

    @Override
    public void execute() {
        System.out.println("MelleStrategy");
    }
}

class ProjectileStrategy implements DragonSlayingStrategy{

    @Override
    public void execute() {
        System.out.println("ProjectileStrategy");
    }
}

class SpellStrategy implements DragonSlayingStrategy{

    @Override
    public void execute() {
        System.out.println("SpellStrategy");
    }
}


