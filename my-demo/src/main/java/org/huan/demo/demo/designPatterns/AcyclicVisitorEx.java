package org.huan.demo.demo.designPatterns;

public class AcyclicVisitorEx {
    public static void main(String[] args) {
        //非循环访问者允许将功能添加到现有的类层次结构中，而无需修改层次结构
        ConfigureForUnixVisitor conUnix = new ConfigureForUnixVisitor();
        ConfigureForDosVisitor conDos = new ConfigureForDosVisitor();
        Zoom zoom = new Zoom();
        Hayes hayes = new Hayes();

        hayes.accept(conDos);
        zoom.accept(conDos);
        hayes.accept(conUnix);
        zoom.accept(conUnix);

    }
}

interface ModemVisitor {}

interface HayesVisitor extends ModemVisitor {
    void visit(Hayes hayes);
}
interface Modem {
    void accept(ModemVisitor modemVisitor);
}

class Hayes implements Modem {

    @Override
    public void accept(ModemVisitor modemVisitor) {
        if (modemVisitor instanceof HayesVisitor) {
            ((HayesVisitor) modemVisitor).visit(this);
        } else {
            System.out.println("Only HayesVisitor is allowed to visit Hayes modem");
        }
    }

    @Override
    public String toString() {
        return "Hayes modem";
    }
}

interface ZoomVisitor extends ModemVisitor {
    void visit(Zoom modem);
}

class Zoom implements Modem {

    @Override
    public void accept(ModemVisitor modemVisitor) {
        if (modemVisitor instanceof ZoomVisitor) {
            ((ZoomVisitor) modemVisitor).visit(this);
        } else {
            System.out.println("Only ZoomVisitor is allowed to visit Zoom modem");
        }
    }

    @Override
    public String toString() {
        return "Zoom modem";
    }
}

interface AllModemVisitor extends ZoomVisitor, HayesVisitor {

}

class ConfigureForDosVisitor implements AllModemVisitor {

    @Override
    public void visit(Hayes hayes) {
        System.out.println(hayes + " used with Dos configurator.");
    }

    @Override
    public void visit(Zoom zoom) {
        System.out.println(zoom + " used with Dos configurator.");
    }
}

class ConfigureForUnixVisitor implements ZoomVisitor {

    @Override
    public void visit(Zoom zoom) {
        System.out.println(zoom + " used with Unix configurator.");
    }
}