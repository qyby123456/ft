#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Time;
uniform float Strength;
uniform float Aspect;
in vec2 texCoord;
out vec4 fragColor;

float hash(vec2 point) {
    return fract(sin(dot(point, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 point) {
    vec2 cell = floor(point);
    vec2 local = fract(point);
    vec2 blend = local * local * (3.0 - 2.0 * local);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), blend.x),
            mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), blend.x), blend.y);
}

void main() {
    vec2 uv = texCoord;
    vec2 centered = (uv - 0.5) * vec2(1.7, 2.2);
    float radius = length(centered);
    float edge = smoothstep(0.40, 1.02, radius);
    float phase = mod(Time, 20.0) * 0.3141592654;
    vec2 flow = vec2(sin(phase), cos(phase));
    vec2 mistUV = (uv - 0.5) * vec2(Aspect, 1.0) * 5.0;
    float cloud = noise(mistUV + flow * 1.4) * 0.62;
    cloud += noise(mistUV * 2.1 - flow.yx * 1.8) * 0.28;
    cloud += noise(mistUV * 4.0 + flow * 0.6) * 0.10;
    vec2 drift = vec2(sin(phase * 2.0 + uv.y * 11.0), cos(phase + uv.x * 13.0)) * 0.008 * edge;
    vec4 frame = texture(Sampler0, clamp(uv + drift, vec2(0.001), vec2(0.999)));
    vec2 runeUV = uv + vec2(sin(phase * 2.0), cos(phase * 2.0)) * 0.0035 * edge;
    vec4 rune = texture(Sampler1, clamp(runeUV, vec2(0.001), vec2(0.999)));
    float breathing = 0.5 + 0.5 * sin(phase * 4.0);
    float fog = smoothstep(0.25, 0.85, cloud) * edge;
    float angle = atan(centered.y, centered.x);
    float ripple = sin(radius * 36.0 - sin(phase) * 2.8 + cloud * 6.0);
    float wisp = pow(max(0.0, ripple), 12.0) * fog;
    float curl = pow(max(0.0, sin(angle * 9.0 + radius * 17.0 + phase * 2.0)), 16.0) * edge;
    float alpha = Strength * clamp(0.20392 + frame.a * 0.52 + edge * 0.10 + fog * 0.17, 0.0, 0.94);
    vec3 color = vec3(0.022, 0.009, 0.045) + frame.rgb * 0.33;
    color += vec3(0.12, 0.025, 0.24) * wisp * (0.42 + breathing * 0.28);
    color += vec3(0.065, 0.018, 0.14) * curl * fog;
    color += rune.rgb * rune.a * edge * (0.30 + breathing * 0.30);
    fragColor = vec4(clamp(color, 0.0, 1.0), alpha);
}
