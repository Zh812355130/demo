package org.huan.demo.demo.designPatterns;

public class AdapterEx {
    /**
     * 适配器模式：把不兼容的对象包在适配器中，以让其兼容其他类
     */
    public static void main(String[] args) {
        //Captain 需要使用FishingBoat的sail功能
        FishingBoatAdapter adapter = new FishingBoatAdapter();
        Captain captain = new Captain(adapter);
        captain.row();
    }
}

interface RowingBoat {
    void row();
}

class FishingBoat {
    public void sail() {
        System.out.println("The fishing boat is sailing");
    }
}

class Captain {
    private final RowingBoat rowingBoat;

    Captain(RowingBoat rowingBoat) {
        this.rowingBoat = rowingBoat;
    }

    public void row() {
        rowingBoat.row();
    }
}

class FishingBoatAdapter implements RowingBoat {
    private final FishingBoat fishingBoat;

    FishingBoatAdapter() {
        this.fishingBoat = new FishingBoat();
    }

    @Override
    public void row() {
        fishingBoat.sail();
    }
}