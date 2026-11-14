#version 150
uniform sampler2D BloomSampler;
uniform float Strength;
uniform float AimProtection;
uniform float Aspect;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    float center = length((texCoord - vec2(0.5)) * vec2(Aspect, 1.0));
    float visibility = mix(1.0, smoothstep(0.012, 0.035, center), AimProtection);
    fragColor = vec4(texture(BloomSampler, texCoord).rgb * Strength * visibility, 0.0);
}
