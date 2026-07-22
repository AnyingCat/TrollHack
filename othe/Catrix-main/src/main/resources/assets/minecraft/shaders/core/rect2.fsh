#version 150

uniform vec2 uSize;
uniform vec2 uLocation;
uniform float radius;
uniform vec4 color;
uniform vec4 color2;
uniform float colorSplit;

out vec4 fragColor;

float roundedBoxSDF(vec2 center, vec2 size, float radius) {
    return length(max(abs(center) - size + radius, 0.0)) - radius;
}

void main() {
    vec2 halfSize = uSize / 2.0;
    vec2 fragPos = gl_FragCoord.xy - uLocation - halfSize;
    float distance = roundedBoxSDF(fragPos, halfSize, radius);
    float alpha = 1.0 - smoothstep(0.0, 1.0, distance);

    float relativeY = (fragPos.y + halfSize.y) / uSize.y;

    vec4 finalColor = (relativeY < colorSplit) ? color : color2;
    fragColor = vec4(finalColor.rgb, finalColor.a * alpha);
}