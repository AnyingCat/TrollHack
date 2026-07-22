#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform int RenderMode;
uniform float FillOpacity;
uniform vec3 color;
uniform float Thickness;
uniform float Intensity;

out vec4 fragColor;

void main() {
    vec4 sample_ = texture(DiffuseSampler, texCoord);
    float silhouette = sample_.r;
    float blurred = sample_.a;

    if (RenderMode == 0) {
        if (silhouette > 0.01) {
            fragColor = vec4(color, FillOpacity);
        } else {
            discard;
        }
        return;
    }

    if (blurred < 0.01 && silhouette < 0.01) {
        discard;
    }

    float sb = 0.0;
    float sw = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            float d = float(x * x + y * y);
            float w = exp(-d / 2.0);
            sb += texture(DiffuseSampler, texCoord + vec2(float(x), float(y)) * oneTexel).r * w;
            sw += w;
        }
    }
    sb /= sw;

    float lineAlpha = 0.0;
    if (sb < 0.6) {
        float dilation = 0.0;
        float dilRadius = max(Thickness, 0.5);
        const int NUM_ANGLES = 16;
        for (int i = 0; i < NUM_ANGLES; i++) {
            float angle = float(i) * (6.28318 / float(NUM_ANGLES));
            vec2 offset = vec2(cos(angle), sin(angle)) * oneTexel * dilRadius;
            dilation = max(dilation, texture(DiffuseSampler, texCoord + offset).r);
        }
        lineAlpha = dilation * (1.0 - smoothstep(0.3, 0.6, sb));
    }

    float outsideMask = 1.0 - smoothstep(0.5, 0.85, blurred);
    float glowThreshold = smoothstep(0.01, 0.04, blurred);
    float glowAlpha = pow(blurred, 0.5) * Intensity * 1.5 * outsideMask * glowThreshold;

    float bloomAlpha = smoothstep(0.25, 0.4, blurred) * (1.0 - smoothstep(0.4, 0.55, blurred)) * Intensity;

    float outlineAlpha = clamp(max(max(lineAlpha, glowAlpha), bloomAlpha), 0.0, 1.0);

    if (RenderMode == 1) {
        if (outlineAlpha > 0.01) {
            fragColor = vec4(color, outlineAlpha);
        } else {
            discard;
        }
    } else {
        if (silhouette > 0.01) {
            fragColor = vec4(color, max(FillOpacity, outlineAlpha));
        } else if (outlineAlpha > 0.01) {
            fragColor = vec4(color, outlineAlpha);
        } else {
            discard;
        }
    }
}
