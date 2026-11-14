#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

out vec4 lightTint;
out vec2 lightUV;
out float fogDistance;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;
    lightTint = Color;
    lightUV = UV0;
    fogDistance = FogShape == 0 ? length(viewPosition.xyz) : max(length(viewPosition.xz), abs(viewPosition.y));
}
