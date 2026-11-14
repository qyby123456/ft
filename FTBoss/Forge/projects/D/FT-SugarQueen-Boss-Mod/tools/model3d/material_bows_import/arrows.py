from PIL import Image, ImageDraw


def texture_bytes():
    import io

    image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.rectangle((0, 0, 57, 63), fill="#b78b52")
    draw.rectangle((59, 0, 63, 5), fill="#aebbc0")
    draw.rectangle((59, 8, 63, 16), fill="#e4e6da")
    draw.rectangle((59, 20, 63, 25), fill="#78512e")
    output = io.BytesIO()
    image.save(output, format="PNG")
    return output.getvalue()


def arrow_elements(nock):
    center_x, center_y, tail = 9.7, nock[1], nock[2]
    tip = tail - 30
    result = []
    regions = {"shaft": (0, 0), "head": (59, 0), "feather": (59, 8), "nock": (59, 20)}

    def box(name, start, end, material):
        size = [end[axis] - start[axis] for axis in range(3)]
        origin_u, origin_v = regions[material]
        faces = {}
        for face in ("north", "east", "south", "west", "up", "down"):
            width = size[0] if face in ("north", "south", "up", "down") else size[2]
            height = size[2] if face in ("up", "down") else size[1]
            uv = [origin_u / 4, origin_v / 4, (origin_u + width * 2) / 4, (origin_v + height * 2) / 4]
            if face == "south":
                uv = [uv[2], uv[1], uv[0], uv[3]]
            faces[face] = {"texture": "#arrow", "uv": uv}
        result.append({"name": name, "from": start, "to": end, "faces": faces})

    box("arrow_shaft", [center_x - 0.18, center_y - 0.18, tip + 1],
        [center_x + 0.18, center_y + 0.18, tail - 0.2], "shaft")
    box("arrow_head", [center_x - 0.55, center_y - 0.55, tip + 0.3],
        [center_x + 0.55, center_y + 0.55, tip + 1.1], "head")
    box("arrow_tip", [center_x - 0.25, center_y - 0.25, tip],
        [center_x + 0.25, center_y + 0.25, tip + 0.4], "head")
    box("arrow_fletching", [center_x - 0.06, center_y - 0.8, tail - 2.4],
        [center_x + 0.06, center_y + 0.8, tail - 0.6], "feather")
    box("arrow_fletching_cross", [center_x - 0.8, center_y - 0.06, tail - 2.4],
        [center_x + 0.8, center_y + 0.06, tail - 0.6], "feather")
    box("arrow_nock", [nock[0] - 0.1, center_y - 0.18, tail - 0.2],
        [center_x + 0.2, center_y + 0.18, tail + 0.2], "nock")
    return result
