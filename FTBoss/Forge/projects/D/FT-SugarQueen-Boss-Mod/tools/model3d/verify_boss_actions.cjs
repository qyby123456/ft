const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const { spawnSync } = require('child_process');
const root = path.resolve(__dirname, '../..');
const output = path.resolve(process.argv[2] || path.join(root, 'build/boss-action-check'));
fs.mkdirSync(output, { recursive: true });
const javaRoot = path.join(root, 'src/main/java/net/farmingtales/ftboss');
const resourceRoot = path.join(root, 'src/main/resources/assets/ftboss');
const entities = ['AppleBoss', 'CompostBoss', 'Benger', 'DefectCore', 'WineBarrelBoss',
  'MeatMountain', 'SugarQueen', 'TeaTreeBoss', 'RatBoss'];
for (const entity of entities) {
  const source = fs.readFileSync(path.join(javaRoot, `entity/${entity}Entity.java`), 'utf8');
  assert.match(source, /entityData\.define\(DATA_ACTION,/);
  assert.match(source, /BossActionState\(this, DATA_ACTION\)/);
  assert.match(source, /super\.defineSynchedData\(\)/);
}
const animations = file => JSON.parse(fs.readFileSync(path.join(resourceRoot, `animations/${file}.animation.json`), 'utf8')).animations;
const rat = animations('rat');
assert.equal(rat['animation.rat.bite'].animation_length * 20, 10);
assert.equal(rat['animation.rat.call'].animation_length * 20, 18);
const barrel = animations('wine_barrel_boss');
assert.equal(barrel['animation.barrel_boss.slam'].animation_length * 20, 24);
assert.equal(barrel['animation.barrel_boss.spray_attack'].animation_length * 20, 50);
assert.deepEqual(barrel['animation.barrel_boss.slam'].bones.root.position['0.5'], [0, 0, 0]);
const stubs = {
  'net/minecraft/nbt/CompoundTag.java': `package net.minecraft.nbt;
public class CompoundTag {
  private final java.util.Map<String,Object> data = new java.util.HashMap<>();
  public void putString(String key, String value) { data.put(key,value); }
  public String getString(String key) { return (String)data.getOrDefault(key, ""); }
  public void putInt(String key, int value) { data.put(key,value); }
  public int getInt(String key) { return (int)data.getOrDefault(key, 0); }
  public void putLong(String key, long value) { data.put(key,value); }
  public long getLong(String key) { return (long)data.getOrDefault(key, 0L); }
}`,
  'net/minecraft/network/syncher/EntityDataAccessor.java': `package net.minecraft.network.syncher;
public class EntityDataAccessor<T> { }`,
  'net/minecraft/network/syncher/SynchedEntityData.java': `package net.minecraft.network.syncher;
public class SynchedEntityData {
  public net.minecraft.nbt.CompoundTag value = new net.minecraft.nbt.CompoundTag();
  public int updates;
  public net.minecraft.nbt.CompoundTag get(EntityDataAccessor<net.minecraft.nbt.CompoundTag> key) { return value; }
  public void set(EntityDataAccessor<net.minecraft.nbt.CompoundTag> key, net.minecraft.nbt.CompoundTag next) { value=next; updates++; }
}`,
  'net/minecraft/world/level/Level.java': `package net.minecraft.world.level;
public class Level { public boolean isClientSide; public long time; public long getGameTime() { return time; } }`,
  'net/minecraft/server/level/ServerLevel.java': `package net.minecraft.server.level;
public class ServerLevel extends net.minecraft.world.level.Level {
  public int particles, sounds;
  public void playSound(Object player, Object position, Object sound, Object category, float volume, float pitch) { sounds++; }
  public void sendParticles(Object kind, double posX, double posY, double posZ, int count,
      double width, double height, double depth, double speed) { particles+=count; }
}`,
  'net/minecraft/world/entity/Entity.java': `package net.minecraft.world.entity;
public class Entity { }`,
  'net/minecraft/world/entity/LivingEntity.java': `package net.minecraft.world.entity;
public class LivingEntity extends Entity {
  public net.minecraft.world.level.Level world;
  public boolean alive=true, removed=false, creative=false;
  public final net.minecraft.network.syncher.SynchedEntityData data = new net.minecraft.network.syncher.SynchedEntityData();
  public net.minecraft.world.level.Level level() { return world; }
  public boolean isAlive() { return alive; }
  public boolean isRemoved() { return removed; }
  public net.minecraft.network.syncher.SynchedEntityData getEntityData() { return data; }
  public double getX() { return 0; } public double getY() { return 0; } public double getZ() { return 0; }
  public float getBbHeight() { return 2; }
}`,
  'net/minecraft/world/entity/Mob.java': `package net.minecraft.world.entity;
public class Mob extends LivingEntity {
  public double distance=1; public boolean visible=true;
  public double distanceToSqr(Entity target) { return distance*distance; }
  public boolean hasLineOfSight(Entity target) { return visible; }
  public Object blockPosition() { return null; }
  public Look getLookControl() { return new Look(); }
  public static class Look { public void setLookAt(Entity target, float yaw, float pitch) { } }
}`,
  'net/minecraft/core/particles/ParticleTypes.java': `package net.minecraft.core.particles;
public class ParticleTypes { public static final Object CRIT = new Object(); }`,
  'net/minecraft/sounds/SoundEvents.java': `package net.minecraft.sounds;
public class SoundEvents { public static final Object PLAYER_ATTACK_SWEEP = new Object(); }`,
  'net/minecraft/sounds/SoundSource.java': `package net.minecraft.sounds;
public class SoundSource { public static final Object HOSTILE = new Object(); }`,
  'software/bernie/geckolib/core/animation/AnimationState.java': `package software.bernie.geckolib.core.animation;
public class AnimationState<T> {
  public final Controller controller = new Controller();
  public Controller getController() { return controller; }
  public static class Controller { public int resets, transition;
    public void forceAnimationReset() { resets++; }
    public void setTransitionLength(int ticks) { transition=ticks; }
  }
}`,
  'net/farmingtales/ftboss/boss/ai/BossTargets.java': `package net.farmingtales.ftboss.boss.ai;
public class BossTargets { public static boolean valid(net.minecraft.world.entity.LivingEntity target) { return !target.creative; } }`,
  'BossActionContractCheck.java': `import net.farmingtales.ftboss.boss.ai.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import software.bernie.geckolib.core.animation.AnimationState;
public class BossActionContractCheck {
  private static int checks;
  private static void check(boolean condition, String label) {
    checks++;
    if (!condition) throw new AssertionError(label);
  }
  public static void main(String[] arguments) {
    ServerLevel server = new ServerLevel(); server.time=100;
    Mob owner = new Mob(); owner.world=server;
    EntityDataAccessor<CompoundTag> key = new EntityDataAccessor<>();
    BossActionState action = new BossActionState(owner,key);
    check(!action.active(), "empty state");
    action.start("melee",18);
    check(action.active() && action.elapsed(0)==0, "start");
    CompoundTag first=owner.data.value;
    AnimationState<Object> animation = new AnimationState<>();
    action.prepare(animation); action.prepare(animation);
    check(animation.controller.resets==1 && animation.controller.transition==0, "no frame-dependent restart");
    server.time=108;
    check(action.elapsed(0.5f)==8.5f, "partial tick");
    for (int frame=0; frame<240; frame++) action.prepare(animation);
    check(action.elapsed(0)==8 && animation.controller.resets==1, "render rate independent");
    server.time=118;
    check(!action.active() && action.name().isEmpty(), "expires");
    action.prepare(animation);
    check(animation.controller.transition==3, "recovery blend");
    action.start("melee",18); action.prepare(animation);
    check(owner.data.value!=first && animation.controller.resets==2, "same action replays with fresh payload");
    Mob observer = new Mob(); observer.world=new ServerLevel(); observer.world.isClientSide=true;
    observer.world.time=122; observer.data.value=owner.data.value;
    BossActionState client = new BossActionState(observer,key);
    check(client.name().equals("melee") && client.elapsed(0)==4, "late observer sees synchronized action");
    client.start("invalid",99);
    check(observer.data.updates==0 && client.name().equals("melee"), "client cannot author actions");
    owner.alive=false; check(!action.active(), "death overrides action"); owner.alive=true;
    server.time=150;
    LivingEntity target = new LivingEntity(); target.world=server;
    BossMeleeWindup melee = new BossMeleeWindup(owner,action,8,18,3.2);
    int[] hits={0};
    melee.start(target); melee.start(target);
    int sequence=owner.data.value.getInt("sequence");
    for (int tick=0; tick<8; tick++) { server.time=150+tick; melee.tick(victim->{hits[0]++; return true;}); }
    check(hits[0]==0, "no early damage");
    server.time=158; melee.tick(victim->{hits[0]++; return true;});
    melee.tick(victim->{hits[0]++; return true;});
    check(hits[0]==1 && server.particles==6, "one hit and impact particles");
    melee.start(target);
    check(owner.data.value.getInt("sequence")==sequence, "recovery cannot restart");
    for (String reason : new String[]{"range","sight","dead","creative","dimension","owner_dead","interrupted","removed"}) {
      server.time+=30; owner.distance=1; owner.visible=true; owner.alive=true;
      target.alive=true; target.creative=false; target.removed=false; target.world=server;
      melee.start(target);
      switch(reason) {
        case "range" -> owner.distance=4;
        case "sight" -> owner.visible=false;
        case "dead" -> target.alive=false;
        case "creative" -> target.creative=true;
        case "dimension" -> target.world=new ServerLevel();
        case "owner_dead" -> owner.alive=false;
        case "interrupted" -> action.start("phase",20);
        case "removed" -> target.removed=true;
      }
      server.time+=8; melee.tick(victim->{hits[0]++; return true;});
      check(hits[0]==1, "cancel: "+reason);
    }
    System.out.println("PASS: "+checks+" action/melee contracts (isolated API stubs, not an in-game test)");
  }
}`
};
const sources = [];
for (const [relative, content] of Object.entries(stubs)) {
  const destination = path.join(output, relative);
  fs.mkdirSync(path.dirname(destination), { recursive: true });
  fs.writeFileSync(destination, content + '\n');
  sources.push(destination);
}
for (const name of ['BossActionState', 'BossMeleeWindup']) sources.push(path.join(javaRoot, `boss/ai/${name}.java`));
const javaHome = process.env.JAVA_HOME;
assert(javaHome, 'JAVA_HOME must point to JDK 17');
const run = (command, args) => {
  const result = spawnSync(path.join(javaHome, 'bin', command), args, { encoding: 'utf8' });
  process.stdout.write(result.stdout || '');
  process.stderr.write(result.stderr || '');
  assert.equal(result.status, 0, command);
};
run('javac.exe', ['-encoding', 'UTF-8', '-d', output, ...sources]);
run('java.exe', ['-cp', output, 'BossActionContractCheck']);
console.log('PASS: all nine non-easter bosses register synchronized actions; rat/barrel timing matches resources');
