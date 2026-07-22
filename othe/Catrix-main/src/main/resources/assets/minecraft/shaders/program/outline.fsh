#version 330

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 resolution;
uniform int RenderMode;
uniform float FillOpacity;
uniform vec3 color;

uniform float radius;
uniform float divider;
uniform float maxSample;
uniform float quality;

out vec4 fragColor;

float quad(float x) {
    return x * x;
}

float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma));
}

float smoothGlowShader() {
    vec2 texelSize = 1.0 / resolution;
    float sigma = radius * 0.5;
    float totalWeight = 0.0;
    float alpha = 0.0;

    int sampleRadius = int(ceil(radius * quality));

    for (int x = -sampleRadius; x <= sampleRadius; x++) {
        for (int y = -sampleRadius; y <= sampleRadius; y++) {
            float distance = length(vec2(x, y));
            if (distance > radius) continue;

            float weight = gaussian(distance, sigma);
            totalWeight += weight;

            vec2 sampleCoord = texCoord + vec2(x, y) * texelSize * quality;
            vec4 sampleColor = texture(DiffuseSampler, sampleCoord);

            if (sampleColor.a > 0.0) {
                alpha += weight * sampleColor.a;
            }
        }
    }

    if (totalWeight > 0.0) {
        alpha /= totalWeight;
        alpha *= maxSample;
        alpha = clamp(alpha, 0.0, 1.0);
    }

    return alpha;
}

float advancedGlowShader() {
    vec2 texelSize = 1.0 / resolution;
    float totalAlpha = 0.0;
    float falloff = 1.0 / (divider * 0.1 + 0.1);

    int steps = int(radius * quality);
    float stepSize = 1.0 / float(steps);

    for (int i = 1; i <= steps; i++) {
        float currentRadius = float(i) * radius / float(steps);
        float weight = 1.0 - (float(i) / float(steps));

        for (int j = 0; j < 8; j++) {
            float angle = float(j) * 3.14159 / 4.0;
            vec2 offset = vec2(cos(angle), sin(angle)) * currentRadius * texelSize;

            vec4 sampleColor = texture(DiffuseSampler, texCoord + offset);
            if (sampleColor.a > 0.0) {
                totalAlpha += sampleColor.a * weight * falloff;
            }
        }
    }

    totalAlpha = clamp(totalAlpha * maxSample, 0.0, 1.0);
    return totalAlpha;
}

float glowShader() {
    vec2 texelSize = 1.0 / resolution;
    float glow = 0.0;
    int samples = int(radius * quality * 2.0);

    if (samples < 1) samples = 1;

    for (int i = 0; i < samples; i++) {
        float angle = float(i) * 6.28318530718 / float(samples);
        for (float r = 1.0; r <= radius; r += 1.0) {
            vec2 offset = vec2(cos(angle), sin(angle)) * r * texelSize * quality;
            vec4 sampleColor = texture(DiffuseSampler, texCoord + offset);

            if (sampleColor.a > 0.0) {
                float attenuation = 1.0 - (r / radius);
                float intensity = maxSample / (divider * 0.1 + 1.0);
                glow += sampleColor.a * attenuation * intensity;
            }
        }
    }

    return clamp(glow / float(samples), 0.0, 1.0);
}

void main() {
    vec4 current = texture(DiffuseSampler, texCoord);

    if (RenderMode == 2) { // Both
        if (current.a != 0.0) {
            fragColor = vec4(color, current.a * FillOpacity);
        } else {
            float alpha = glowShader();
            if (alpha > 0.0) {
                float smoothAlpha = 1.0 - exp(-alpha * 4.0);
                fragColor = vec4(color, smoothAlpha);
            } else {
                discard;
            }
        }
    } else if (RenderMode == 0) { // Fill
        if (current.a != 0.0) {
            fragColor = vec4(color, current.a * FillOpacity);
        } else {
            discard;
        }
    } else if (RenderMode == 1) { // Outline
        if (current.a != 0.0) {
            discard;
        } else {
            float alpha = glowShader();
            if (alpha > 0.0) {
                float smoothAlpha = 1.0 - exp(-alpha * 3.0);
                fragColor = vec4(color, smoothAlpha);
            } else {
                discard;
            }
        }
    } else {
        discard;
    }
}