#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in vec4 lightTint;
in vec2 lightUV;
in float fogDistance;

out vec4 fragColor;

void main() {
    vec4 radiance = texture(Sampler0, lightUV) * lightTint * ColorModulator;
    if (radiance.a < 0.014) discard;
    float veil = smoothstep(FogStart, max(FogStart + 0.0001, FogEnd), fogDistance);
    fragColor = vec4(mix(radiance.rgb, FogColor.rgb, veil * FogColor.a), radiance.a);
}
