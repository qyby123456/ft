package com.farmingtales.drinkanim.math;

import com.farmingtales.drinkanim.math.BottleMotion.Pose;

/** C2 poses with shared velocity/acceleration at interior keys. */
public final class PoseCurve {
    public record Key(double time,Pose pose) {}
    private final Key[] keys;
    private final double[][] points,velocity,acceleration;
    public PoseCurve(Key... keys) {
        this.keys=keys;points=new double[keys.length][];velocity=new double[keys.length][7];acceleration=new double[keys.length][7];
        for(int i=0;i<keys.length;i++) {
            Pose p=keys[i].pose();points[i]=new double[]{p.x(),p.y(),p.z(),p.pitch(),p.yaw(),p.roll(),p.fill()};
            if(i>0&&keys[i].time()<=keys[i-1].time())throw new IllegalArgumentException("Key times must increase");
        }
        for(int i=1;i<keys.length-1;i++)for(int j=0;j<7;j++) {
            double h0=keys[i].time()-keys[i-1].time(),h1=keys[i+1].time()-keys[i].time();
            double d0=(points[i][j]-points[i-1][j])/h0,d1=(points[i+1][j]-points[i][j])/h1;
            if(d0*d1>0)velocity[i][j]=3*(h0+h1)/((2*h1+h0)/d0+(h1+2*h0)/d1);
            if(d0!=0&&d1!=0)acceleration[i][j]=2*(d1-d0)/(h0+h1);
        }
    }
    public Pose sample(double time) {
        if(time<=keys[0].time())return keys[0].pose();
        int b=1;while(b<keys.length&&time>=keys[b].time())b++;
        if(b==keys.length)return keys[b-1].pose();
        int a=b-1;double h=keys[b].time()-keys[a].time(),u=(time-keys[a].time())/h;
        double u2=u*u,u3=u2*u,u4=u3*u,u5=u4*u;double[] v=new double[7];
        for(int j=0;j<7;j++)v[j]=(1-10*u3+15*u4-6*u5)*points[a][j]+(10*u3-15*u4+6*u5)*points[b][j]
            +(u-6*u3+8*u4-3*u5)*h*velocity[a][j]+(-4*u3+7*u4-3*u5)*h*velocity[b][j]
            +.5*(u2-3*u3+3*u4-u5)*h*h*acceleration[a][j]+.5*(u3-2*u4+u5)*h*h*acceleration[b][j];
        return new Pose(v[0],v[1],v[2],v[3],v[4],v[5],v[6]);
    }
}
