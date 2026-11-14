/*
 * 2026-09-12 手持加农炮显示(正确做法):
 *  ① 模型加一个**手部骨骼 hand_r**(arm_r 的子级, 枢轴在右手稻草束中心) —— BlockAndItemGeoLayer 需要一个骨骼来定位物品
 *  ② CompostRenderer 挂 BlockAndItemGeoLayer: 在 hand_r 上渲染 create:potato_cannon
 *     (GeckoLib 实体不能直接用原版 ItemInHandLayer: GeoModel 不是 EntityModel)
 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);

/* ① 生成器加 hand_r 骨骼 */
{
    const rel = 'tools/model3d/gen_compost_boss_v4.cjs';
    let s = read(rel);
    let n = 0;
    const a1 = "    fork: [-11.0, B - 3.0, 0], shovel: [11.0, B - 3.0, 0],";
    const b1 = "    fork: [-11.0, B - 3.0, 0], shovel: [11.0, B - 3.0, 0],\n    hand_r: [11.0, B - 6.2, -0.6],   // 右手稻草束中心(手持物渲染定位用)";
    if (s.includes(a1) && !s.includes('hand_r:')) { s = s.replace(a1, b1); n++; } else console.log('  ① PIV 未命中或已存在');
    const a2 = "    cannon: 'arm_r', fly1: 'head', fly2: 'head'";
    const b2 = "    cannon: 'arm_r', fly1: 'head', fly2: 'head', hand_r: 'arm_r'";
    if (s.includes(a2) && !s.includes("hand_r: 'arm_r'")) { s = s.replace(a2, b2); n++; } else console.log('  ① PAR 未命中或已存在');
    write(rel, s);
    console.log(`  ① 生成器: ${n}/2`);
}
for (const mode of ['brown', 'rainbow']) {
    const r = spawnSync('node', ['gen_compost_boss_v4.cjs', 'out_compost_boss', mode], { cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit' });
    console.log(`  ${mode}: ${r.status}`);
}
{
    const A = path.join(ROOT, 'src/main/resources/assets/ftboss');
    fs.copyFileSync(path.join(ROOT, 'tools/model3d/out_compost_boss/compost_superman.geo.json'), path.join(A, 'geo/compost_superman.geo.json'));
    fs.copyFileSync(path.join(ROOT, 'tools/model3d/out_compost_boss/compost_superman.geo.json'), path.join(A, 'geo/compost_superman_p2.geo.json'));
    const g = JSON.parse(fs.readFileSync(path.join(A, 'geo/compost_superman.geo.json'), 'utf8'))['minecraft:geometry'][0];
    const bones = g.bones.map(b => b.name);
    console.log('  骨骼数 ' + bones.length + ', hand_r 存在: ' + bones.includes('hand_r') + ', null: ' + (JSON.stringify(g).includes(': null') ? '⚠' : '无'));
}
/* ② 渲染器挂 BlockAndItemGeoLayer */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/client/render/CompostRenderer.java';
    let s = read(rel);
    if (s.includes('BlockAndItemGeoLayer')) console.log('  ② 已存在');
    else {
        s = s.replace('        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));',
            `        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
        /* 2026-09-12: 在 hand_r 骨骼上渲染手持的 Create 土豆加农炮
           (GeckoLib 实体不能用原版 ItemInHandLayer, 必须用 BlockAndItemGeoLayer) */
        this.addRenderLayer(new software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer<CompostBossEntity>(this,
                (bone, animatable) -> "hand_r".equals(bone.getName()) ? heldCannon() : null,
                (bone, animatable) -> null));`);
        /* 加一个静态方法取手持物(缺 mod 时返回空, 不崩) */
        s = s.replace('    public CompostRenderer(EntityRendererProvider.Context ctx) {',
            `    /** 手持物: Create 的手持土豆加农炮(取不到就返回空) */
    private static net.minecraft.world.item.ItemStack CANNON_CACHE = null;

    private static net.minecraft.world.item.ItemStack heldCannon() {
        if (CANNON_CACHE == null) {
            net.minecraft.world.item.Item it = net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getValue(new net.minecraft.resources.ResourceLocation("create", "potato_cannon"));
            CANNON_CACHE = it == null ? net.minecraft.world.item.ItemStack.EMPTY : new net.minecraft.world.item.ItemStack(it);
        }
        return CANNON_CACHE;
    }

    public CompostRenderer(EntityRendererProvider.Context ctx) {`);
        write(rel, s);
        console.log('  ② CompostRenderer: 已挂 BlockAndItemGeoLayer(hand_r)');
    }
}
console.log('完成');
