package org.huan.demo.demo.designPatterns;

import lombok.Getter;
import lombok.Setter;

public class AbstractFactoryEx {
    private static  Kingdom kingdom = new Kingdom();
    public static void main(String[] args) {


        /**
         * 抽象工厂模式提供了一种封装一组具有共同主题的单个工厂而无需指定其具体类的方法
         */
//        KingdomFactory factory = new ElfKingdomFactory();
//        Castle castle = factory.createCastle();
//        King king = factory.createKing();
//        Army army = factory.createArmy();
//        System.out.println(castle.getDescription());
//        System.out.println(king.getDescription());
//        System.out.println(army.getDescription());

        createKingdom(Kingdom.FactoryMaker.KingdomType.ELF);
        System.out.println(kingdom.getArmy().getDescription());
        System.out.println(kingdom.getKing().getDescription());
        System.out.println(kingdom.getCastle().getDescription());
        createKingdom(Kingdom.FactoryMaker.KingdomType.ORC);
        System.out.println(kingdom.getArmy().getDescription());
        System.out.println(kingdom.getKing().getDescription());
        System.out.println(kingdom.getCastle().getDescription());

    }


    public static void createKingdom(Kingdom.FactoryMaker.KingdomType type){
        KingdomFactory factory = Kingdom.FactoryMaker.makeFactory(type);
        kingdom.setArmy(factory.createArmy());
        kingdom.setKing(factory.createKing());
        kingdom.setCastle(factory.createCastle());
    }

}

interface Castle {
    String getDescription();
}

interface King {
    String getDescription();
}

interface Army {
    String getDescription();
}

class ElfCastle implements Castle {
    static final String DESCRIPTION = "This is the Elven castle!";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}

class ElfKing implements King {
    static final String DESCRIPTION = "This is the Elven king!";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}

class ElfArmy implements Army {
    static final String DESCRIPTION = "This is Elven Army!";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}


class OrcCastle implements Castle {
    static final String DESCRIPTION = "This is the orc castle!";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}

class OrcKing implements King {
    static final String DESCRIPTION = "This is the orc king!";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}

class OrcArmy implements Army {
    static final String DESCRIPTION = "This is orc Army!";

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}

interface KingdomFactory {
    Castle createCastle();

    King createKing();

    Army createArmy();
}

class ElfKingdomFactory implements KingdomFactory {

    @Override
    public Castle createCastle() {
        return new ElfCastle();
    }

    @Override
    public King createKing() {
        return new ElfKing();
    }

    @Override
    public Army createArmy() {
        return new ElfArmy();
    }
}

class OrcKingdomFactory implements KingdomFactory {

    @Override
    public Castle createCastle() {
        return new OrcCastle();
    }

    @Override
    public King createKing() {
        return new OrcKing();
    }

    @Override
    public Army createArmy() {
        return new OrcArmy();
    }
}

@Getter
@Setter
class Kingdom {
    private King king;
    private Castle castle;
    private Army army;

    public static class FactoryMaker {
        public enum KingdomType {
            ELF, ORC
        }

        public static KingdomFactory makeFactory(KingdomType type) {
            if(type == KingdomType.ELF){
                return new ElfKingdomFactory();
            }else if(type==KingdomType.ORC){
                return new OrcKingdomFactory();
            }
            return null;
        }
    }

}






