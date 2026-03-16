package game;

import imgui.*;
import imgui.flag.ImGuiCond;
import org.joml.*;
import engine.*;
import engine.graph.*;
import engine.scene.*;

import java.lang.Math;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic, IGuiInstance {

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;
    private Entity cubeEntity;
    private float rotation;


    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEng = new Engine("3DTerrain", new Window.WindowOptions(), main);
        gameEng.start();
    }

    @Override
    public void cleanup() {
        // Nothing to be done yet
    }
    /* Bug in Generation methods that created a skewed plane look into that
       also missing one quad.
    */
    @Override
    public void init(Window window, Scene scene, Render render) {

        int width = 50;
        int height = 50;

        float[] positions = positionsGen(width, height);
        // Figure out what text coords even needs
        /* TextCoords go from 0->1
        * all we need is every corner of every quad thus every value
        * though it needs proper mapping so that is annoying part
        * Overall, for now focus on position design as the order of
        * vertices are what matter for these coords.*/
        float[] textCoords = textureGen(width, height);

        /* (NOTE ON FACE CULLING): Indices are where the order is set, so it's very
        important to have it coded into the for loop when I eventually create the actual
        generator, though further research is required with how it
        fits into with triangle strips. Alternatively, I can try to
        do it with triangles only. Though either way I am going to have
        to apply the strip formula.
        */

        int[] indices = indicesGen(width, height);

        Texture texture = scene.getTextureCache().createTexture("src/shaders/default_texture.png");
        Material material = new Material();
        material.setTexturePath(texture.getTexturePath());
        List<Material> materialList = new ArrayList<>();
        materialList.add(material);

        Mesh mesh = new Mesh(positions, textCoords, indices);
        material.getMeshList().add(mesh);
        Model cubeModel = new Model("cube-model", materialList);
        scene.addModel(cubeModel);

        cubeEntity = new Entity("cube-entity", cubeModel.getId());
        cubeEntity.setPosition(0, 0, -2);
        scene.addEntity(cubeEntity);

        scene.setGuiInstance(this);
    }

    @Override
    public void drawGui() {
        ImGui.newFrame( );
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.showDemoWindow();
        ImGui.endFrame();
        ImGui.render();
    }

    @Override
    public boolean handleGuiInput(Scene scene, Window window) {
        ImGuiIO imGuiIO = ImGui.getIO();
        MouseInput mouseInput = window.getMouseInput();
        Vector2f mousePos = mouseInput.getCurrentPos();
        imGuiIO.addMousePosEvent(mousePos.x, mousePos.y);
        imGuiIO.addMouseButtonEvent(0, mouseInput.isLeftButtonPressed());
        imGuiIO.addMouseButtonEvent(1, mouseInput.isRightButtonPressed());

        return imGuiIO.getWantCaptureMouse() || imGuiIO.getWantCaptureKeyboard();
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis,boolean inputConsumed) {
        if (inputConsumed) {
            return;
        }
        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }
        if (window.isKeyPressed(GLFW_KEY_UP)) {
            camera.moveUp(move);
        } else if (window.isKeyPressed(GLFW_KEY_DOWN)) {
            camera.moveDown(move);
        }

        MouseInput mouseInput = window.getMouseInput();
        if (mouseInput.isRightButtonPressed()) {
            Vector2f displVec = mouseInput.getDisplVec();
            camera.addRotation((float) Math.toRadians(-displVec.x * MOUSE_SENSITIVITY),
                    (float) Math.toRadians(-displVec.y * MOUSE_SENSITIVITY));
        }
    }


    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        rotation += 0.1f;
        if (rotation > 360) {
            rotation = 0;
        }
        cubeEntity.setRotation(1, 1, 1, (float) Math.toRadians(rotation));
        cubeEntity.updateModelMatrix();
    }

    // Generation Data - should be a nested for loop
    public float[] positionsGen(int width, int height) {
        int vertexCount = width * height * 3;

        float[] pos = new float[vertexCount];
        int i = 0;

        for (int row = 0; row < height; row++) {
            for (int col=0; col<width; col++) {
                pos[i++] = (float) col;
                pos[i++] = 0.0f; // dont forget that y is z in GL coords
                pos[i++] = (float) row;
            }
        }

        return pos;

    }

    // Texture Generation
    public float[] textureGen(int width, int height) {
        float[] tex = new float[width * height * 8];

        int i = 0;
        for (int j = 0; j < tex.length / 8; ++j) {
            // Top Right (0,0)
            tex[i++] = 0;
            tex[i++] = 0;
            // Bottom Right (0,1)
            tex[i++] = 0;
            tex[i++] = 1;
            // Top Left (1,0)
            tex[i++] = 1;
            tex[i++] = 0;
            // Bottom left (1,1)
            tex[i++] = 1;
            tex[i++] = 1;
        }

        return tex;
    }

    // Indices Generation
    public int[] indicesGen(int width, int height) {
        width++;

        int i = 0;

        int numberInxRow = width * 2 + 2;
        int numberInxDegens = (height - 1) * 2;
        int inxSize = (numberInxRow)*height+(numberInxDegens);
        int[] inx = new int[inxSize];

        for(int col = 0; col < height; ++col) {
            int base = col * width;
            for (int row = 0; row < width; ++row) {
                inx[i++] = base + row;
                inx[i++] = base + width+ row;
            }
            // add a degen triangle (expect for last row)
            if(col < height - 1) {
                inx[i++] = (col + 1) * width + (width - 1);
                inx[i++] = (col + 1) * width + (width - 1);
            }
        }

        return inx;
    }
}

