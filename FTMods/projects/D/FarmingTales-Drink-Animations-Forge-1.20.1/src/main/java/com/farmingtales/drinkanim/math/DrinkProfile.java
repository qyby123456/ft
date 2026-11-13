package com.farmingtales.drinkanim.math;

import java.util.List;

public record DrinkProfile(String id, Style style, float scale, float width, float depth,
    float[] capPivot, float neckJoin, float neckMin, float neckMax, float mouthY,
    String bodyModel, String capModel, String neckModel, float[] boundsMin, float[] boundsMax,
    List<Region> liquid, CanLid canLid, CupShape cupShape) {
    public enum Style { WINE, BEER, BRANDY, CAN, PINK_CAN, WATER, SPORTS, JUICE, CUP, JAM, FOOD }
    public record CupShape(float gripY,float radius,float liquidYaw) {}
    public record CanLid(float[] origin,float yaw,float[] hinge,float[] hole,String flapModel) {}
    public record Region(double[] min, double[] max, float[] sideUV, float[] surfaceUV, String texture) {
        public LiquidMesh.V minimum() {return new LiquidMesh.V(min[0],min[1],min[2]);}
        public LiquidMesh.V maximum() {return new LiquidMesh.V(max[0],max[1],max[2]);}
    }
    public boolean wine() {return style==Style.WINE||style==Style.BEER||style==Style.BRANDY;}
    public boolean cork() {return style==Style.WINE&&!BottleClosures.screw(this);}
    public int liquidAlpha() {return food()||breweryMug()?255:id.startsWith("vinery:")?242:184;}
    public boolean breweryMug() {return switch(id) {
        case "brewery:beer_wheat","brewery:beer_barley","brewery:beer_hops",
             "brewery:beer_nettle","brewery:beer_haley","brewery:beer_oat" -> true;
        default -> false;
    };}
    public boolean pullCap() {return cork()||style==Style.BEER;}
    public boolean can() {return style==Style.CAN||style==Style.PINK_CAN;}
    public boolean cup() {return style==Style.CUP;}
    public boolean jam() {return style==Style.JAM;}
    public boolean food() {return style==Style.FOOD;}
    public boolean sweetJam() {return jam()&&(id.endsWith("_jam_1")||id.equals("farmingtales:chocolate_sauce"));}
    public boolean agedBottle() {return id.startsWith("farmingtales:")&&!cup()&&!jam()&&!food();}
    public float renderScale() {return BottleRig.SCALE*scale;}
}
