package com.farmingtales.drinkanim.math;

import java.util.Random;

/** Select once on action start; each bag visits all styles without an immediate repeat. */
public final class EatingVariants {
    private final Random random;
    private final int[] bag=new int[EatingMotion.VARIANTS];
    private int cursor=bag.length,last=-1;
    public EatingVariants(Random random) {this.random=random;}
    public int next() {
        if(cursor==bag.length) {
            for(int i=0;i<bag.length;i++)bag[i]=i;
            for(int i=bag.length-1;i>0;i--)swap(i,random.nextInt(i+1));
            if(bag[0]==last)swap(0,1+random.nextInt(bag.length-1));
            cursor=0;
        }
        return last=bag[cursor++];
    }
    private void swap(int a,int b) {int old=bag[a];bag[a]=bag[b];bag[b]=old;}
}
