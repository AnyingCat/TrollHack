#version 150

uniform sampler2D InputSampler;
uniform vec2 InputResolution;
uniform vec2 uSize;
uniform vec2 uLocation;
uniform vec4 radii;
uniform float Brightness;
uniform float Quality;
uniform vec4 color1;

in vec2 texCoord;

out vec4 fragColor;

float getCornerRadius(vec2 pos, vec2 size, vec4 radii) {
    vec2 halfSize = size / 2.0;
    vec2 centerPos = pos - halfSize;

    vec2 s = step(0.0, centerPos);

    return mix(
        mix(radii.x, radii.w, s.y),
        mix(radii.y, radii.z, s.y),
        s.x
    );
}

vec4 blur() {
    #define TAU 6.28318530718

    vec2 Radius = Quality / InputResolution.xy;

    vec2 uv = gl_FragCoord.xy / InputResolution.xy;
    vec4 Color = texture(InputSampler, uv);

    float step =  TAU / 16.0;

    for (float d = 0.0; d < TAU; d += step) {
        for (float i = 0.2; i <= 1.0; i += 0.2) {
            Color += texture(InputSampler, uv + vec2(cos(d), sin(d)) * Radius * i);
        }
    }

    Color /= 80;
    return (Color + color1) * Brightness;
}

void main() {
    vec2 pos = gl_FragCoord.xy - uLocation;
    vec2 halfSize = uSize / 2.0;
    vec2 center = uSize / 2.0;
    vec2 p = pos - center;

    float r = getCornerRadius(pos, uSize, radii);

    vec2 q = abs(p) - halfSize + r;
    float dist = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;

    float delta = fwidth(dist);
    float alpha = 1.0 - smoothstep(-delta, delta, dist);

    fragColor = vec4(blur().rgb, alpha);
}
