"""Render an authored bbmodel with its animated camera and translucent glass."""
from pathlib import Path
import base64
import io
import json
import math
import subprocess
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT.parent / ".preview-deps"))
import moderngl


OUT = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "TACZ视角与检视重制-v7"
MODEL_PATH = Path(sys.argv[2]) if len(sys.argv) > 2 else OUT / "农场物语-橙子果酒-TACZ视角与检视重制-v7.bbmodel"
model = json.loads(MODEL_PATH.read_text(encoding="utf-8"))
OUT.mkdir(parents=True, exist_ok=True)
groups = {group["uuid"]: group for group in model["groups"]}
camera = next(element for element in model["elements"] if element.get("type") == "camera")


def inflate(nodes):
    return [
        {**groups[node["uuid"]], **node, "children": inflate(node.get("children", []))}
        if isinstance(node, dict)
        else node
        for node in nodes
    ]


model["outliner"] = inflate(model["outliner"])
WIDTH, HEIGHT = 1024, 576
context = moderngl.create_standalone_context()
target = context.simple_framebuffer((WIDTH, HEIGHT), components=4, samples=4)
resolve = context.simple_framebuffer((WIDTH, HEIGHT), components=4)

program = context.program(
    vertex_shader="""#version 330
in vec3 p; in vec2 uv; in vec3 n;
uniform mat4 mvp; uniform mat4 model;
out vec2 texcoord; out float shade;
void main() {
    gl_Position = mvp * vec4(p, 1.0);
    texcoord = uv;
    vec3 normal = normalize(mat3(model) * n);
    shade = 0.68 + 0.30 * max(0.0, dot(normal, normalize(vec3(-0.4, 1.0, 0.7))));
}
""",
    fragment_shader="""#version 330
uniform sampler2D tex; uniform bool translucentPass;
in vec2 texcoord; in float shade; out vec4 color;
void main() {
    vec4 sampled = texture(tex, texcoord);
    if (sampled.a < 0.1) discard;
    if (translucentPass != (sampled.a < 0.999)) discard;
    color = vec4(sampled.rgb * shade, sampled.a);
}
""",
)
background_program = context.program(
    vertex_shader="""#version 330
in vec2 p; out vec2 v;
void main() { v = p; gl_Position = vec4(p, 0, 1); }
""",
    fragment_shader="""#version 330
in vec2 v; out vec4 color;
void main() {
    float amount = (v.y + 1.0) * 0.5;
    vec3 base = mix(vec3(0.075, 0.095, 0.11), vec3(0.17, 0.20, 0.22), amount);
    color = vec4(base, 1.0);
}
""",
)
background = context.vertex_array(
    background_program,
    [
        (
            context.buffer(
                np.array(
                    [-1, -1, 1, -1, -1, 1, 1, -1, 1, 1, -1, 1], dtype="f4"
                ).tobytes()
            ),
            "2f",
            "p",
        )
    ],
)

textures = []
for texture_data in model["textures"]:
    image_data = Image.open(
        io.BytesIO(base64.b64decode(texture_data["source"].split(",")[1]))
    ).convert("RGBA")
    texture = context.texture(image_data.size, 4, image_data.tobytes())
    texture.filter = (moderngl.NEAREST, moderngl.NEAREST)
    texture.repeat_x = texture.repeat_y = False
    textures.append((texture, image_data.size))


def translate(vector):
    matrix = np.eye(4)
    matrix[:3, 3] = vector
    return matrix


def rotate(angles):
    x, y, z = np.radians(angles)
    cx, sx = math.cos(x), math.sin(x)
    cy, sy = math.cos(y), math.sin(y)
    cz, sz = math.cos(z), math.sin(z)
    matrix = np.eye(4)
    matrix[:3, :3] = [
        [cz * cy, cz * sy * sx - sz * cx, cz * sy * cx + sz * sx],
        [sz * cy, sz * sy * sx + cz * cx, sz * sy * cx - cz * sx],
        [-sy, cy * sx, cy * cx],
    ]
    return matrix


meshes = {}
corners = {}
for element in model["elements"]:
    if element.get("type") != "cube":
        continue
    x0, y0, z0 = element["from"]
    x1, y1, z1 = element["to"]
    faces = {
        "north": [[x0, y1, z0], [x1, y1, z0], [x1, y0, z0], [x0, y0, z0]],
        "south": [[x1, y1, z1], [x0, y1, z1], [x0, y0, z1], [x1, y0, z1]],
        "east": [[x1, y1, z0], [x1, y1, z1], [x1, y0, z1], [x1, y0, z0]],
        "west": [[x0, y1, z1], [x0, y1, z0], [x0, y0, z0], [x0, y0, z1]],
        "up": [[x0, y1, z1], [x1, y1, z1], [x1, y1, z0], [x0, y1, z0]],
        "down": [[x0, y0, z0], [x1, y0, z0], [x1, y0, z1], [x0, y0, z1]],
    }
    batches = {}
    for face_name, face_data in element["faces"].items():
        texture_id = face_data.get("texture")
        if texture_id is None:
            continue
        points = np.array(faces[face_name], dtype=float)
        normal = np.cross(points[1] - points[0], points[2] - points[0])
        normal_length = np.linalg.norm(normal)
        if normal_length < 1e-9:
            continue
        normal /= normal_length
        u0, v0, u1, v1 = face_data["uv"]
        texture_width, texture_height = textures[texture_id][1]
        uv = np.array([[u0, v0], [u1, v0], [u1, v1], [u0, v1]]) / [
            texture_width,
            texture_height,
        ]
        uv = np.roll(uv, int(face_data.get("rotation", 0) / 90), axis=0)
        for index in (0, 1, 2, 0, 2, 3):
            batches.setdefault(texture_id, []).append([*points[index], *uv[index], *normal])
    origin = np.array(element.get("origin", [0, 0, 0]))
    element_matrix = translate(origin) @ rotate(element.get("rotation", [0, 0, 0])) @ translate(-origin)
    buffers = []
    for texture_id, vertices in batches.items():
        vertex_buffer = context.buffer(np.array(vertices, dtype="f4").tobytes())
        vertex_array = context.vertex_array(
            program, [(vertex_buffer, "3f 2f 3f", "p", "uv", "n")]
        )
        buffers.append((texture_id, vertex_array))
    meshes[element["uuid"]] = (element_matrix, buffers, element["name"])
    corners[element["uuid"]] = np.array(
        [[x, y, z, 1] for x in (x0, x1) for y in (y0, y1) for z in (z0, z1)]
    )


def channel(animator, channel_name, time, default):
    keys = animator.get(channel_name, [])
    if not keys:
        return np.array(default, dtype=float)
    if time <= keys[0][0]:
        return keys[0][1]
    for index in range(1, len(keys)):
        if time <= keys[index][0] + 1e-8:
            left_time, left_value, left_key = keys[index - 1]
            right_time, right_value, right_key = keys[index]
            amount = (time - left_time) / (right_time - left_time)
            if left_key.get("interpolation", "linear") == "step":
                return left_value
            if left_key.get("interpolation", "linear") == "bezier":
                return (
                    (1 - amount) ** 3 * left_value
                    + 3 * (1 - amount) ** 2 * amount * (
                        left_value + np.array(left_key.get("bezier_right_value", [0] * 3))
                    )
                    + 3 * (1 - amount) * amount**2 * (
                        right_value + np.array(right_key.get("bezier_left_value", [0] * 3))
                    )
                    + amount**3 * right_value
                )
            return left_value + (right_value - left_value) * amount
    return keys[-1][1]


animations = {}
for animation in model["animations"]:
    animator_channels = {}
    for identifier, animator in animation["animators"].items():
        channels = {}
        for keyframe in animator["keyframes"]:
            point = keyframe["data_points"][0]
            vector = np.array([float(point.get(axis, 0)) for axis in "xyz"])
            channels.setdefault(keyframe["channel"], []).append(
                (keyframe["time"], vector, keyframe)
            )
        for values in channels.values():
            values.sort(key=lambda value: value[0])
        animator_channels[identifier] = channels
    animations[animation["name"].split(".")[-1]] = animator_channels


def instances(clip_name, time):
    animator_channels = animations[clip_name]

    def walk(nodes, parent):
        for node in nodes:
            if isinstance(node, str):
                if node not in meshes:
                    continue
                cube_matrix, batches, name = meshes[node]
                yield node, parent @ cube_matrix, batches, name
                continue
            animator = animator_channels.get(node["uuid"], {})
            origin = np.array(node["origin"])
            position = channel(animator, "position", time, [0, 0, 0])
            rotation = channel(animator, "rotation", time, [0, 0, 0]) + np.array(
                node.get("rotation", [0, 0, 0])
            )
            scale = channel(animator, "scale", time, [1, 1, 1])
            if np.min(np.abs(scale)) < 1e-7:
                continue
            matrix = (
                parent
                @ translate(origin + position)
                @ rotate(rotation)
                @ np.diag([*scale, 1])
                @ translate(-origin)
            )
            yield from walk(node.get("children", []), matrix)

    return list(walk(model["outliner"], np.eye(4)))


near, far = 0.1, 100
projection_scale = 1 / math.tan(math.radians(camera["fov"]) / 2)
projection = np.array(
    [
        [projection_scale / (WIDTH / HEIGHT), 0, 0, 0],
        [0, projection_scale, 0, 0],
        [0, 0, (far + near) / (near - far), 2 * far * near / (near - far)],
        [0, 0, -1, 0],
    ]
)


def camera_values(clip_name, time, animated=True):
    camera_animator = animations[clip_name].get(camera["uuid"], {})
    if not animated:
        return np.array(camera["position"], dtype=float), np.array(camera["rotation"], dtype=float)
    position = np.array(camera["position"], dtype=float) + channel(
        camera_animator, "position", time, [0, 0, 0]
    )
    rotation = np.array(camera["rotation"], dtype=float) + channel(
        camera_animator, "rotation", time, [0, 0, 0]
    )
    return position, rotation


def view_projection(clip_name, time, animated=True):
    position, rotation = camera_values(clip_name, time, animated)
    camera_world = translate(position) @ rotate(rotation)
    return np.linalg.inv(camera_world), projection @ np.linalg.inv(camera_world)


font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 23)
small_font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 15)


def render(clip_name, time, caption, animated_camera=True):
    view, view_projection_matrix = view_projection(clip_name, time, animated_camera)
    target.use()
    context.disable(moderngl.DEPTH_TEST | moderngl.CULL_FACE | moderngl.BLEND)
    context.depth_mask = True
    target.clear(0.1, 0.12, 0.14, 1, depth=1)
    background.render()
    context.enable(moderngl.DEPTH_TEST | moderngl.CULL_FACE)
    objects = instances(clip_name, time)
    for translucent in (False, True):
        program["translucentPass"].value = translucent
        if translucent:
            context.enable(moderngl.BLEND)
            context.blend_func = moderngl.SRC_ALPHA, moderngl.ONE_MINUS_SRC_ALPHA
            context.depth_mask = False
            objects.sort(key=lambda item: (view @ item[1] @ corners[item[0]].mean(axis=0))[2])
        for _, matrix, batches, _ in objects:
            program["model"].write(matrix.astype("f4").T.tobytes())
            program["mvp"].write((view_projection_matrix @ matrix).astype("f4").T.tobytes())
            for texture_id, vertex_array in batches:
                textures[texture_id][0].use()
                vertex_array.render()
    context.depth_mask = True
    context.disable(moderngl.BLEND)
    context.copy_framebuffer(resolve, target)
    output = (
        Image.frombytes("RGBA", (WIDTH, HEIGHT), resolve.read(components=4))
        .transpose(Image.Transpose.FLIP_TOP_BOTTOM)
        .convert("RGB")
    )
    draw = ImageDraw.Draw(output)
    if caption:
        draw.text((25, 18), caption, font=font, fill=(239, 214, 165))
        camera_position, camera_rotation = camera_values(clip_name, time, animated_camera)
        draw.text(
            (25, 52),
            "Blockbench 工程预览 · 动态 camera · "
            + f"旋转 {camera_rotation[0]:.2f}, {camera_rotation[1]:.2f}, {camera_rotation[2]:.2f}",
            font=small_font,
            fill=(176, 191, 199),
        )
    draw.line((WIDTH / 2 - 4, HEIGHT / 2, WIDTH / 2 + 4, HEIGHT / 2), fill=(126, 140, 147))
    draw.line((WIDTH / 2, HEIGHT / 2 - 4, WIDTH / 2, HEIGHT / 2 + 4), fill=(126, 140, 147))
    return output


def preview_caption(time):
    if time < 0.22:
        return "自然空手下探 · 酒瓶仍在画面外"
    if time < 0.92:
        return "从右侧下方提起酒瓶 · 加速、过冲、回稳"
    if time < 1.45:
        return "采用你调整的握持 · 瓶身已恢复等比"
    inspect_time = time - 1.45
    if inspect_time < 0.78:
        return "右手把酒瓶提近 · 轻微向右倾斜"
    if inspect_time < 1.32:
        return "完整左臂进入 · 从下方稳稳托住"
    if inspect_time < 2.28:
        return "双手缓慢转动 · 查看瓶身背面"
    if inspect_time < 2.58:
        return "背标短暂停留 · 镜头自然呼吸"
    if inspect_time < 4.08:
        return "继续转回正面 · 轻微过冲后回稳"
    if inspect_time < 4.68:
        return "左手松开并沿画面边缘退出"
    if inspect_time < 5.70:
        return "右手把酒瓶平顺放回持握位置"
    if time < 7.45:
        return "检视结束 · 与常规持握无跳变"
    if time < 8.10:
        return "酒瓶先收入口袋"
    return "酒瓶离开画面后 · 空手回到右下自然位置"


def main():
    for clip_name, time in (("draw", 0.24), ("put_away", 0.52)):
        view, _ = view_projection(clip_name, time)
        for identifier, matrix, _, name in instances(clip_name, time):
            if name.startswith("bottle_original") or name == "original_yellow_stopper":
                points = (view @ matrix @ corners[identifier].T).T
                assert np.all(points[:, 1] < -np.abs(points[:, 2])), (
                    clip_name,
                    time,
                    name,
                    points.tolist(),
                )

    shots = [
        (0.00, "自然空手"),
        (0.36, "下方提起"),
        (0.92, "用户握持"),
        (1.97, "拉近与倾斜"),
        (2.63, "左手托底"),
        (3.40, "转向背标"),
        (3.88, "背标停留"),
        (4.97, "转回正面"),
        (5.53, "轻微回稳"),
        (5.91, "左手退出"),
        (7.15, "回到持握"),
        (8.60, "收好后回手"),
    ]
    sheet = Image.new("RGB", (1280, 3 * 220), (20, 24, 29))
    sheet_draw = ImageDraw.Draw(sheet)
    for index, (time, label) in enumerate(shots):
        frame = render("preview_complete", time, "").crop((0, 80, WIDTH, HEIGHT)).resize((320, 155))
        x = index % 4 * 320
        y = index // 4 * 220
        sheet.paste(frame, (x, y + 40))
        sheet_draw.text((x + 10, y + 10), label, font=small_font, fill="white")
    sheet.save(OUT / "关键姿态预览.jpg")

    inspect_times = [
        0.00,
        0.30,
        0.52,
        0.78,
        1.18,
        1.32,
        1.62,
        1.95,
        2.28,
        2.48,
        2.58,
        3.08,
        3.52,
        3.82,
        4.08,
        4.46,
        5.12,
        5.70,
    ]
    detail = Image.new("RGB", (1536, 6 * 320), (20, 24, 29))
    for index, time in enumerate(inspect_times):
        frame = render("inspect", time, f"检视 {time:.2f} s").resize((512, 288))
        detail.paste(frame, (index % 3 * 512, index // 3 * 320))
    detail.save(OUT / "完整手臂与镜头检查.jpg")

    comparison = Image.new("RGB", (1536, 2 * 320), (20, 24, 29))
    comparison_draw = ImageDraw.Draw(comparison)
    for row, time in enumerate((0.52, 3.52)):
        for column, animated in enumerate((False, True)):
            frame = render("inspect", time, "", animated_camera=animated).resize((768, 288))
            comparison.paste(frame, (column * 768, row * 320 + 32))
            comparison_draw.text(
                (column * 768 + 14, row * 320 + 6),
                ("固定相机" if not animated else "动态 camera") + f" · {time:.2f}s",
                font=small_font,
                fill="white",
            )
    comparison.save(OUT / "camera动态对比.jpg")
    render("pose_reference", 0, "你的握持 · 瓶身等比 1.25").save(OUT / "持握预览.png")

    drink_length = next(
        animation["length"]
        for animation in model["animations"]
        if animation["name"].endswith(".drink")
    )
    drink_times = np.linspace(0, drink_length, 12)
    drink_sheet = Image.new("RGB", (1280, 3 * 220), (20, 24, 29))
    drink_sheet_draw = ImageDraw.Draw(drink_sheet)
    for index, time in enumerate(drink_times):
        frame = render("drink", float(time), "").crop((0, 80, WIDTH, HEIGHT)).resize((320, 155))
        x = index % 4 * 320
        y = index // 4 * 220
        drink_sheet.paste(frame, (x, y + 40))
        drink_sheet_draw.text((x + 10, y + 10), f"drink {time:.2f} s", font=small_font, fill="white")
    drink_sheet.save(OUT / "饮用与换瓶关键姿态.jpg")

    transition_times = [5.34, 5.60, 5.72, 5.84, 5.94, 6.08, 6.24, 6.33, 6.42, 6.56, 6.88, 7.34]
    transition_sheet = Image.new("RGB", (1280, 3 * 220), (20, 24, 29))
    transition_sheet_draw = ImageDraw.Draw(transition_sheet)
    for index, time in enumerate(transition_times):
        frame = render("drink", time, "").crop((0, 80, WIDTH, HEIGHT)).resize((320, 155))
        x = index % 4 * 320
        y = index // 4 * 220
        transition_sheet.paste(frame, (x, y + 40))
        transition_sheet_draw.text(
            (x + 10, y + 10),
            f"换瓶衔接 {time:.2f} s",
            font=small_font,
            fill="white",
        )
    transition_sheet.save(OUT / "饮用换瓶衔接检查.jpg")

    walk_length = next(
        animation["length"]
        for animation in model["animations"]
        if animation["name"].endswith(".walk")
    )
    walk_times = np.linspace(0, walk_length, 16)
    walk_sheet = Image.new("RGB", (1280, 4 * 220), (20, 24, 29))
    walk_sheet_draw = ImageDraw.Draw(walk_sheet)
    for index, time in enumerate(walk_times):
        frame = render("walk", float(time), "").crop((0, 80, WIDTH, HEIGHT)).resize((320, 155))
        x = index % 4 * 320
        y = index // 4 * 220
        walk_sheet.paste(frame, (x, y + 40))
        walk_sheet_draw.text((x + 10, y + 10), f"walk {time:.2f} s", font=small_font, fill="white")
    walk_sheet.save(OUT / "行走循环关键姿态.jpg")

    for clip_name, output_name, frame_count, rows in (
        ("sprint", "奔跑循环关键姿态.jpg", 16, 4),
        ("jump", "跳跃关键姿态.jpg", 12, 3),
    ):
        clip_length = next(
            animation["length"]
            for animation in model["animations"]
            if animation["name"].endswith(f".{clip_name}")
        )
        sheet = Image.new("RGB", (1280, rows * 220), (20, 24, 29))
        sheet_draw = ImageDraw.Draw(sheet)
        for index, time in enumerate(np.linspace(0, clip_length, frame_count)):
            frame = render(clip_name, float(time), "").crop((0, 80, WIDTH, HEIGHT)).resize((320, 155))
            x = index % 4 * 320
            y = index // 4 * 220
            sheet.paste(frame, (x, y + 40))
            sheet_draw.text(
                (x + 10, y + 10),
                f"{clip_name} {time:.2f} s",
                font=small_font,
                fill="white",
            )
        sheet.save(OUT / output_name)

    if "--stills-only" in sys.argv:
        print("Rendered still review with animated camera and translucent glass.")
        raise SystemExit(0)

    ffmpeg = ROOT.parent.parents[2] / "ffmpeg" / "bin" / "ffmpeg.exe"
    command = [
        str(ffmpeg),
        "-hide_banner",
        "-loglevel",
        "error",
        "-y",
        "-f",
        "rawvideo",
        "-pix_fmt",
        "rgb24",
        "-s",
        f"{WIDTH}x{HEIGHT}",
        "-r",
        "60",
        "-i",
        "-",
        "-an",
        "-c:v",
        "libx264",
        "-crf",
        "18",
        "-pix_fmt",
        "yuv420p",
        "-movflags",
        "+faststart",
        str(OUT / "第一人称动态camera动画预览.mp4"),
    ]
    process = subprocess.Popen(command, stdin=subprocess.PIPE)
    for frame_index in range(math.ceil(8.73 * 60)):
        time = frame_index / 60
        process.stdin.write(render("preview_complete", time, preview_caption(time)).tobytes())
    for clip_name, length, label in (
        ("draw", 0.92, "拿出慢放 · 手先下探，酒瓶从画面外连续进入"),
        ("put_away", 0.98, "收回慢放 · 酒瓶先离开，空手随后回到自然位置"),
    ):
        for frame_index in range(math.ceil(length * 120)):
            process.stdin.write(render(clip_name, frame_index / 120, label).tobytes())
    process.stdin.close()
    assert process.wait() == 0


    def drink_caption(time):
        if time < 5.60:
            return "饮用动作 · 酒液逐渐减少，木塞保持拔出"
        if time < 5.94:
            return "饮用结束 · 右手把空瓶完整收回画面外"
        if time < 6.24:
            return "空瓶已经收好 · 右臂继续下探并完全隐藏"
        if time < 6.42:
            return "短暂空手 · 在画面外换成一瓶新的酒"
        if time < 6.88:
            return "右臂带着新瓶从下方重新拿出"
        return "满瓶回到正常持握 · 没有直接刷新"


    drink_command = command[:-1] + [str(OUT / "饮用后收空瓶再拿新瓶-60fps.mp4")]
    drink_process = subprocess.Popen(drink_command, stdin=subprocess.PIPE)
    for frame_index in range(math.ceil(drink_length * 60) + 1):
        time = min(frame_index / 60, drink_length)
        drink_process.stdin.write(render("drink", time, drink_caption(time)).tobytes())
    drink_process.stdin.close()
    assert drink_process.wait() == 0

    movement_command = command[:-1] + [str(OUT / "行走奔跑跳跃-60fps.mp4")]
    movement_process = subprocess.Popen(movement_command, stdin=subprocess.PIPE)
    for clip_name, length, repeats, label in (
        ("walk", 1.00, 2, "行走 · 一秒双步循环，瓶身与手臂保持贴合"),
        ("sprint", 0.68, 3, "奔跑 · 低位快速摆动，木塞固定，酒液有惯性"),
        ("jump", 1.20, 1, "跳跃 · 起跳下压、滞空、落地回弹"),
    ):
        for repeat in range(repeats):
            for frame_index in range(math.ceil(length * 60)):
                movement_process.stdin.write(
                    render(clip_name, frame_index / 60, label).tobytes()
                )
    movement_process.stdin.close()
    assert movement_process.wait() == 0
    print(
        "Rendered at 60 fps with animated camera and translucent glass; draw/stow off-screen and "
        "empty-bottle replacement checks passed."
    )


if __name__ == "__main__":
    main()
