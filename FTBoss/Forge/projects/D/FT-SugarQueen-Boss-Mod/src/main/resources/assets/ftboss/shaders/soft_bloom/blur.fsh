#version 150
uniform sampler2D DiffuseSampler;
uniform vec2 Step;
in vec2 texCoord;
out vec4 fragColor;
void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb * 0.2270270270;
    color += texture(DiffuseSampler, texCoord + Step * 1.3846153846).rgb * 0.3162162162;
    color += texture(DiffuseSampler, texCoord - Step * 1.3846153846).rgb * 0.3162162162;
    color += texture(DiffuseSampler, texCoord + Step * 3.2307692308).rgb * 0.0702702703;
    color += texture(DiffuseSampler, texCoord - Step * 3.2307692308).rgb * 0.0702702703;
    fragColor = vec4(color, 0.0);
}
