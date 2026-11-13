package com.farmingtales.drinkanim.math;

/** Project liquid faces onto their dominant plane, including horizontal shoulders. */
public final class LiquidUV {
    public record UV(float u,float v) {}
    public static UV sample(DrinkProfile.Region region,LiquidMesh.Face face,LiquidMesh.V point) {
        var normal=face.normal();
        double x=fraction(point.x(),region.min()[0],region.max()[0]);
        double y=1-fraction(point.y(),region.min()[1],region.max()[1]);
        double z=fraction(point.z(),region.min()[2],region.max()[2]);
        boolean horizontal=Math.abs(normal.y())>=Math.max(Math.abs(normal.x()),Math.abs(normal.z()));
        double u=horizontal?x:Math.abs(normal.x())>Math.abs(normal.z())?z:x;
        double v=horizontal?z:y;
        float[] uv=face.surface()||horizontal?region.surfaceUV():region.sideUV();
        return new UV((float)(uv[0]+(uv[2]-uv[0])*u)/16,(float)(uv[1]+(uv[3]-uv[1])*v)/16);
    }
    private static double fraction(double value,double min,double max) {return Math.max(0,Math.min(1,(value-min)/(max-min)));}
    private LiquidUV() {}
}
