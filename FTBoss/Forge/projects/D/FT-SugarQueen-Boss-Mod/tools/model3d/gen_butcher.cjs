const fs = require('node:fs');
const path = require('node:path');
const {Canvas} = require('./png.cjs');
const {SPEC, POSES, REST} = require('./butcher_spec.cjs');
const output = path.resolve(process.argv[2] || 'out');
fs.mkdirSync(output, {recursive:true});
const textureSize = 256;
const canvas = new Canvas(textureSize, textureSize);
const palette = {
    skin:[194,139,111], shirt:[106,43,41], apron:[208,190,151], cap:[230,217,185],
    leather:[66,44,39], brass:[185,140,63], beard:[76,47,38], trouser:[49,67,64],
    boot:[40,34,37], metal:[138,149,145]
};
let cursorX = 1, cursorY = 1, shelf = 0;
for (const bone of SPEC) {
    for (const cube of bone.boxes) {
        const width = 2 * (cube.w + cube.d), height = cube.d + cube.h;
        if (cursorX + width + 1 > textureSize) { cursorX = 1; cursorY += shelf + 2; shelf = 0; }
        if (cursorY + height + 1 > textureSize) throw new Error('Texture atlas overflow');
        cube.uv = [cursorX, cursorY];
        const faces = [
            [cube.d,0,cube.w,cube.d,1.08], [cube.d+cube.w,0,cube.w,cube.d,0.78],
            [0,cube.d,cube.d,cube.h,0.82], [cube.d,cube.d,cube.w,cube.h,1],
            [cube.d+cube.w,cube.d,cube.d,cube.h,0.90], [2*cube.d+cube.w,cube.d,cube.w,cube.h,0.86]
        ];
        for (const [faceX,faceY,faceW,faceH,shade] of faces) {
            for (let pixelY = 0; pixelY < faceH; pixelY++) {
                for (let pixelX = 0; pixelX < faceW; pixelX++) {
                    const edge = pixelY === 0 ? 1.08 : pixelY === faceH - 1 ? 0.78 : 1;
                    const grain = 0.98 + ((pixelX * 7 + pixelY * 13 + cursorY) % 5) * 0.01;
                    const color = palette[cube.mat].map(channel => Math.min(255, Math.round(channel * shade * edge * grain)));
                    if (cube.mat === 'apron' && pixelX % 6 === 0 && pixelY > 1) color.forEach((channel,index) => color[index] = Math.round(channel * 0.90));
                    canvas.set(cursorX+faceX+pixelX,cursorY+faceY+pixelY,...color,1);
                }
            }
        }
        const frontX = cursorX + cube.d, frontY = cursorY + cube.d;
        if (cube.detail === 'stain') {
            canvas.rect(frontX+1,frontY+2,2,3,120,57,39,1);
            canvas.rect(frontX+2,frontY+4,2,1,143,76,46,1);
            if (cube.w > 8) canvas.rect(frontX+8,frontY+1,3,2,145,85,48,1);
        }
        if (cube.detail === 'face') {
            canvas.rect(frontX+1,frontY+2,3,1,78,44,33,1);
            canvas.rect(frontX+6,frontY+2,3,1,78,44,33,1);
            canvas.rect(frontX+1,frontY+3,3,2,62,40,35,1);
            canvas.rect(frontX+6,frontY+3,3,2,62,40,35,1);
            canvas.rect(frontX+2,frontY+3,1,1,246,176,69,1);
            canvas.rect(frontX+7,frontY+3,1,1,246,176,69,1);
            canvas.rect(frontX+7,frontY+5,1,3,138,74,56,1);
            canvas.rect(frontX+2,frontY+8,6,1,82,40,32,1);
        }
        if (cube.mat === 'beard') canvas.rect(frontX+2,frontY,5,1,211,189,150,1);
        cursorX += width + 2;
        shelf = Math.max(shelf,height);
    }
}
canvas.save(path.join(output,'butcher_v3.png'));
const number = value => Number(value).toFixed(4).replace(/0+$/,'').replace(/\.$/,'.0') + 'F';
const lines = [
    'package net.farmingtales.ftboss.client.model;',
    '',
    'import com.mojang.blaze3d.vertex.PoseStack;',
    'import com.mojang.blaze3d.vertex.VertexConsumer;',
    'import net.farmingtales.ftboss.entity.MeatMountainEntity;',
    'import net.minecraft.client.model.ArmedModel;',
    'import net.minecraft.client.model.EntityModel;',
    'import net.minecraft.client.model.geom.ModelLayerLocation;',
    'import net.minecraft.client.model.geom.ModelPart;',
    'import net.minecraft.client.model.geom.PartPose;',
    'import net.minecraft.client.model.geom.builders.CubeListBuilder;',
    'import net.minecraft.client.model.geom.builders.LayerDefinition;',
    'import net.minecraft.client.model.geom.builders.MeshDefinition;',
    'import net.minecraft.client.model.geom.builders.PartDefinition;',
    'import net.minecraft.resources.ResourceLocation;',
    'import net.minecraft.util.Mth;',
    'import net.minecraft.world.entity.HumanoidArm;',
    '',
    'public class ButcherBossModel<T extends MeatMountainEntity> extends EntityModel<T> implements ArmedModel {',
    '    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation("ftboss", "butcher"), "main");',
    '    public static final float HAND_OFFSET_Y = -0.375F;',
    '    public static final float WEAPON_SCALE = 1.15F;'
];
for (const bone of SPEC) lines.push(`    private final ModelPart ${bone.name};`);
lines.push('', '    public ButcherBossModel(ModelPart root) {','        this.root = root;');
for (const bone of SPEC.filter(bone => bone.parent)) lines.push(`        this.${bone.name} = this.${bone.parent}.getChild("${bone.name}");`);
lines.push('    }','','    public static LayerDefinition createBodyLayer() {',
    '        MeshDefinition mesh = new MeshDefinition();','        PartDefinition root = mesh.getRoot();');
for (const bone of SPEC.filter(bone => bone.parent)) {
    lines.push(`        PartDefinition ${bone.name} = ${bone.parent}.addOrReplaceChild("${bone.name}", CubeListBuilder.create()`);
    for (const cube of bone.boxes) lines.push(`                .texOffs(${cube.uv.join(', ')}).addBox(${[cube.x,cube.y,cube.z,cube.w,cube.h,cube.d].map(number).join(', ')})`);
    lines.push(`                , PartPose.offset(${bone.pivot.map(number).join(', ')}));`);
}
lines.push(`        return LayerDefinition.create(mesh, ${textureSize}, ${textureSize});`,'    }','',
    '    @Override',
    '    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {',
    '        this.root.getAllParts().forEach(ModelPart::resetPose);',
    '        float stride = limbSwing * 0.62F;',
    '        float amplitude = Math.min(limbSwingAmount, 0.6F);',
    '        this.leg_l.xRot = Mth.cos(stride) * amplitude * 0.8F;',
    '        this.leg_r.xRot = -this.leg_l.xRot;',
    '        this.shin_l.xRot = -Math.max(0, this.leg_l.xRot) * 0.6F;',
    '        this.shin_r.xRot = -Math.max(0, this.leg_r.xRot) * 0.6F;',
    '        this.apron_l.xRot = -Math.max(0, -this.leg_l.xRot) * 0.8F;',
    '        this.apron_r.xRot = -Math.max(0, -this.leg_r.xRot) * 0.8F;');
for (const [bone,rotation] of Object.entries(REST)) {
    for (const [index,axis] of ['x','y','z'].entries()) lines.push(`        this.${bone}.${axis}Rot = ${number(rotation[index] * Math.PI / 180)};`);
}
lines.push(
    '        this.body.xRot += Mth.sin(ageInTicks * 0.08F) * 0.012F;',
    '        this.arm_l.xRot -= this.leg_l.xRot * 0.6F;',
    '        this.head.yRot = Mth.clamp(netHeadYaw, -40, 40) * Mth.DEG_TO_RAD;',
    '        this.head.xRot += Mth.clamp(headPitch, -25, 25) * Mth.DEG_TO_RAD;',
    '        String action = entity.getButcherAction();',
    '        float age = entity.getButcherActionAge(ageInTicks - entity.tickCount);',
    '        applyAction(action, age);',
    '    }','',
    '    private void applyAction(String action, float age) {',
    '        switch (action) {');
for (const [action,frames] of Object.entries(POSES)) {
    const bones = [...new Set(frames.flatMap(frame => Object.keys(frame[1])))];
    lines.push(`            case "${action}" -> {`);
    for (const bone of bones) lines.push(`                animate(this.${bone}, age, ${action.toUpperCase()}_${bone.toUpperCase()});`);
    lines.push('            }');
}
lines.push('            default -> { }','        }','    }','');
for (const [action,frames] of Object.entries(POSES)) {
    const bones = [...new Set(frames.flatMap(frame => Object.keys(frame[1])))];
    for (const bone of bones) {
        lines.push(`    private static final float[][] ${action.toUpperCase()}_${bone.toUpperCase()} = {`);
        for (const [tick,pose] of frames) lines.push('            {' + [tick,...(pose[bone] || REST[bone] || [0,0,0])].map(number).join(', ') + '},');
        lines.push('    };');
    }
}
lines.push('',
    '    private static void animate(ModelPart part, float age, float[][] frames) {',
    '        int index = 0;',
    '        while (index < frames.length - 2 && age > frames[index + 1][0]) index++;',
    '        float[] from = frames[index];',
    '        float[] to = frames[index + 1];',
    '        float progress = Mth.clamp((age - from[0]) / (to[0] - from[0]), 0, 1);',
    '        progress = progress * progress * (3 - 2 * progress);',
    '        part.xRot = Mth.lerp(progress, from[1], to[1]) * Mth.DEG_TO_RAD;',
    '        part.yRot = Mth.lerp(progress, from[2], to[2]) * Mth.DEG_TO_RAD;',
    '        part.zRot = Mth.lerp(progress, from[3], to[3]) * Mth.DEG_TO_RAD;',
    '    }','',
    '    @Override',
    '    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float red, float green, float blue, float alpha) {',
    '        this.root.render(pose, buffer, light, overlay, red, green, blue, alpha);',
    '    }','',
    '    @Override',
    '    public void translateToHand(HumanoidArm arm, PoseStack pose) {',
    '        this.root.translateAndRotate(pose);',
    '        this.body.translateAndRotate(pose);',
    '        boolean right = arm == HumanoidArm.RIGHT;',
    '        (right ? this.arm_r : this.arm_l).translateAndRotate(pose);',
    '        (right ? this.forearm_r : this.forearm_l).translateAndRotate(pose);',
    '        (right ? this.hand_r : this.hand_l).translateAndRotate(pose);',
    '        pose.translate(0, HAND_OFFSET_Y, -0.05F);',
    '        pose.scale(WEAPON_SCALE, WEAPON_SCALE, WEAPON_SCALE);',
    '    }','',
    '    public ModelPart root() { return this.root; }','}','');
fs.writeFileSync(path.join(output,'ButcherBossModel.java'),lines.join('\n'));
const absolute = {root:[0,0,0]};
for (const bone of SPEC.filter(bone => bone.parent)) absolute[bone.name] = bone.pivot.map((value,index) => value + absolute[bone.parent][index]);
function geometry(pose) {
    const bones = SPEC.map(bone => {
        const pivot = absolute[bone.name];
        const rotation = pose[bone.name] || [0,0,0];
        return {name:bone.name, ...(bone.parent ? {parent:bone.parent} : {}),
            pivot:[pivot[0],24-pivot[1],pivot[2]], rotation:[-rotation[0],rotation[1],-rotation[2]],
            cubes:bone.boxes.map(cube => ({origin:[pivot[0]+cube.x,24-pivot[1]-cube.y-cube.h,pivot[2]+cube.z],
                size:[cube.w,cube.h,cube.d],uv:cube.uv}))};
    });
    return {format_version:'1.12.0','minecraft:geometry':[{description:{identifier:'geometry.butcher_v3',
        texture_width:textureSize,texture_height:textureSize,visible_bounds_width:5,visible_bounds_height:5,
        visible_bounds_offset:[0,1.5,0]},bones}]};
}
fs.writeFileSync(path.join(output,'butcher_preview.geo.json'),JSON.stringify(geometry(REST),null,2));
fs.writeFileSync(path.join(output,'butcher_neutral.geo.json'),JSON.stringify(geometry({}),null,2));
for (const [action,frames] of Object.entries(POSES)) {
    for (let index = 0; index < frames.length; index++) {
        fs.writeFileSync(path.join(output,`${action}-${frames[index][0]}.geo.json`),JSON.stringify(geometry({...REST,...frames[index][1]}),null,2));
    }
}
console.log(JSON.stringify({bones:SPEC.length,cubes:SPEC.reduce((total,bone) => total+bone.boxes.length,0),atlas:textureSize,usedHeight:cursorY+shelf,output}));
