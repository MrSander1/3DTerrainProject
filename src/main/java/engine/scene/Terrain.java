package engine.scene;

//float frequency = 0.3f;
//float amplitude = 1.0f;
//float gain = 0.5f;
//float lacunarity = 2.0f;
//int octaves = 8;
//int max = 10;
//int min = -10;

public class Terrain {

    private float frequency;

    private float amplitude;

    private float gain;

    private float lacunarity;

    private float scale;

    private int octaves;

    private int max;

    private int min;

    public Terrain(float frequency, float amplitude, float gain, float lacunarity, int octaves, int max, int min, float scale) {
        this.frequency = frequency;
        this.amplitude = amplitude;
        this.gain = gain;
        this.lacunarity = lacunarity;
        this.scale = scale;
        this.octaves = octaves;
        this.max = max;
        this.min = min;
    }


    // Getters
    public float getFrequency() {
        return frequency;
    }

    public float getAmplitude() {
        return amplitude;
    }

    public float getGain() {
        return gain;
    }

    public float getLacunarity() {
        return lacunarity;
    }

    public float getScale() {
        return scale;
    }

    public int getOctaves() {
        return octaves;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    //Setters
    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public void setLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setOctaves(int octaves) {
        this.octaves = octaves;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setMin(int min) {
        this.min = min;
    }
}
