package game;

import engine.scene.Terrain;
import imgui.*;
import imgui.flag.ImGuiCond;
import org.joml.*;
import engine.*;
import engine.scene.Scene;


public class TerrainControls implements IGuiInstance  {
    private float[] frequency;

    private float[] amplitude;

    private float[] gain;

    private float[] lacunarity;

    private float[] speed;

    private float[] scale;

    private float[] sensitivity;

    private float[] size;

    private int[] octaves;

    private int[] max;

    private int[] min;

    private int[] subdivisions;

    public boolean consumed;

    public boolean consumed1;

    public boolean consumed2;

    public TerrainControls(Scene scene) {
        Terrain terrain = scene.getTerrain();

        frequency = new float[]{terrain.getFrequency()};
        amplitude = new float[]{terrain.getAmplitude()};
        gain = new float[]{terrain.getGain()};
        lacunarity = new float[]{terrain.getLacunarity()};
        octaves = new int[]{terrain.getOctaves()};
        max = new int[]{terrain.getMax()};
        min = new int[]{terrain.getMin()};
        scale = new float[]{terrain.getScale()};
        speed = new float[]{terrain.getSpeed()};
        sensitivity = new float[]{terrain.getSensitivity()};
        size = new float[]{terrain.getSize()};
        subdivisions = new int[]{terrain.getSubdivisions()};
    }

    @Override
    public void drawGui() {

        ImGui.begin("Terrain controls");
        if (ImGui.collapsingHeader("Terrain")) {
            ImGui.sliderFloat("Scale", scale, 0.0f, 10.0f, "%.2f");
            ImGui.sliderFloat("Frequency", frequency, 0.0f, 1.0f, "%.2f");
            ImGui.sliderFloat("Gain", gain, 0.0f, 1.0f, "%.2f");
            ImGui.sliderFloat("Lacunarity", lacunarity, 0.0f, 4.0f, "%.2f");
            ImGui.sliderInt("Octaves", octaves, 0, 16, "%d");
            ImGui.sliderInt("Max", max,  0, 10, "%d");
            ImGui.sliderInt("Min", min,  0, 10, "%d");
            ImGui.sliderFloat("Size", size, 0.0f, 1000.0f, "%.2f");
            consumed1 = ImGui.isItemActive();
            ImGui.sliderInt("Subdivisions", subdivisions,  1, 1000, "%d");
            consumed2 = ImGui.isItemActive();
        }

        if (ImGui.collapsingHeader("Camera Controls")) {
            ImGui.sliderFloat("Speed", speed, 0.0001f, 0.5f, "%.4f");
            ImGui.sliderFloat("Sensitivity", sensitivity, 0.1f, 1f, "%.2f");

        }

        ImGui.end();

    }

    @Override
    public boolean handleGuiInput(Scene scene, Window window) {
        ImGuiIO imGuiIO = ImGui.getIO();
        MouseInput mouseInput = window.getMouseInput();
        Vector2f mousePos = mouseInput.getCurrentPos();
        imGuiIO.addMousePosEvent(mousePos.x, mousePos.y);
        imGuiIO.addMouseButtonEvent(0, mouseInput.isLeftButtonPressed());
        imGuiIO.addMouseButtonEvent(1, mouseInput.isRightButtonPressed());

        consumed = imGuiIO.getWantCaptureMouse() || imGuiIO.getWantCaptureKeyboard();
        if (consumed) {
            Terrain terrain = scene.getTerrain();
            terrain.setFrequency(frequency[0]);
            terrain.setScale(scale[0]);
            terrain.setAmplitude(amplitude[0]);
            terrain.setGain(gain[0]);
            terrain.setLacunarity(lacunarity[0]);
            terrain.setOctaves(octaves[0]);
            terrain.setMax(max[0]);
            terrain.setMin(min[0]);
            terrain.setSpeed(speed[0]);
            terrain.setSensitivity(sensitivity[0]);
            terrain.setSize(size[0]);
            terrain.setSubdivisions(subdivisions[0]);

        }
        return consumed;

    }
    @Override
    public boolean getConsumed() {
        return consumed1 || consumed2;
    }
}
