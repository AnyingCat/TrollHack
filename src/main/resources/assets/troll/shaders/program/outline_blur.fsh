#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 BlurDir;
uniform float BlurRadius;

out vec4 fragColor;

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);

    vec2 dir = BlurDir * oneTexel;

    float sigma = max(BlurRadius * 0.5, 0.5);
    float twoSigmaSq = 2.0 * sigma * sigma;

    float blurSum = center.a;
    float weightSum = 1.0;

    const int MAX_R = 24;
    for (int i = 1; i <= MAX_R; i += 2) {
        float fi = float(i);
        if (fi > BlurRadius) break;

        float w1 = exp(-fi * fi / twoSigmaSq);
        float fi1 = float(i + 1);

        if (fi1 > BlurRadius) {
            float aPos = texture(DiffuseSampler, texCoord + dir * fi).a;
            float aNeg = texture(DiffuseSampler, texCoord - dir * fi).a;
            blurSum += (aPos + aNeg) * w1;
            weightSum += 2.0 * w1;
            break;
        }

        float w2 = exp(-fi1 * fi1 / twoSigmaSq);
        float wSum = w1 + w2;
        float sampleOffset = fi + w2 / wSum;

        float aPos = texture(DiffuseSampler, texCoord + dir * sampleOffset).a;
        float aNeg = texture(DiffuseSampler, texCoord - dir * sampleOffset).a;
        blurSum += (aPos + aNeg) * wSum;
        weightSum += 2.0 * wSum;
    }

    float blurred = blurSum / max(weightSum, 0.001);

    fragColor = vec4(center.r, center.g, 0.0, blurred);
}
