#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
    vec4 current = texture(DiffuseSampler, texCoord);
    float silhouette = current.a;

    fragColor = vec4(silhouette, 0.0, 0.0, silhouette);
}
