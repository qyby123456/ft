"""Build the v7 Blockbench-only bottle inspection with sparse editable curves."""
from pathlib import Path
from copy import deepcopy
import hashlib
import json
import math
import uuid

import numpy as np


ROOT = Path(__file__).resolve().parent
OUT = ROOT / "TACZ视角与检视重制-v7"
SOURCE = ROOT / "用户持握与精简关键帧-v6" / "用户调整原稿-v5-0.93333秒.bbmodel"
OUT.mkdir(parents=True, exist_ok=True)

original = json.loads(SOURCE.read_text(encoding="utf-8"))
model = deepcopy(original)
groups = {group["name"]: group for group in model["groups"]}
by_id = {group["uuid"]: group for group in model["groups"]}
uid = lambda value: str(uuid.uuid5(uuid.NAMESPACE_URL, "farmingtales-inspect-v7/" + value))


def key_vector(keyframe):
    point = keyframe["data_points"][0]
    return np.array([float(point[axis]) for axis in "xyz"])


def linear_channel(animator, channel_name, time, default=(0, 0, 0)):
    keys = sorted(
        [key for key in animator.get("keyframes", []) if key["channel"] == channel_name],
        key=lambda key: key["time"],
    )
    if not keys:
        return np.array(default, dtype=float)
    if time <= keys[0]["time"]:
        return key_vector(keys[0])
    for left_key, right_key in zip(keys, keys[1:]):
        if time <= right_key["time"]:
            if left_key.get("interpolation") == "step":
                return key_vector(left_key)
            amount = (time - left_key["time"]) / (right_key["time"] - left_key["time"])
            return key_vector(left_key) + (key_vector(right_key) - key_vector(left_key)) * amount
    return key_vector(keys[-1])


reference_animation = next(
    animation for animation in original["animations"] if animation["name"].endswith(".draw")
)
reference_time = 0.90
reference = {
    group["name"]: {
        channel_name: linear_channel(
            reference_animation["animators"].get(group["uuid"], {}),
            channel_name,
            reference_time,
            [1, 1, 1] if channel_name == "scale" else [0, 0, 0],
        )
        for channel_name in ("position", "rotation", "scale")
    }
    for group in original["groups"]
}
source_bottle_pose = {name: value.copy() for name, value in reference["bottle_turn"].items()}
reference["bottle_turn"]["scale"] = np.array([1.25, 1.25, 1.25])

bottle_turn = groups["bottle_turn"]
bottle = groups["bottle"]
right_arm = groups["right_forearm"]
left_arm = groups["left_cork_hand"]
bottle_origin = np.array(bottle_turn["origin"], dtype=float)
bottle_center = bottle_origin + reference["bottle_turn"]["position"]
right_origin = np.array(right_arm["origin"], dtype=float)
left_origin = np.array(left_arm["origin"], dtype=float)
STOPPER_SEAT_OFFSET = np.array([0.0, -0.06, 0.0])


def rotation_matrix(angles):
    x, y, z = np.radians(angles)
    cx, sx = math.cos(x), math.sin(x)
    cy, sy = math.cos(y), math.sin(y)
    cz, sz = math.cos(z), math.sin(z)
    return np.array(
        [
            [cz * cy, cz * sy * sx - sz * cx, cz * sy * cx + sz * sx],
            [sz * cy, sz * sy * sx + cz * cx, sz * sy * cx - cz * sx],
            [-sy, cy * sx, cy * cx],
        ]
    )


def matrix_euler(matrix):
    return np.degrees(
        [
            math.atan2(matrix[2, 1], matrix[2, 2]),
            math.asin(np.clip(-matrix[2, 0], -1, 1)),
            math.atan2(matrix[1, 0], matrix[0, 0]),
        ]
    )


def aim_local_y(direction):
    local_y = np.array(direction, dtype=float)
    local_y /= np.linalg.norm(local_y)
    reference_x = np.array([1.0, 0.0, 0.0])
    local_z = np.cross(reference_x, local_y)
    if np.linalg.norm(local_z) < 1e-6:
        reference_x = np.array([0.0, 0.0, 1.0])
        local_z = np.cross(reference_x, local_y)
    local_z /= np.linalg.norm(local_z)
    local_x = np.cross(local_y, local_z)
    return np.column_stack([local_x, local_y, local_z])


def new_group(name, origin, color):
    group = {
        "name": name,
        "uuid": uid(name),
        "origin": [float(value) for value in origin],
        "rotation": [0, 0, 0],
        "export": True,
        "visibility": True,
        "color": color,
        "isOpen": True,
    }
    model["groups"].append(group)
    groups[name] = group
    by_id[group["uuid"]] = group
    return group


calibration = new_group("user_bottle_pose", bottle_origin, 3)
inspection_tilt = new_group("inspect_tilt", bottle_center, 0)
bottle_turn["origin"] = bottle_center.tolist()

root_node = model["outliner"][0]
spin_node = next(child for child in root_node["children"] if child["uuid"] == bottle_turn["uuid"])
calibration_node = {
    "uuid": calibration["uuid"],
    "isOpen": False,
    "children": spin_node["children"],
}
spin_node["children"] = [calibration_node]
original_root_children = root_node["children"]
root_node["children"] = [
    {
        "uuid": inspection_tilt["uuid"],
        "isOpen": True,
        "children": original_root_children,
    }
]

camera = {
    "uuid": uid("first_person_camera"),
    "name": "camera_first_person",
    "type": "camera",
    "position": [0, 16, 18],
    "rotation": [0, 0, 0],
    "fov": 70,
    "aspect_ratio": [16, 9],
    "linked_preview": "main",
    "camera_linked": True,
    "visibility": False,
}
model["elements"].append(camera)
model["outliner"].append(camera["uuid"])
by_id[camera["uuid"]] = camera


def monotone_slopes(points):
    array = np.array(points, dtype=float)
    times = array[:, 0]
    values = array[:, 1:]
    tangents = np.zeros_like(values)
    widths = np.diff(times)
    deltas = np.diff(values, axis=0) / widths[:, None]
    for index in range(1, len(times) - 1):
        for component in range(values.shape[1]):
            if deltas[index - 1, component] * deltas[index, component] > 0:
                left_weight = 2 * widths[index] + widths[index - 1]
                right_weight = widths[index] + 2 * widths[index - 1]
                tangents[index, component] = (left_weight + right_weight) / (
                    left_weight / deltas[index - 1, component]
                    + right_weight / deltas[index, component]
                )
    return times, values, tangents


def sample_points(points, time):
    times, values, tangents = monotone_slopes(points)
    if time <= times[0]:
        return values[0].copy()
    if time >= times[-1]:
        return values[-1].copy()
    index = int(np.searchsorted(times, time) - 1)
    width = times[index + 1] - times[index]
    amount = (time - times[index]) / width
    return (
        (2 * amount**3 - 3 * amount**2 + 1) * values[index]
        + (amount**3 - 2 * amount**2 + amount) * width * tangents[index]
        + (-2 * amount**3 + 3 * amount**2) * values[index + 1]
        + (amount**3 - amount**2) * width * tangents[index + 1]
    )


def bezier_keys(channel_name, points, namespace=""):
    filtered = [
        point
        for index, point in enumerate(points)
        if index == 0
        or index == len(points) - 1
        or not (
            np.max(np.abs(np.array(point[1:]) - points[index - 1][1:])) < 1e-9
            and np.max(np.abs(np.array(point[1:]) - points[index + 1][1:])) < 1e-9
        )
    ]
    times, values, tangents = monotone_slopes(filtered)
    if np.max(np.abs(values - values[0])) < 1e-9:
        times, values, tangents = times[:1], values[:1], tangents[:1]
    keys = []
    for index, time in enumerate(times):
        left_width = (time - times[index - 1]) / 3 if index else 0
        right_width = (times[index + 1] - time) / 3 if index + 1 < len(times) else 0
        keys.append(
            {
                "uuid": uid(f"{namespace}:{channel_name}:{time}:{values[index].tolist()}"),
                "channel": channel_name,
                "time": round(float(time), 6),
                "interpolation": "bezier",
                "data_points": [
                    dict(zip("xyz", [round(float(value), 7) for value in values[index]]))
                ],
                "bezier_linked": False,
                "bezier_left_time": [-left_width] * 3,
                "bezier_right_time": [right_width] * 3,
                "bezier_left_value": (-tangents[index] * left_width).tolist(),
                "bezier_right_value": (tangents[index] * right_width).tolist(),
            }
        )
    return keys


def blank_animation(name, length):
    return {
        "uuid": uid("animation:" + name),
        "name": "animation.orange_wine." + name,
        "loop": "hold",
        "override": True,
        "length": length,
        "snapping": 60,
        "animators": {},
    }


def track_identifier(animation, identifier, channel_name, points):
    target = animation["animators"].setdefault(
        identifier,
        {
            "name": by_id[identifier]["name"],
            "type": "camera" if by_id[identifier].get("type") == "camera" else "bone",
            "keyframes": [],
        },
    )
    target["keyframes"] += bezier_keys(
        channel_name, points, animation["name"] + ":" + by_id[identifier]["name"]
    )


def track(animation, group_name, channel_name, points):
    track_identifier(animation, groups[group_name]["uuid"], channel_name, points)


def constant(animation, group_name, channel_name, value):
    track(animation, group_name, channel_name, [[0, *value]])


def replace_track(animation, group_name, channel_name, points):
    identifier = groups[group_name]["uuid"]
    target = animation["animators"].setdefault(
        identifier,
        {"name": group_name, "type": "bone", "keyframes": []},
    )
    target["keyframes"] = [
        key for key in target["keyframes"] if key["channel"] != channel_name
    ]
    track(animation, group_name, channel_name, points)


def set_constant(animation, group_name, channel_name, value):
    replace_track(animation, group_name, channel_name, [[0, *value]])


def camera_tracks(animation, position_points, rotation_points):
    track_identifier(animation, camera["uuid"], "position", position_points)
    track_identifier(animation, camera["uuid"], "rotation", rotation_points)


def basics(animation):
    for channel_name in ("position", "rotation", "scale"):
        constant(animation, calibration["name"], channel_name, reference["bottle_turn"][channel_name])
    constant(animation, "recolored_original_neck", "scale", [0, 0, 0])
    constant(animation, "wooden_stopper", "scale", [1, 1, 1])
    constant(animation, "wooden_stopper", "position", STOPPER_SEAT_OFFSET)
    constant(animation, "wine_fill_preview", "scale", [1, 1, 1])
    constant(animation, left_arm["name"], "scale", [0.7, 0.7, 0.7])


DRAW_ROOT = [
    [0.00, 3.8, -20.5, 2.0],
    [0.16, 3.8, -20.5, 2.0],
    [0.24, 3.6, -18.2, 1.7],
    [0.36, 2.6, -10.8, 1.0],
    [0.48, 0.8, -2.4, -0.2],
    [0.58, -0.18, 0.7, -0.35],
    [0.72, 0.07, -0.18, 0.10],
    [0.92, 0, 0, 0],
]
DRAW_HAND = [
    [0.00, -0.8, 17.5, 1.5],
    [0.10, -0.5, 17.0, 0.8],
    [0.20, 0.6, 7.4, -0.15],
    [0.28, 0, 0, 0],
    [0.92, 0, 0, 0],
]
DRAW_ROTATION = [
    [0.00, -12, -18, 16],
    [0.12, -16, -20, 20],
    [0.24, 12, 3, -4],
    [0.42, -5, -2, 1],
    [0.58, 2, 0.5, -0.6],
    [0.72, -0.5, -0.2, 0.1],
    [0.92, 0, 0, 0],
]
PUT_ROOT = [
    [0.00, 0, 0, 0],
    [0.10, 0.08, -0.04, 0.20],
    [0.22, 0.75, -2.4, 0.8],
    [0.36, 2.7, -10.8, 1.8],
    [0.52, 4.1, -20.4, 2.4],
    [0.98, 4.1, -20.4, 2.4],
]
PUT_HAND = [
    [0.00, 0, 0, 0],
    [0.45, 0, 0, 0],
    [0.55, 0.4, 6.8, -0.2],
    [0.72, -0.2, 14.2, 0.2],
    [0.88, -0.9, 17.7, 1.7],
    [0.98, -0.8, 17.5, 1.5],
]
PUT_ROTATION = [
    [0.00, 0, 0, 0],
    [0.10, -1, 0, 0.3],
    [0.22, 7, 1, -1],
    [0.36, 18, 3, -2],
    [0.52, 12, 3, -4],
    [0.66, -16, -20, 20],
    [0.84, -12, -18, 16],
    [0.98, -12, -18, 16],
]


def standalone_animation(name, length, root_points, hand_points, rotation_points):
    animation = blank_animation(name, length)
    basics(animation)
    track(
        animation,
        "viewmodel_motion",
        "position",
        [[time, *(np.array(values) + reference["viewmodel_motion"]["position"])] for time, *values in root_points],
    )
    track(
        animation,
        right_arm["name"],
        "position",
        [[time, *(np.array(values) + reference[right_arm["name"]]["position"])] for time, *values in hand_points],
    )
    track(
        animation,
        right_arm["name"],
        "rotation",
        [[time, *(np.array(values) + reference[right_arm["name"]]["rotation"])] for time, *values in rotation_points],
    )
    constant(animation, left_arm["name"], "position", np.array([-7, -12, 10]) - left_origin)
    return animation


draw = standalone_animation("draw", 0.92, DRAW_ROOT, DRAW_HAND, DRAW_ROTATION)
camera_tracks(
    draw,
    [[0, 0, 0, 0], [0.18, 0.01, -0.03, 0.03], [0.40, 0.05, -0.07, 0.10], [0.58, -0.025, 0.03, -0.05], [0.92, 0, 0, 0]],
    [[0, 0, 0, 0], [0.18, 0.25, 0.10, -0.08], [0.40, 1.05, -0.30, 0.42], [0.58, -0.28, 0.13, -0.18], [0.75, 0.10, -0.04, 0.06], [0.92, 0, 0, 0]],
)

put_away = standalone_animation("put_away", 0.98, PUT_ROOT, PUT_HAND, PUT_ROTATION)
camera_tracks(
    put_away,
    [[0, 0, 0, 0], [0.18, -0.015, 0.02, 0.02], [0.42, 0.035, -0.04, 0.06], [0.68, -0.015, 0.015, -0.03], [0.98, 0, 0, 0]],
    [[0, 0, 0, 0], [0.18, -0.10, 0.05, -0.04], [0.42, 0.48, -0.16, 0.20], [0.68, -0.18, 0.08, -0.10], [0.98, 0, 0, 0]],
)

INSPECT_LENGTH = 5.70
INSPECT_ROOT = [
    [0.00, 0, 0, 0],
    [0.12, 0.06, -0.06, 0.03],
    [0.42, -1.55, 2.85, 0.50],
    [0.64, -1.42, 2.70, 0.42],
    [1.95, -1.16, 2.92, 0.54],
    [2.28, -0.92, 3.08, 0.62],
    [2.58, -0.92, 3.08, 0.62],
    [3.52, -1.68, 2.76, 0.46],
    [4.08, -1.42, 2.70, 0.42],
    [4.55, -1.42, 2.70, 0.42],
    [5.12, 0.06, -0.10, -0.03],
    [5.70, 0, 0, 0],
]
INSPECT_TILT = [
    [0.00, 0, 0, 0],
    [0.18, 0, 0, -1.2],
    [0.48, -2.0, -2.0, -9.0],
    [0.70, -1.0, 0.0, -7.5],
    [1.32, -1.0, 0.0, -7.5],
    [1.95, 2.5, -10.0, -2.0],
    [2.28, 4.0, -14.0, 1.0],
    [2.58, 4.0, -14.0, 1.0],
    [3.08, 1.0, 0.0, -5.0],
    [3.52, -2.0, 9.0, -10.0],
    [3.82, -1.0, 3.0, -8.0],
    [4.08, -0.7, 0.2, -7.5],
    [4.42, -0.7, 0.2, -7.5],
    [4.92, 0.15, -0.1, 0.6],
    [5.25, -0.05, 0, -0.15],
    [5.70, 0, 0, 0],
]
BOTTLE_TURN = [
    [0.00, 0, 0, 0],
    [1.32, 0, 0, 0],
    [1.95, 0, 155, 0],
    [2.28, 0, 170, 0],
    [2.58, 0, 170, 0],
    [3.52, 0, 345, 0],
    [3.82, 0, 365, 0],
    [4.08, 0, 360, 0],
    [5.70, 0, 360, 0],
]
INSPECTION_PAUSES = [(2.28, 2.58, 170)]

user_rotation = rotation_matrix(reference["bottle_turn"]["rotation"])
user_scale = np.diag(reference["bottle_turn"]["scale"])
bottle_rotation = rotation_matrix(bottle["rotation"])
bottle_base = bottle_center + user_rotation @ user_scale @ bottle_rotation @ (
    np.array([bottle_origin[0], 7.72, bottle_origin[2]]) - bottle_origin
)
support_center = bottle_base + np.array([0.0, -0.18, 0.58])
left_shoulder = support_center + np.array([-7.5, -5.6, 6.0])
left_hidden = np.array([-7.0, -12.0, 10.0])

# time, horizontal support offset, vertical arc, depth arc, reach amount
LEFT_MOTION = [
    [0.00, -0.80, -0.25, 0.10, 0.00],
    [0.72, -0.80, -0.25, 0.10, 0.00],
    [0.94, -0.90, -0.42, 0.34, 0.32],
    [1.18, -0.58, -0.08, 0.10, 1.00],
    [1.32, -0.58, -0.08, 0.10, 1.00],
    [1.95, 0.38, -0.02, 0.14, 1.00],
    [2.28, 0.52, -0.05, 0.16, 1.00],
    [2.58, 0.52, -0.05, 0.16, 1.00],
    [3.08, 0.06, -0.18, 0.24, 1.00],
    [3.52, -0.42, -0.08, 0.14, 1.00],
    [4.08, 0.00, -0.08, 0.12, 1.00],
    [4.46, -0.52, -0.34, 0.30, 0.68],
    [4.68, -0.80, -0.25, 0.10, 0.00],
    [5.70, -0.80, -0.25, 0.10, 0.00],
]


def left_arm_pose(point):
    _, horizontal, vertical, depth, reach = point
    contact = support_center + np.array([horizontal, vertical, depth])
    endpoint = left_hidden * (1 - reach) + contact * reach
    direction = contact - left_shoulder
    target_rotation = matrix_euler(aim_local_y(direction))
    return endpoint - left_origin, target_rotation - np.array(left_arm["rotation"])


left_poses = [left_arm_pose(point) for point in LEFT_MOTION]
inspect = blank_animation("inspect", INSPECT_LENGTH)
basics(inspect)
track(
    inspect,
    "viewmodel_motion",
    "position",
    [[time, *(np.array(values) + reference["viewmodel_motion"]["position"])] for time, *values in INSPECT_ROOT],
)
track(inspect, inspection_tilt["name"], "rotation", INSPECT_TILT)
track(inspect, bottle_turn["name"], "rotation", BOTTLE_TURN)
track(
    inspect,
    right_arm["name"],
    "position",
    [
        [0.00, *reference[right_arm["name"]]["position"]],
        [0.42, *(reference[right_arm["name"]]["position"] + np.array([-0.08, 0.08, 0.03]))],
        [1.32, *(reference[right_arm["name"]]["position"] + np.array([-0.10, 0.06, 0.03]))],
        [2.28, *(reference[right_arm["name"]]["position"] + np.array([0.06, 0.02, 0.01]))],
        [2.58, *(reference[right_arm["name"]]["position"] + np.array([0.06, 0.02, 0.01]))],
        [3.52, *(reference[right_arm["name"]]["position"] + np.array([-0.04, 0.04, 0.02]))],
        [4.08, *(reference[right_arm["name"]]["position"] + np.array([0.00, 0.02, 0.01]))],
        [4.55, *(reference[right_arm["name"]]["position"] + np.array([-0.05, 0.04, 0.02]))],
        [5.70, *reference[right_arm["name"]]["position"]],
    ],
)
track(
    inspect,
    right_arm["name"],
    "rotation",
    [
        [0.00, *reference[right_arm["name"]]["rotation"]],
        [0.42, *(reference[right_arm["name"]]["rotation"] + np.array([-2.0, -0.8, 0.5]))],
        [0.70, *(reference[right_arm["name"]]["rotation"] + np.array([-1.2, -0.4, 0.2]))],
        [2.28, *(reference[right_arm["name"]]["rotation"] + np.array([-0.8, 0.6, -0.3]))],
        [2.58, *(reference[right_arm["name"]]["rotation"] + np.array([-0.8, 0.6, -0.3]))],
        [3.52, *(reference[right_arm["name"]]["rotation"] + np.array([-1.2, -0.2, 0.15]))],
        [4.08, *(reference[right_arm["name"]]["rotation"] + np.array([-1.0, 0.1, -0.05]))],
        [4.55, *(reference[right_arm["name"]]["rotation"] + np.array([-1.0, -0.3, 0.15]))],
        [5.70, *reference[right_arm["name"]]["rotation"]],
    ],
)
track(
    inspect,
    left_arm["name"],
    "position",
    [[point[0], *pose[0]] for point, pose in zip(LEFT_MOTION, left_poses)],
)
track(
    inspect,
    left_arm["name"],
    "rotation",
    [[point[0], *pose[1]] for point, pose in zip(LEFT_MOTION, left_poses)],
)
camera_tracks(
    inspect,
    [
        [0.00, 0, 0, 0],
        [0.52, 0.08, -0.07, 0.14],
        [0.78, 0.025, -0.02, 0.06],
        [1.16, -0.035, 0.025, 0.035],
        [1.95, 0.055, -0.04, 0.09],
        [2.28, 0.025, -0.01, 0.05],
        [2.58, 0.025, -0.01, 0.05],
        [3.52, 0.06, -0.04, 0.09],
        [4.08, 0.015, 0.005, 0.03],
        [4.55, -0.025, 0.025, 0.015],
        [5.12, 0.015, -0.01, -0.025],
        [5.70, 0, 0, 0],
    ],
    [
        [0.00, 0, 0, 0],
        [0.24, 0.42, -0.22, 0.14],
        [0.52, 1.85, -1.05, 0.62],
        [0.78, 0.78, -0.35, 0.22],
        [1.16, 1.20, 0.32, -0.30],
        [1.95, 2.15, -1.10, 0.72],
        [2.28, 1.30, -0.34, 0.28],
        [2.58, 1.20, -0.22, 0.22],
        [3.52, 2.05, -0.92, 0.66],
        [4.08, 0.84, 0.02, 0.14],
        [4.55, 0.34, 0.26, -0.12],
        [5.12, -0.24, 0.14, -0.14],
        [5.38, 0.08, -0.04, 0.05],
        [5.70, 0, 0, 0],
    ],
)

pose_reference = blank_animation("pose_reference", 1.0)
basics(pose_reference)
constant(pose_reference, "viewmodel_motion", "position", reference["viewmodel_motion"]["position"])
for channel_name in ("position", "rotation"):
    constant(pose_reference, right_arm["name"], channel_name, reference[right_arm["name"]][channel_name])
constant(pose_reference, left_arm["name"], "position", left_hidden - left_origin)
camera_tracks(pose_reference, [[0, 0, 0, 0]], [[0, 0, 0, 0]])


def evaluate_keys(keys, time, channel_name):
    channel_keys = sorted(
        [key for key in keys if key["channel"] == channel_name], key=lambda key: key["time"]
    )
    if not channel_keys:
        return np.ones(3) if channel_name == "scale" else np.zeros(3)
    if time <= channel_keys[0]["time"]:
        return key_vector(channel_keys[0])
    for left_key, right_key in zip(channel_keys, channel_keys[1:]):
        if time <= right_key["time"]:
            amount = (time - left_key["time"]) / (right_key["time"] - left_key["time"])
            if left_key.get("interpolation") == "step":
                return key_vector(left_key)
            if left_key.get("interpolation") != "bezier":
                return key_vector(left_key) + (key_vector(right_key) - key_vector(left_key)) * amount
            return (
                (1 - amount) ** 3 * key_vector(left_key)
                + 3 * (1 - amount) ** 2 * amount * (key_vector(left_key) + left_key["bezier_right_value"])
                + 3 * (1 - amount) * amount**2 * (key_vector(right_key) + right_key["bezier_left_value"])
                + amount**3 * key_vector(right_key)
            )
    return key_vector(channel_keys[-1])


def combine_animations(name, length, segments):
    animation = blank_animation(name, length)
    identifiers = {identifier for _, _, clip in segments for identifier in clip["animators"]}
    for identifier in identifiers:
        channels = {
            key["channel"]
            for _, _, clip in segments
            for key in clip["animators"].get(identifier, {}).get("keyframes", [])
        }
        target = {
            "name": by_id[identifier]["name"],
            "type": "camera" if by_id[identifier].get("type") == "camera" else "bone",
            "keyframes": [],
        }
        for channel_name in channels:
            combined = []
            for start, end, clip in segments:
                source_keys = clip["animators"].get(identifier, {}).get("keyframes", [])
                matching = [
                    deepcopy(key)
                    for key in source_keys
                    if key["channel"] == channel_name and key["time"] <= end - start
                ]
                if not matching:
                    matching = bezier_keys(
                        channel_name,
                        [[0, *evaluate_keys(source_keys, 0, channel_name)]],
                        f"{name}:{identifier}:missing",
                    )
                for key in matching:
                    key["time"] = round(key["time"] + start, 6)
                final_key = bezier_keys(
                    channel_name,
                    [[end, *evaluate_keys(source_keys, end - start, channel_name)]],
                    f"{name}:{identifier}:end",
                )[0]
                combined += matching + [final_key]
            merged = {}
            for key in combined:
                time = key["time"]
                if time in merged:
                    key["bezier_left_time"] = merged[time].get("bezier_left_time", [0] * 3)
                    key["bezier_left_value"] = merged[time].get("bezier_left_value", [0] * 3)
                merged[time] = key
            keys = sorted(merged.values(), key=lambda key: key["time"])
            if all(np.max(np.abs(key_vector(key) - key_vector(keys[0]))) < 1e-7 for key in keys):
                keys = keys[:1]
            for key in keys:
                key["uuid"] = uid(f"{name}:{identifier}:{channel_name}:{key['time']}")
            target["keyframes"] += keys
        animation["animators"][identifier] = target
    return animation


hold_after = deepcopy(pose_reference)
constant(hold_after, bottle_turn["name"], "rotation", [0, 360, 0])
stow_after = deepcopy(put_away)
constant(stow_after, bottle_turn["name"], "rotation", [0, 360, 0])
empty_hand = blank_animation("empty_hand_reference", 1.0)
basics(empty_hand)
constant(
    empty_hand,
    "viewmodel_motion",
    "position",
    np.array(PUT_ROOT[-1][1:]) + reference["viewmodel_motion"]["position"],
)
constant(
    empty_hand,
    right_arm["name"],
    "position",
    np.array(PUT_HAND[-1][1:]) + reference[right_arm["name"]]["position"],
)
constant(
    empty_hand,
    right_arm["name"],
    "rotation",
    np.array(PUT_ROTATION[-1][1:]) + reference[right_arm["name"]]["rotation"],
)
constant(empty_hand, left_arm["name"], "position", left_hidden - left_origin)
constant(empty_hand, bottle_turn["name"], "rotation", [0, 360, 0])
camera_tracks(empty_hand, [[0, 0, 0, 0]], [[0, 0, 0, 0]])
preview_complete = combine_animations(
    "preview_complete",
    8.73,
    [
        (0.00, 0.92, draw),
        (0.92, 1.45, pose_reference),
        (1.45, 7.15, inspect),
        (7.15, 7.45, hold_after),
        (7.45, 8.43, stow_after),
        (8.43, 8.73, empty_hand),
    ],
)
preview_draw_hold_put_away = combine_animations(
    "preview_draw_hold_put_away",
    3.35,
    [(0.00, 0.92, draw), (0.92, 2.10, pose_reference), (2.10, 3.08, put_away), (3.08, 3.35, empty_hand)],
)


def simplify_channel(keys, channel_name, tolerance):
    channel_keys = sorted(
        [key for key in keys if key["channel"] == channel_name], key=lambda key: key["time"]
    )
    if len(channel_keys) < 3 or any(key.get("interpolation") == "step" for key in channel_keys):
        return channel_keys
    times = np.array([key["time"] for key in channel_keys])
    values = np.array([key_vector(key) for key in channel_keys])
    keep = {0, len(channel_keys) - 1}

    def split(start, end):
        if end <= start + 1:
            return
        amounts = (times[start + 1 : end] - times[start]) / (times[end] - times[start])
        predicted = values[start] + amounts[:, None] * (values[end] - values[start])
        errors = np.max(np.abs(values[start + 1 : end] - predicted), axis=1)
        index = int(np.argmax(errors)) + start + 1
        if max(errors) > tolerance:
            keep.add(index)
            split(start, index)
            split(index, end)

    split(0, len(channel_keys) - 1)
    return [channel_keys[index] for index in sorted(keep)]


retained = [
    deepcopy(animation)
    for animation in original["animations"]
    if animation["name"].split(".")[-1] in {"idle", "drink", "walk", "sprint", "jump"}
]
grip_local = np.linalg.solve(
    user_rotation @ user_scale @ bottle_rotation, right_origin - bottle_center
)
for animation in retained:
    for channel_name in ("position", "rotation", "scale"):
        constant(animation, calibration["name"], channel_name, reference["bottle_turn"][channel_name])
    bottle_animator = animation["animators"][bottle["uuid"]]
    right_animator = animation["animators"][right_arm["uuid"]]
    times = sorted(
        {
            key["time"]
            for key in bottle_animator["keyframes"]
            if key["channel"] in {"position", "rotation"}
        }
        | {0, animation["length"]}
    )
    right_positions = []
    for time in times:
        bottle_position = linear_channel(bottle_animator, "position", time)
        animated_rotation = rotation_matrix(
            np.array(bottle["rotation"]) + linear_channel(bottle_animator, "rotation", time)
        )
        grip_world = bottle_center + user_rotation @ user_scale @ (
            bottle_position + animated_rotation @ grip_local
        )
        right_positions.append(
            {
                "uuid": uid(f"retained:{animation['name']}:right:{time}"),
                "channel": "position",
                "time": time,
                "interpolation": "linear",
                "data_points": [dict(zip("xyz", (grip_world - right_origin).tolist()))],
            }
        )
    right_animator["keyframes"] = [
        key for key in right_animator["keyframes"] if key["channel"] != "position"
    ] + right_positions
    for key in right_animator["keyframes"]:
        if key["channel"] == "rotation":
            key["data_points"] = [
                dict(zip("xyz", (key_vector(key) + reference[right_arm["name"]]["rotation"]).tolist()))
            ]
    if left_arm["uuid"] in animation["animators"]:
        for key in animation["animators"][left_arm["uuid"]]["keyframes"]:
            if key["channel"] == "position":
                point = left_origin + key_vector(key)
                transformed = bottle_center + user_rotation @ user_scale @ (point - bottle_origin)
                key["data_points"] = [dict(zip("xyz", (transformed - left_origin).tolist()))]
    stopper_animator = animation["animators"].setdefault(
        groups["wooden_stopper"]["uuid"],
        {"name": "wooden_stopper", "type": "bone", "keyframes": []},
    )
    if animation["name"].endswith(".drink"):
        stopper_position_keys = [
            key for key in stopper_animator["keyframes"] if key["channel"] == "position"
        ]
        if stopper_position_keys:
            for key in stopper_position_keys:
                key["data_points"] = [
                    dict(zip("xyz", (key_vector(key) + STOPPER_SEAT_OFFSET).tolist()))
                ]
        else:
            constant(animation, "wooden_stopper", "position", STOPPER_SEAT_OFFSET)
    else:
        set_constant(animation, "wooden_stopper", "position", STOPPER_SEAT_OFFSET)
        set_constant(animation, "wooden_stopper", "rotation", [0, 0, 0])
        set_constant(animation, "wooden_stopper", "scale", [1, 1, 1])
    for animator in animation["animators"].values():
        keys = animator["keyframes"]
        animator["keyframes"] = sum(
            [
                simplify_channel(
                    keys,
                    channel_name,
                    {"position": 0.028, "rotation": 0.22, "scale": 0.002}[channel_name],
                )
                for channel_name in ("position", "rotation", "scale")
            ],
            [],
        )
    camera_tracks(animation, [[0, 0, 0, 0]], [[0, 0, 0, 0]])


def movement_animation(
    name,
    length,
    loop_mode,
    root_positions,
    root_rotations,
    _liquid_rotations,
    camera_positions,
    camera_rotations,
):
    animation = blank_animation(name, length)
    animation["loop"] = loop_mode
    basics(animation)
    constant(animation, "wooden_stopper", "rotation", [0, 0, 0])
    track(
        animation,
        "viewmodel_motion",
        "position",
        [
            [time, *(np.array(values) + reference["viewmodel_motion"]["position"])]
            for time, *values in root_positions
        ],
    )
    track(
        animation,
        "viewmodel_motion",
        "rotation",
        [
            [time, *(np.array(values) + reference["viewmodel_motion"]["rotation"])]
            for time, *values in root_rotations
        ],
    )
    for channel_name in ("position", "rotation"):
        constant(
            animation,
            right_arm["name"],
            channel_name,
            reference[right_arm["name"]][channel_name],
        )
    constant(animation, left_arm["name"], "position", left_hidden - left_origin)
    constant(animation, bottle_turn["name"], "rotation", [0, 0, 0])
    constant(animation, "wine_fill_preview", "rotation", [0, 0, 0])
    camera_tracks(animation, camera_positions, camera_rotations)
    return animation


walk = movement_animation(
    "walk",
    1.00,
    "loop",
    [
        [0.000, 0.00, 0.00, 0.00],
        [0.125, 0.09, -0.05, 0.02],
        [0.250, 0.14, 0.10, -0.05],
        [0.375, 0.07, -0.06, 0.02],
        [0.500, 0.00, 0.00, 0.00],
        [0.625, -0.09, -0.05, 0.02],
        [0.750, -0.14, 0.10, -0.05],
        [0.875, -0.07, -0.06, 0.02],
        [1.000, 0.00, 0.00, 0.00],
    ],
    [
        [0.000, 0.00, 0.00, 0.00],
        [0.125, 0.30, -0.25, 0.45],
        [0.250, -0.35, -0.55, 0.80],
        [0.375, 0.15, -0.20, 0.30],
        [0.500, 0.00, 0.00, 0.00],
        [0.625, 0.30, 0.25, -0.45],
        [0.750, -0.35, 0.55, -0.80],
        [0.875, 0.15, 0.20, -0.30],
        [1.000, 0.00, 0.00, 0.00],
    ],
    [
        [0.000, 0.00, 0.00, 0.00],
        [0.125, -0.45, 0.00, -0.80],
        [0.250, 0.60, 0.00, -1.25],
        [0.375, -0.20, 0.00, -0.45],
        [0.500, 0.00, 0.00, 0.00],
        [0.625, -0.45, 0.00, 0.80],
        [0.750, 0.60, 0.00, 1.25],
        [0.875, -0.20, 0.00, 0.45],
        [1.000, 0.00, 0.00, 0.00],
    ],
    [[0.00, 0, 0, 0], [0.25, 0.00, 0.015, -0.01], [0.50, 0, 0, 0], [0.75, 0.00, 0.015, -0.01], [1.00, 0, 0, 0]],
    [[0.00, 0, 0, 0], [0.25, 0.18, -0.10, 0.12], [0.50, 0, 0, 0], [0.75, 0.18, 0.10, -0.12], [1.00, 0, 0, 0]],
)

sprint = movement_animation(
    "sprint",
    0.68,
    "loop",
    [
        [0.000, 0.55, -1.05, 0.80],
        [0.085, 0.68, -1.22, 0.90],
        [0.170, 0.78, -0.72, 0.62],
        [0.255, 0.62, -1.16, 0.88],
        [0.340, 0.35, -1.05, 0.80],
        [0.425, 0.18, -1.22, 0.90],
        [0.510, 0.10, -0.72, 0.62],
        [0.595, 0.28, -1.16, 0.88],
        [0.680, 0.55, -1.05, 0.80],
    ],
    [
        [0.000, 7.0, -5.0, 6.0],
        [0.085, 8.0, -5.5, 7.5],
        [0.170, 5.5, -4.0, 4.0],
        [0.255, 7.5, -5.2, 6.8],
        [0.340, 7.0, -5.0, 6.0],
        [0.425, 8.0, -4.5, 4.5],
        [0.510, 5.5, -6.0, 8.0],
        [0.595, 7.5, -5.2, 6.4],
        [0.680, 7.0, -5.0, 6.0],
    ],
    [
        [0.000, 0.0, 0.0, 0.0],
        [0.085, -1.6, 0.0, -2.2],
        [0.170, 2.2, 0.0, -3.2],
        [0.255, -0.8, 0.0, -1.2],
        [0.340, 0.0, 0.0, 0.0],
        [0.425, -1.6, 0.0, 2.2],
        [0.510, 2.2, 0.0, 3.2],
        [0.595, -0.8, 0.0, 1.2],
        [0.680, 0.0, 0.0, 0.0],
    ],
    [[0.000, 0, 0, 0], [0.170, 0.01, 0.035, -0.02], [0.340, 0, 0, 0], [0.510, -0.01, 0.035, -0.02], [0.680, 0, 0, 0]],
    [[0.000, 0, 0, 0], [0.170, 0.65, -0.24, 0.28], [0.340, 0, 0, 0], [0.510, 0.65, 0.24, -0.28], [0.680, 0, 0, 0]],
)

jump = movement_animation(
    "jump",
    1.20,
    "hold",
    [
        [0.00, 0.00, 0.00, 0.00],
        [0.10, 0.00, -0.08, 0.00],
        [0.22, 0.00, -0.72, 0.20],
        [0.42, 0.06, -0.45, 0.12],
        [0.62, -0.04, 0.10, -0.06],
        [0.78, 0.00, 0.42, -0.16],
        [0.88, 0.00, -0.28, 0.10],
        [1.02, 0.00, 0.10, -0.03],
        [1.20, 0.00, 0.00, 0.00],
    ],
    [
        [0.00, 0.0, 0.0, 0.0],
        [0.10, 1.0, 0.0, 0.4],
        [0.22, 6.0, -0.5, 2.2],
        [0.42, 3.0, 0.3, 1.0],
        [0.62, -1.0, 0.2, -0.8],
        [0.78, -5.0, 0.0, -2.0],
        [0.88, 2.0, -0.2, 1.0],
        [1.02, -0.5, 0.0, -0.2],
        [1.20, 0.0, 0.0, 0.0],
    ],
    [
        [0.00, 0.0, 0.0, 0.0],
        [0.10, -0.8, 0.0, -0.4],
        [0.22, -3.5, 0.0, -2.0],
        [0.42, -1.4, 0.0, -0.8],
        [0.62, 1.2, 0.0, 0.8],
        [0.78, 4.5, 0.0, 2.6],
        [0.88, -2.2, 0.0, -1.2],
        [1.02, 0.6, 0.0, 0.3],
        [1.20, 0.0, 0.0, 0.0],
    ],
    [[0.00, 0, 0, 0], [0.22, 0, -0.035, 0.02], [0.62, 0, 0.015, -0.01], [0.78, 0, 0.055, -0.03], [1.02, 0, -0.015, 0.01], [1.20, 0, 0, 0]],
    [[0.00, 0, 0, 0], [0.22, -0.85, 0.0, 0.20], [0.62, 0.35, 0.0, -0.10], [0.78, 1.65, 0.0, -0.35], [0.88, -0.70, 0.0, 0.18], [1.20, 0, 0, 0]],
)

movement_replacements = {"walk": walk, "sprint": sprint, "jump": jump}
retained = [
    movement_replacements.get(animation["name"].split(".")[-1], animation)
    for animation in retained
]


def snapshot_animation(source, name, length):
    snapshot = blank_animation(name, length)
    for identifier, animator in source["animators"].items():
        channels = {key["channel"] for key in animator.get("keyframes", [])}
        for channel_name in channels:
            track_identifier(
                snapshot,
                identifier,
                channel_name,
                [[0, *evaluate_keys(animator["keyframes"], source["length"], channel_name)]],
            )
    return snapshot


drink_base = next(
    animation for animation in retained if animation["name"].endswith(".drink")
)
DRINK_HIDDEN_HAND_OFFSET = np.array([-0.8, 8.0, 1.5])
drink_stow = snapshot_animation(drink_base, "drink_stow_empty_bottle", 0.34)
replace_track(
    drink_stow,
    "viewmodel_motion",
    "position",
    [
        [0.00, 0, 0, 0],
        [0.12, 0.4, -4.8, 0.4],
        [0.34, *(np.array(DRAW_ROOT[0][1:]) + reference["viewmodel_motion"]["position"])],
    ],
)
set_constant(drink_stow, "wooden_stopper", "scale", [0, 0, 0])
set_constant(drink_stow, "recolored_original_neck", "scale", [1, 1, 1])
set_constant(drink_stow, "wine_fill_preview", "scale", [1, 0.03125, 1])
camera_tracks(
    drink_stow,
    [[0, 0, 0, 0], [0.18, 0.02, -0.03, 0.04], [0.34, 0, 0, 0]],
    [[0, 0, 0, 0], [0.18, 0.38, -0.12, 0.16], [0.34, 0, 0, 0]],
)

drink_recover = snapshot_animation(drink_stow, "drink_empty_hand_recover", 0.30)
right_end_position = evaluate_keys(
    drink_base["animators"][right_arm["uuid"]]["keyframes"],
    drink_base["length"],
    "position",
)
right_end_rotation = evaluate_keys(
    drink_base["animators"][right_arm["uuid"]]["keyframes"],
    drink_base["length"],
    "rotation",
)
replace_track(
    drink_recover,
    right_arm["name"],
    "rotation",
    [
        [0.00, *right_end_rotation],
        [0.30, *(np.array(DRAW_ROTATION[0][1:]) + reference[right_arm["name"]]["rotation"])],
    ],
)
replace_track(
    drink_recover,
    left_arm["name"],
    "position",
    [
        [
            0.00,
            *evaluate_keys(
                drink_base["animators"][left_arm["uuid"]]["keyframes"],
                drink_base["length"],
                "position",
            ),
        ],
        [0.30, *(left_hidden - left_origin)],
    ],
)
replace_track(
    drink_recover,
    right_arm["name"],
    "position",
    [
        [0.00, *right_end_position],
        [0.30, *(DRINK_HIDDEN_HAND_OFFSET + reference[right_arm["name"]]["position"])],
    ],
)
set_constant(drink_recover, "wooden_stopper", "scale", [0, 0, 0])
set_constant(drink_recover, "recolored_original_neck", "scale", [1, 1, 1])
set_constant(drink_recover, "wine_fill_preview", "scale", [1, 0.03125, 1])
camera_tracks(drink_recover, [[0, 0, 0, 0]], [[0, 0, 0, 0]])

new_bottle_pause = deepcopy(empty_hand)
new_bottle_pause["name"] = "animation.orange_wine.new_bottle_offscreen_pause"
new_bottle_pause["uuid"] = uid("animation:new_bottle_offscreen_pause")
set_constant(
    new_bottle_pause,
    right_arm["name"],
    "position",
    DRINK_HIDDEN_HAND_OFFSET + reference[right_arm["name"]]["position"],
)

drink_draw = deepcopy(draw)
drink_draw["name"] = "animation.orange_wine.drink_draw_new_bottle"
drink_draw["uuid"] = uid("animation:drink_draw_new_bottle")
replace_track(
    drink_draw,
    right_arm["name"],
    "position",
    [
        [0.00, *(DRINK_HIDDEN_HAND_OFFSET + reference[right_arm["name"]]["position"])],
        [0.12, *(DRINK_HIDDEN_HAND_OFFSET + reference[right_arm["name"]]["position"])],
        [0.20, *(np.array(DRAW_HAND[2][1:]) + reference[right_arm["name"]]["position"])],
        [0.28, *reference[right_arm["name"]]["position"]],
        [0.52, *reference[right_arm["name"]]["position"]],
        [0.92, *reference[right_arm["name"]]["position"]],
    ],
)
drink_cycle = combine_animations(
    "drink",
    7.34,
    [
        (0.00, 5.60, drink_base),
        (5.60, 5.94, drink_stow),
        (5.94, 6.24, drink_recover),
        (6.24, 6.42, new_bottle_pause),
        (6.42, 7.34, drink_draw),
    ],
)
retained = [drink_cycle if animation is drink_base else animation for animation in retained]

model["animations"] = [
    preview_complete,
    draw,
    put_away,
    inspect,
    pose_reference,
    empty_hand,
    drink_stow,
    drink_recover,
    new_bottle_pause,
    drink_draw,
    preview_draw_hold_put_away,
] + retained
for animation in model["animations"]:
    animation["selected"] = animation is preview_complete

model["editor_state"] = {
    "mode": "animate",
    "tool": "move_tool",
    "saved": True,
    "selected_elements": [],
    "selected_groups": [],
    "previews": {
        "main": {
            "position": [0, 16, 18],
            "target": [0, 16, 2],
            "orthographic": False,
            "zoom": 1,
            "angle": None,
        }
    },
}
model["name"] = "农场物语-橙子果酒-TACZ视角与检视重制-v7"
target = OUT / (model["name"] + ".bbmodel")
target.write_text(json.dumps(model, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")


def key_counts(value):
    return {
        animation["name"].split(".")[-1]: sum(
            len(animator["keyframes"]) for animator in animation["animators"].values()
        )
        for animation in value["animations"]
    }


def camera_is_animated(animation):
    keys = animation["animators"][camera["uuid"]]["keyframes"]
    return any(np.max(np.abs(key_vector(key))) > 1e-7 for key in keys)


original_elements = {element["uuid"]: element for element in original["elements"]}
cube_elements = [element for element in model["elements"] if element.get("type") == "cube"]
mod_path = ROOT.parent.parent.parent.parent / "mods" / "[农场物语-饮用与检视动画]farmingtales_drink_anim-1.2.0.jar"
report = {
    "source": str(SOURCE),
    "source_sha256": hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
    "reference_animation": reference_animation["name"],
    "reference_time": reference_time,
    "source_bottle_pose": {name: value.tolist() for name, value in source_bottle_pose.items()},
    "corrected_bottle_pose": {name: value.tolist() for name, value in reference["bottle_turn"].items()},
    "original_cube_count": sum(element.get("type") == "cube" for element in original["elements"]),
    "output_cube_count": len(cube_elements),
    "all_original_elements_unchanged": all(
        element == original_elements[element["uuid"]] for element in cube_elements
    ),
    "textures_unchanged": model["textures"] == original["textures"],
    "no_split_palm_or_sleeve_extensions": not any(
        token in item["name"]
        for item in model["elements"] + model["groups"]
        for token in ("left_palm", "sleeve_continuation")
    ),
    "left_arm_element": "left_arm_skin",
    "uniform_bottle_scale": reference["bottle_turn"]["scale"].tolist(),
    "stopper_seat_offset": STOPPER_SEAT_OFFSET.tolist(),
    "inspect_stopper_locked": [
        key_vector(key).tolist()
        for key in inspect["animators"][groups["wooden_stopper"]["uuid"]]["keyframes"]
        if key["channel"] == "position"
    ] == [STOPPER_SEAT_OFFSET.tolist()],
    "inspection_pauses": INSPECTION_PAUSES,
    "back_label_pause_stationary": all(
        np.max(np.abs(sample_points(BOTTLE_TURN, start) - sample_points(BOTTLE_TURN, end))) < 1e-7
        for start, end, _ in INSPECTION_PAUSES
    ),
    "camera_nonzero": {
        name: camera_is_animated(animation)
        for name, animation in (("draw", draw), ("put_away", put_away), ("inspect", inspect))
    },
    "movement_animations": {
        name: {
            "length": animation["length"],
            "loop": animation["loop"],
            "root_position_closed": bool(np.max(
                np.abs(
                    evaluate_keys(
                        animation["animators"][groups["viewmodel_motion"]["uuid"]]["keyframes"],
                        0,
                        "position",
                    )
                    - evaluate_keys(
                        animation["animators"][groups["viewmodel_motion"]["uuid"]]["keyframes"],
                        animation["length"],
                        "position",
                    )
                )
            ) < 1e-7),
            "stopper_locked": len(
                [
                    key
                    for key in animation["animators"][groups["wooden_stopper"]["uuid"]]["keyframes"]
                    if key["channel"] in {"position", "rotation", "scale"}
                ]
            ) == 3,
            "liquid_preview_static": [
                key_vector(key).tolist()
                for key in animation["animators"][groups["wine_fill_preview"]["uuid"]]["keyframes"]
                if key["channel"] == "rotation"
            ] == [[0.0, 0.0, 0.0]],
        }
        for name, animation in (("walk", walk), ("sprint", sprint), ("jump", jump))
    },
    "source_keyframes": key_counts(original),
    "output_keyframes": key_counts(model),
    "mod_sha256": hashlib.sha256(mod_path.read_bytes()).hexdigest(),
    "mod_untouched": hashlib.sha256(mod_path.read_bytes()).hexdigest()
    == "9ce7cf0dc9ccb096621faf4693432f75d58370ea7d34307a5d389e5b02a4cd8a",
    "delivered_sha256": hashlib.sha256(target.read_bytes()).hexdigest(),
}
assert report["source_sha256"] == "cdd4313c6f319c273325eb10d78ce650b9da759ec0b81baf15c5e615bd7436f2"
assert report["original_cube_count"] == report["output_cube_count"]
assert report["all_original_elements_unchanged"]
assert report["textures_unchanged"]
assert report["no_split_palm_or_sleeve_extensions"]
assert report["uniform_bottle_scale"] == [1.25, 1.25, 1.25]
assert report["stopper_seat_offset"] == [0.0, -0.06, 0.0]
assert report["reference_animation"].endswith(".draw") and report["reference_time"] == 0.9
assert report["inspect_stopper_locked"]
assert all(
    values["root_position_closed"]
    and values["stopper_locked"]
    and values["liquid_preview_static"]
    for values in report["movement_animations"].values()
)
assert report["back_label_pause_stationary"]
assert all(report["camera_nonzero"].values())
assert report["output_keyframes"]["inspect"] < 120
assert report["mod_untouched"]

(OUT / "检查记录.json").write_text(
    json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8"
)
(OUT / "作者关键姿态.json").write_text(
    json.dumps(
        {
            "draw_root": DRAW_ROOT,
            "put_away_root": PUT_ROOT,
            "inspect_root": INSPECT_ROOT,
            "inspect_tilt": INSPECT_TILT,
            "bottle_turn": BOTTLE_TURN,
            "left_arm_motion": LEFT_MOTION,
            "inspection_pauses": INSPECTION_PAUSES,
        },
        ensure_ascii=False,
        indent=2,
    ),
    encoding="utf-8",
)
print(json.dumps(report, ensure_ascii=False, indent=2))
