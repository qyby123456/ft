/* 贴图补边(2026-09-12): 把贴图扩到指定尺寸, 多出来的区域用**边缘像素**填充。
 * 用途: geo 声明的 texture_height 比实际贴图大时(例如姜饼王 128x128 声明 vs 128x64 实际),
 *       超出部分本来会被 GL 边缘钳制; 用边缘填充后采样结果**完全一致**, 但 UV 落在图内, 不再越界。
 * 用法: node pad_texture.cjs <in.png> <out.png> <W> <H>
 */
const { decodePNG, Canvas } = require('./png.cjs');
const [src, out, W, H] = [process.argv[2], process.argv[3], parseInt(process.argv[4], 10), parseInt(process.argv[5], 10)];
const img = decodePNG(src);
const cv = new Canvas(W, H);
const at = (x, y) => {
    const cx = Math.min(img.w - 1, Math.max(0, x)), cy = Math.min(img.h - 1, Math.max(0, y));
    const i = (cy * img.w + cx) * 4;
    return [img.data[i], img.data[i + 1], img.data[i + 2], img.data[i + 3]];
};
for (let y = 0; y < H; y++) {
    for (let x = 0; x < W; x++) {
        const c = at(x, y);
        if (c[3] === 0) continue;
        cv.set(x, y, c[0], c[1], c[2], c[3] / 255);
    }
}
cv.save(out);
console.log(`  ${src} ${img.w}x${img.h} -> ${out} ${W}x${H} (边缘填充)`);
