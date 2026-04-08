#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

struct Terrain
{
    float frequency;
    float amplitude;
    float gain;
    float lacunarity;
    int octaves;
    int max;
    int min;
};

out vec2 outTextCoord;

uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
// You need uniforms
uniform mat4 modelMatrix;
uniform Terrain terrain;

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
    // could cause an issue
    float frequency = terrain.frequency;
    float amplitude = terrain.amplitude;
    float gain = terrain.gain;
    float lacunarity = terrain.lacunarity;
    int octaves = terrain.octaves;
    int max = terrain.max;
    int min = terrain.min;

    float fbmValue = 0.0f;
    float normalization = 0.0f;

    for (int i = 0; i < octaves; ++i){
        fbmValue += (amplitude * noise(vec2(pos.x * frequency, pos.z * frequency)));
        normalization += amplitude;
        frequency *= lacunarity;
        amplitude *= gain;
    }

    fbmValue /= normalization;
    pos.y += map(fbmValue,-1, 1,-min,max);

    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(pos, 1.0);
    outTextCoord = texCoord;
}