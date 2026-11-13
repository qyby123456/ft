package ftboss.bossprobe;
import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import io.netty.buffer.Unpooled;
public final class LightObservation {
    public record Sample(String family,int kind,int phase,int ownerId,UUID owner,ResourceLocation dimension,long born,Vec3 origin,Vec3 direction,double radius,int ticks,int index,boolean valid){}
    private static final List<Sample> pending=new ArrayList<>();
    public static final List<String> events=new ArrayList<>();
    public static List<Sample> drain(){List<Sample> result=List.copyOf(pending);pending.clear();return result;}
    private static void check(boolean value){if(!value)throw new AssertionError("Native packet codec roundtrip differs");}
    public static void qingyi(net.farmingtales.ftboss.qingyilight.LightPacket packet){
        events.add(packet.toString());if(packet.kind()>2||packet.phase()>1)return;
        FriendlyByteBuf buffer=new FriendlyByteBuf(Unpooled.buffer());try{net.farmingtales.ftboss.qingyilight.LightPacket.STREAM_CODEC.encode(buffer,packet);var decoded=net.farmingtales.ftboss.qingyilight.LightPacket.STREAM_CODEC.decode(buffer);check(decoded.valid()&&decoded.kind()==packet.kind()&&decoded.phase()==packet.phase()&&decoded.ownerId()==packet.ownerId()&&decoded.owner().equals(packet.owner())&&decoded.dimension().equals(packet.dimension())&&decoded.born()==packet.born()&&decoded.origin().distanceToSqr(packet.origin())<0.00000001&&decoded.direction().distanceToSqr(packet.direction())<0.00000001&&decoded.radius()==packet.radius()&&decoded.duration()==packet.duration()&&decoded.pulse()==packet.pulse()&&decoded.entityId()==packet.entityId()&&Objects.equals(decoded.entity(),packet.entity())&&buffer.readableBytes()==0);}finally{buffer.release();}
        pending.add(new Sample("qingyi",packet.kind(),packet.phase(),packet.ownerId(),packet.owner(),packet.dimension(),packet.born(),packet.origin(),packet.direction(),packet.radius(),packet.duration(),packet.pulse(),packet.valid()));
    }
    public static void dandan(net.farmingtales.ftboss.dandannativelights.LightPacket packet){
        events.add(packet.toString());if(packet.kind()>3)return;
        FriendlyByteBuf buffer=new FriendlyByteBuf(Unpooled.buffer());try{net.farmingtales.ftboss.dandannativelights.LightPacket.STREAM_CODEC.encode(buffer,packet);var decoded=net.farmingtales.ftboss.dandannativelights.LightPacket.STREAM_CODEC.decode(buffer);check(decoded.valid()&&decoded.kind()==packet.kind()&&decoded.ownerId()==packet.ownerId()&&decoded.owner().equals(packet.owner())&&decoded.dimension().equals(packet.dimension())&&decoded.origin().distanceToSqr(packet.origin())<0.00000001&&decoded.yaw()==packet.yaw()&&decoded.radius()==packet.radius()&&decoded.ticks()==packet.ticks()&&decoded.index()==packet.index()&&buffer.readableBytes()==0);}finally{buffer.release();}
        pending.add(new Sample("dandan",packet.kind(),packet.kind()%2,packet.ownerId(),packet.owner(),packet.dimension(),-1,packet.origin(),new Vec3(Math.sin(packet.yaw()),0,Math.cos(packet.yaw())),packet.radius(),packet.ticks(),packet.index(),packet.valid()));
    }
    public static void shadow(net.farmingtales.ftboss.zheergenfx.LightPacket packet){
        events.add(packet.toString());if(packet.kind()>3)return;
        FriendlyByteBuf buffer=new FriendlyByteBuf(Unpooled.buffer());try{net.farmingtales.ftboss.zheergenfx.LightPacket.STREAM_CODEC.encode(buffer,packet);var decoded=net.farmingtales.ftboss.zheergenfx.LightPacket.STREAM_CODEC.decode(buffer);check(decoded.valid()&&decoded.kind()==packet.kind()&&decoded.ownerId()==packet.ownerId()&&decoded.owner().equals(packet.owner())&&decoded.dimension().equals(packet.dimension())&&decoded.born()==packet.born()&&decoded.origin().distanceToSqr(packet.origin())<0.00000001&&decoded.direction().distanceToSqr(packet.direction())<0.00000001&&decoded.radius()==packet.radius()&&decoded.duration()==packet.duration()&&decoded.index()==packet.index()&&buffer.readableBytes()==0);}finally{buffer.release();}
        pending.add(new Sample("shadow",packet.kind(),packet.kind()%2,packet.ownerId(),packet.owner(),packet.dimension(),packet.born(),packet.origin(),packet.direction(),packet.radius(),packet.duration(),packet.index(),packet.valid()));
    }
}
