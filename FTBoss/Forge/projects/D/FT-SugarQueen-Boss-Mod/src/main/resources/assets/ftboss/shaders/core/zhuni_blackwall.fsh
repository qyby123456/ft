#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Time;
uniform float Strength;
uniform float Seed;
uniform vec2 ScreenSize;
in vec2 texCoord;
out vec4 fragColor;

float hash(float value) {
    return fract(sin(value * 127.1 + Seed * 0.17) * 43758.5453);
}

void main() {
    vec2 uv = texCoord;
    if (Strength <= 0.0) {
        fragColor = vec4(0.0);
        return;
    }
    float frame = floor(Time * 12.0);
    float transition = smoothstep(0.0, 1.0, fract(Time * 12.0));
    float peripheral = smoothstep(0.22, 0.90, max(abs(uv.x - 0.5), abs(uv.y - 0.5)) * 2.0);
    float distortion = Strength * mix(0.18, 1.0, peripheral);
    float row = floor(uv.y * 54.0);
    float fault = smoothstep(0.78, 0.88, mix(hash(row + frame * 0.37), hash(row + (frame + 1.0) * 0.37), transition));
    float offset = (mix(hash(row * 4.7 + frame), hash(row * 4.7 + frame + 1.0), transition) - 0.5) * 0.055 * fault * distortion;
    offset += sin(uv.y * 23.0 + Time * 4.0) * 0.0018 * distortion;
    vec2 warped = clamp(uv + vec2(offset, 0.0), vec2(0.002), vec2(0.998));
    float split = (0.0018 + fault * 0.004) * distortion;
    vec3 scene = vec3(texture(Sampler0, clamp(warped + vec2(split, 0.0), vec2(0.002), vec2(0.998))).r,
            texture(Sampler0, warped).g,
            texture(Sampler0, clamp(warped - vec2(split, 0.0), vec2(0.002), vec2(0.998))).b);
    vec2 grid = vec2(floor(uv.x * 78.0), floor(uv.y * 44.0));
    vec2 cellUV = fract((grid + vec2(frame * 3.0, -frame) + Seed * 0.013) / 256.0);
    vec4 cell = mix(texture(Sampler1, cellUV), texture(Sampler1, fract(cellUV + vec2(3.0, -1.0) / 256.0)), transition);
    float edge = pow(clamp(max(abs(uv.x - 0.5) * 2.0, abs(uv.y - 0.5) * 1.7), 0.0, 1.0), 3.0);
    float curtain = smoothstep(0.62, 1.0, edge + cell.r * 0.35);
    float lane = fract(uv.x * 78.0);
    float trail = sin(uv.y * 17.0 - Time * (0.7 + cell.b) + floor(uv.x * 78.0) * 1.7);
    float vein = step(0.91, trail) * step(0.60, cell.g) * (1.0 - step(0.24, lane));
    float seam = step(0.955, fract(uv.y * 44.0 + cell.b * 0.2));
    float darkPlate = fault * step(0.6, hash(floor(uv.x * 12.0) + row * 2.3)) * 0.26 * mix(0.25, 1.0, peripheral);
    scene *= 1.0 - Strength * (curtain * 0.76 + edge * 0.18 + darkPlate);
    float red = Strength * (curtain * cell.g * 0.38 + vein * (0.03 + edge * 0.77) + seam * fault * 0.4 * peripheral);
    scene += vec3(0.94, 0.025, 0.075) * red;
    scene.r += Strength * fault * 0.035 * peripheral;
    float scan = sin(uv.y * ScreenSize.y * 1.5708 + Time * 2.0) * 0.012 * distortion;
    float coverage = peripheral * smoothstep(0.0, 0.12, Strength);
    fragColor = vec4(clamp(scene + scan, 0.0, 1.0), coverage);
}
