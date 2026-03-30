package game;

import com.raylabz.opensimplex.OpenSimplexNoise;
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
    private Entity planeEntity;
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
    /* Skewed plane bug was not a bug
       Missing triangle is probably is a textCoord issue
    */
    @Override
    public void init(Window window, Scene scene, Render render) {

        int subdivisions = 100;
        float size = 10.0f;

        float[] positions = positionsGen(size, subdivisions);
        // Figure out what text coords even needs
        /* TextCoords go from 0->1
        * all we need is every corner of every quad thus every value
        * though it needs proper mapping so that is annoying part
        * Overall, for now focus on position design as the order of
        * vertices are what matter for these coords.*/
        float[] textCoords = textureGen(size, subdivisions);

        /* (NOTE ON FACE CULLING): Indices are where the order is set, so it's very
        important to have it coded into the for loop when I eventually create the actual
        generator, though further research is required with how it
        fits into with triangle strips. Alternatively, I can try to
        do it with triangles only. Though either way I am going to have
        to apply the strip formula.
        */

        int[] indices = indicesGen(size, subdivisions);

        Texture texture = scene.getTextureCache().createTexture("src/shaders/default_texture.png");
        Material material = new Material();
        material.setTexturePath(texture.getTexturePath());
        List<Material> materialList = new ArrayList<>();
        materialList.add(material);

        Mesh mesh = new Mesh(positions, textCoords, indices);
        material.getMeshList().add(mesh);
        Model planeModel = new Model("plane-model", materialList);
        scene.addModel(planeModel);

        planeEntity = new Entity("plane-entity", planeModel.getId());
        planeEntity.setPosition(0, -2, -10);
        scene.addEntity(planeEntity);

        scene.setGuiInstance(this);
    }

    static public final float map(float value, float istart, float istop, float ostart, float ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
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
        planeEntity.setRotation(1, 1, 1, (float) Math.toRadians(rotation));
        planeEntity.updateModelMatrix();
    }

    // Generation Data - should be a nested for loop

    /*
    Create a very dense plane, im talking a lot of subdivisions so I can have detail already imbeded
    Fbm algorithm should not be implemented in the vertex array but rather through the vertex shader
    So overall, figure out how to make an incredibly dense plane

    */

    // Good stackoverflow article about a simlar soultion though difference are there evidently: https://stackoverflow.com/questions/78063818/how-to-calculate-correct-normals-on-a-deformed-plane-with-fractal-brownian-motio#:~:text=Your%20program%20basically%20tries%20to,vertices%20per%20period%2C%20possibly%20higher.
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

    public int vertexCount(float size, int subdivisions) {
        return ((subdivisions) * (subdivisions)) * 3;

    }

    public float[] positionsGen(float size, int subdivisions) {

        float tilewidth = size / (float)subdivisions;

        OpenSimplexNoise noise = new OpenSimplexNoise(12345L);

        int vertexCount = vertexCount(size, subdivisions);


        float[] pos = new float[vertexCount];

        int i = 0;
        for (int z = 0; z < subdivisions; z++) {
            for (int x = 0; x < subdivisions; x++) {
                pos[i++] = (float) x * tilewidth;
                pos[i++] = 0.0f; // dont forget that y is z in GL coords
                pos[i++] = (float) z * tilewidth;
            }
        }
        return pos;

    }

    // Texture Generation - I think have to exclude degenerate triangles map(fbmValues(0.01f, 0.1f, 0.5f, 2.0f, 16, subdivisions, x, z), -1, 1,-10,10);;

    public float[] textureGen(float size, int subdivisions) {

        int count = vertexCount(size, subdivisions) / 3;

        float[] tex = new float[count * 8];


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

    // Indices Generation - This 100% is what is bugging out
    public int[] indicesGen(float size, int subdivisions) {

        int a = 0;


        int inxSize = (subdivisions - 1) * (subdivisions * 2);
        int[] inx = new int[inxSize];

        for(int i = 0; i < subdivisions-1; i++)       // for each row a.k.a. each strip
        {
            for(int j = 0; j < subdivisions; j++)      // for each column
            {
                for(int k = 0; k < 2; k++)      // for each side of the strip
                {
                    inx[a] = (j + subdivisions * (i + k));
                    ++a;
                }
            }
        }



       return inx;
    }
}

