#version 150
uniform sampler2D DiffuseSampler;
uniform sampler2D AfterSampler;
uniform float Threshold;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec3 before = texture(DiffuseSampler, texCoord).rgb;
    vec3 after = texture(AfterSampler, texCoord).rgb;
    vec3 emission = max(after - before, vec3(0.0));
    float brightness = max(after.r, max(after.g, after.b));
    float gate = smoothstep(Threshold, Threshold + 0.10, brightness);
    fragColor = vec4(emission * gate, 0.0);
}
