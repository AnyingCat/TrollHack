#version 150

uniform vec2 uSize;
uniform vec2 uLocation;
uniform float radius;
uniform vec4 color;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

void main() {
    vec2 halfSize = uSize / 2.0;
    float distance = roundedBoxSDF(gl_FragCoord.xy - uLocation - halfSize, halfSize, radius);
    float alpha = 1.0 - smoothstep(0.0, 1.0, distance);
    fragColor = vec4(color.rgb, color.a * alpha);
}