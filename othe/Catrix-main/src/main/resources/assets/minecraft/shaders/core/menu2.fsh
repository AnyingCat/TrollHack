#version 150

uniform vec2 uSize;
uniform vec2 resolution;
uniform vec2 mouse;
uniform float Time;
uniform vec4 color;

out vec4 fragColor;

mat2 mm2(in float a){float c = cos(a), s = sin(a);return mat2(c,s,-s,c);}
const mat2 m2 = mat2(0.95534, 0.29552, -0.29552, 0.95534);
float tri(in float x){return clamp(abs(fract(x)-0.5),0.01,0.49);}
vec2 tri2(in vec2 p){return vec2(tri(p.x)+tri(p.y),tri(p.y+tri(p.x)));}

float triNoise2d(in vec2 p, float spd)
{
    float z=1.8;
    float z2=1.1;
    float rz = 0.0;
    p *= mm2(p.x*0.06);
    vec2 bp = p;
    for (int i=0; i<5; i++)
    {
        vec2 dg = tri2(bp*1.85)*0.75;
        dg *= mm2(Time*spd);
        p -= dg/z2;

        bp *= 1.3;
        z2 *= 0.45;
        z *= 0.42;
        p *= 1.21 + (rz-1.0)*0.02;

        rz += tri(p.x+tri(p.y))*z;
        p *= -m2;
    }
    return clamp(1.0/pow(rz*29.0, 1.3),0.0,0.55);
}

float hash21(in vec2 n){ return fract(sin(dot(n, vec2(12.9898, 4.1414))) * 43758.5453); }

vec4 aurora(vec3 ro, vec3 rd)
{
    vec4 col = vec4(0.0);
    vec4 avgCol = vec4(0.0);

    for(int i=0; i<50; i++)
    {
        float of = 0.006*hash21(gl_FragCoord.xy)*smoothstep(0.0,15.0, float(i));
        float pt = ((0.8+pow(float(i),1.4)*0.002)-ro.y)/(rd.y*2.0+0.4);
        pt -= of;
        vec3 bpos = ro + pt*rd;
        vec2 p = bpos.zx;
        float rzt = triNoise2d(p, 0.06);
        vec4 col2 = vec4(0.0,0.0,0.0, rzt);
        col2.rgb = (sin(2.0-vec3(-10.0,-0.7, 1.3)+float(i)*0.043)*0.5+0.5)*rzt;
        avgCol =  mix(avgCol, col2, 0.5);
        col += avgCol*exp2(-float(i)*0.065 - 2.5)*smoothstep(0.7,5.0, float(i));
    }

    col *= (clamp(rd.y*17.0+0.4,0.0,1.0));

    return col*1.8;
}

vec3 nmzHash33(vec3 q)
{
    uvec3 p = uvec3(ivec3(q));
    p = p*uvec3(374761393U, 1103515245U, 668265263U) + p.zxy + p.yzx;
    p = p.yzx*(p.zxy^(p >> 3U));
    return vec3(p^(p >> 16U))*(1.0/vec3(0xffffffffU));
}

vec3 stars(in vec3 p)
{
    vec3 c = vec3(0.0);
    float res = pow(resolution.x / 400.0, 0.5) * 400.0;

    for (int i=0; i<4; i++)
    {
        vec3 q = fract(p*(0.15*res))-0.5;
        vec3 id = floor(p*(0.15*res));
        vec2 rn = nmzHash33(id).xy;
        float c2 = 1.0-smoothstep(0.0,0.7, length(q));
        c2 *= step(rn.x,0.0005+float(i)*float(i)*0.001);
        c += c2*(mix(vec3(1.0,0.49,0.1),vec3(0.75,0.9,1.0),rn.y)*0.1+0.9);
        p *= 1.3;
    }
    return c*c*0.8;
}

vec3 bg(in vec3 rd)
{
    float sd = dot(normalize(vec3(-4.0, -0.6, -4.0)), rd)*0.5+0.5;
    sd = pow(sd, 5.0);
    vec3 col = mix(vec3(0.08,0.05,0.15), vec3(0.4,0.1,0.5), sd);
    return col*0.89;
}

void main()
{
    vec2 q = gl_FragCoord.xy / resolution;
    vec2 p = q - 0.5;
    p.x *= resolution.x / resolution.y;

    vec3 ro = vec3(0.0,0.0,-6.7);
    vec3 rd = normalize(vec3(p,1.3));

    vec2 mo = mouse.xy / resolution - 0.5;
    mo.x *= resolution.x / resolution.y;

    float mouseRotation = mo.x * 0.5;
    float autoRotation = sin(Time * 0.03) * 0.1;

    float smoothRotation = mouseRotation * (1.0 - exp(-Time * 0.1)) + autoRotation;

    rd.xz *= mm2(smoothRotation);

    vec3 col = vec3(0.0);
    vec3 brd = rd;
    float fade = smoothstep(0.0, 0.01, abs(brd.y)) * 0.1 + 0.9;

    col = bg(rd) * fade;

    if (rd.y > 0.0) {
        vec4 aur = smoothstep(0.0,1.5,aurora(ro,rd))*fade;
        col += stars(rd);
        col = col*(1.0-aur.a) + aur.rgb;
    }
    else
    {
        rd.y = abs(rd.y);
        col = bg(rd)*fade*0.6;
        vec4 aur = smoothstep(0.0,2.5,aurora(ro,rd));
        col += stars(rd)*0.1;
        col = col*(1.0-aur.a) + aur.rgb;
        vec3 pos = ro + ((0.5-ro.y)/rd.y)*rd;
        float nz2 = triNoise2d(pos.xz*vec2(0.5,0.7), 0.0);
        col += mix(vec3(0.2,0.25,0.5)*0.08,vec3(0.3,0.3,0.5)*0.7, nz2*0.4);
    }

    fragColor = vec4(col, 1.0);
}