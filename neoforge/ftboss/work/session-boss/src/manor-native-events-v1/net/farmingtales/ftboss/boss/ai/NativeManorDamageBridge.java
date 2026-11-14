package net.farmingtales.ftboss.boss.ai;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.event.NativeManorDamageEvents;
import net.farmingtales.ftboss.event.NativeWeaponDamageEvent.BeforeHealth;
import net.farmingtales.ftboss.event.NativeWeaponDamagePhases;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
public final class NativeManorDamageBridge {
    private static final ThreadLocal<Deque<Frame>> FRAMES=new ThreadLocal<>();
    private static final class Frame {
        final EasterBossEntity boss;
        final DamageSource source;
        final DamageContainer container;
        final Stack<DamageContainer> stack;
        final int callerDepth;
        boolean incoming,pre,healthEvent,post;
        Frame(EasterBossEntity boss,DamageSource source,float amount) {
            this.boss=boss;this.source=source;this.container=new DamageContainer(source,amount);
            this.container.setPostAttackInvulnerabilityTicks(10);
            this.stack=((NativeManorDamageScopeAccess)boss).ftboss$manorDamageContainers();
            this.callerDepth=stack.size();
        }
    }
    private NativeManorDamageBridge() {}
    private static Frame current(LivingEntity boss,DamageSource source) {
        Deque<Frame> frames=FRAMES.get();Frame frame=frames==null?null:frames.peek();
        if(frame==null||frame.boss!=boss||frame.source!=source)throw new IllegalStateException("Missing exact owned Manor damage invocation");
        return frame;
    }
    public static void begin(EasterBossEntity boss,DamageSource source,float amount) {
        Frame frame=new Frame(boss,source,amount);Deque<Frame> frames=FRAMES.get();
        if(frames==null){frames=new ArrayDeque<>();FRAMES.set(frames);}
        frame.stack.push(frame.container);frames.push(frame);
    }
    public static void finish(EasterBossEntity boss,DamageSource source) {
        Frame frame=current(boss,source);Deque<Frame> frames=FRAMES.get();
        if(frame.stack.size()!=frame.callerDepth+1||frame.stack.peek()!=frame.container)throw new IllegalStateException("Foreign Manor damage stack mutation");
        frame.stack.pop();frames.pop();if(frames.isEmpty())FRAMES.remove();
    }
    public static void abort(Throwable original,EasterBossEntity boss,DamageSource source) {
        try{finish(boss,source);}catch(Throwable cleanup){if(cleanup!=original)original.addSuppressed(cleanup);}
    }
    public static DamageContainer reductionContainer(EasterBossEntity boss,DamageSource source,float amount) {
        Frame frame=current(boss,source);frame.container.setNewDamage(amount);return frame.container;
    }
    public static float incoming(LivingEntity boss,DamageSource source,float amount) {
        Frame frame=current(boss,source);
        if(frame.incoming)throw new IllegalStateException("Duplicate native incoming Manor phase");frame.incoming=true;
        LivingIncomingDamageEvent event=new LivingIncomingDamageEvent(boss,frame.container);NeoForge.EVENT_BUS.post(event);
        if(event.isCanceled()){frame.container.setNewDamage(0.0F);return 0.0F;}
        NativeWeaponDamagePhases.beforeMitigation(boss,frame.container);return frame.container.getNewDamage();
    }
    public static float armorResult(EasterBossEntity boss,DamageSource source,float amount) {
        Frame frame=current(boss,source);frame.container.setReduction(Reduction.ARMOR,frame.container.getNewDamage()-amount);return frame.container.getNewDamage();
    }
    public static float magicResult(EasterBossEntity boss,DamageSource source,float amount) {
        return current(boss,source).container.getNewDamage();
    }
    public static float pre(LivingEntity boss,DamageSource source,float amount) {
        Frame frame=current(boss,source);if(!frame.incoming||frame.pre)throw new IllegalStateException("Changed native Manor pre phase ordering");
        frame.pre=true;frame.container.setNewDamage(amount);return CommonHooks.onLivingDamagePre(boss,frame.container);
    }
    public static float absorption(LivingEntity boss,DamageSource source,float amount) {
        Frame frame=current(boss,source);if(!frame.pre)throw new IllegalStateException("Absorption precedes native Manor pre phase");
        frame.container.setReduction(Reduction.ABSORPTION,amount);return Math.min(boss.getAbsorptionAmount(),frame.container.getReduction(Reduction.ABSORPTION));
    }
    public static BeforeHealth damageEvent(LivingEntity boss,DamageSource source,float amount) {
        Frame frame=current(boss,source);if(!frame.pre||frame.healthEvent)throw new IllegalStateException("Changed original Manor before-health ordering");
        frame.healthEvent=true;frame.container.setNewDamage(amount);return net.farmingtales.ftboss.event.NativeManorDamageEventFactory.create(boss,frame.container);
    }
    public static boolean post(BeforeHealth event) {
        Frame frame=current(event.getEntity(),event.getSource());if(!frame.healthEvent||frame.post)throw new IllegalStateException("Changed original Manor mutable phase");
        return NativeManorDamageEvents.post(event);
    }
    public static void timing(EasterBossEntity boss,DamageSource source) {
        Frame frame=current(boss,source);boss.invulnerableTime=frame.container.getPostAttackInvulnerabilityTicks();
    }
    public static void applied(EasterBossEntity boss,DamageSource source) {
        Frame frame=current(boss,source);if(!frame.healthEvent||frame.post)throw new IllegalStateException("Native Manor post lacks actual unique health write");
        frame.post=true;CommonHooks.onLivingDamagePost(boss,frame.container);
    }
}
