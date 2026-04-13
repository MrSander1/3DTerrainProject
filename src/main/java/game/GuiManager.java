package game;

import engine.IGuiInstance;
import engine.Window;
import engine.scene.Scene;
import imgui.ImGui;
import imgui.flag.ImGuiCond;

import java.util.ArrayList;
import java.util.List;

public class GuiManager implements IGuiInstance {

    private final List<IGuiInstance> subGuiInstances;

    public GuiManager() {
        this.subGuiInstances = new ArrayList<>();
    }

    public void addGui(IGuiInstance gui) {
        subGuiInstances.add(gui);
    }

    @Override
    public void drawGui() {

        ImGui.newFrame();
        int i = 0;
        for (IGuiInstance gui : subGuiInstances) {
            i++;
            ImGui.setNextWindowPos((i * 500), 0, ImGuiCond.Once);
            ImGui.setNextWindowSize(450, 400, ImGuiCond.Once);
            gui.drawGui();
        }

        ImGui.endFrame();
        ImGui.render();
    }

    @Override
    public boolean handleGuiInput(Scene scene, Window window) {
        boolean consumed = false;
        for (IGuiInstance gui : subGuiInstances) {

            if (gui.handleGuiInput(scene, window)) {
                consumed = true;
            }
        }
        return consumed;
    }

    public boolean getConsumed() {
        for (IGuiInstance gui : subGuiInstances) {
            if (gui.getConsumed()) {
                return true;
            }
        }
        return false;
    }
}