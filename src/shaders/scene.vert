#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

out vec2 outTextCoord;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;

// fbm goes in here

/*
public float fbmValues(float frequency, float amplitude, float gain, float lacunarity, int octaves, int subdivisions, float x, float y) {
        float fbmValue = 0;
        OpenSimplexNoise noise = new OpenSimplexNoise(12345L);
        for (int i = 0; i < octaves; ++i){
            fbmValue += (amplitude * (float)noise.eval(x * frequency, y * frequency));
            frequency *= lacunarity;
            amplitude *= gain;
        }

        return fbmValue;
    }

(0.01f, 0.1f, 0.5f, 2.0f, 16,)
*/

// noise function doesnt exist here

float random (in vec2 st) {
    return fract(sin(dot(st.xy,
    vec2(12.9898,78.233)))*
    43758.5453123);
}

float noise (in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    // Four corners in 2D of a tile
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) +
    (c - a)* u.y * (1.0 - u.x) +
    (d - b) * u.x * u.y;
}

float map(float value, float istart, float istop, float ostart, float ostop) {
    return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
}

void main()
{

    vec3 pos = position;
    float frequency = 0.05f;
    float amplitude = 0.2f;
    float gain = 0.5f;
    float lacunarity = 2.0f;
    int octaves = 16;

    float fbmValue = 0.0f;

    for (int i = 0; i < octaves; ++i){
        fbmValue += (amplitude * noise(vec2(pos.x * frequency, pos.z * frequency)));
        frequency *= lacunarity;
        amplitude *= gain;
    }

    pos.y += map(fbmValue,-1, 1,-20,20);

    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(pos, 1.0);
    outTextCoord = texCoord;
}