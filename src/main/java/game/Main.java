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

public class Main implements IAppLogic {

    private Entity planeEntity;
    private TerrainControls terrainControls;
    private Terrain terrain;
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

        int subdivisions = 1000;
        // figure out a way to change the size through imgui.
        float size = 20.0f;

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

        int[] indices = indicesGen(subdivisions);

        Texture texture = scene.getTextureCache().createTexture("tex/13105.jpg");
        Material material = new Material();
        material.setTexturePath(texture.getTexturePath());
        List<Material> materialList = new ArrayList<>();
        materialList.add(material);

        Mesh mesh = new Mesh(positions, textCoords, indices);
        material.getMeshList().add(mesh);
        Model planeModel = new Model("plane-model", materialList);
        scene.addModel(planeModel);

        planeEntity = new Entity("plane-entity", planeModel.getId());
        planeEntity.setPosition(0, -10, -10);
        scene.addEntity(planeEntity);

        terrain = new Terrain( 0.3f, 0.5f, 0.5f, 2.0f, 8, 10, 10, 1.0f, 0.05f, 0.1f);
        scene.setTerrain(terrain);
        terrainControls = new TerrainControls(scene);
        scene.setGuiInstance(terrainControls);
    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis,boolean inputConsumed) {
        if (inputConsumed) {
            return;
        }
        float move = diffTimeMillis * terrain.getSpeed();
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
            camera.addRotation((float) Math.toRadians(-displVec.x * terrain.getSensitivity()),
                    (float) Math.toRadians(-displVec.y * terrain.getSensitivity()));
        }
    }


    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        planeEntity.setScale(terrain.getScale());
        planeEntity.updateModelMatrix();
    }

    // Generation Data - should be a nested for loop

    /*
    Create a very dense plane, im talking a lot of subdivisions so I can have detail already imbeded
    Fbm algorithm should not be implemented in the vertex array but rather through the vertex shader
    So overall, figure out how to make an incredibly dense plane

    */

    // Good stackoverflow article about a simlar soultion though difference are there evidently: https://stackoverflow.com/questions/78063818/how-to-calculate-correct-normals-on-a-deformed-plane-with-fractal-brownian-motio#:~:text=Your%20program%20basically%20tries%20to,vertices%20per%20period%2C%20possibly%20higher.
    public int vertexCount(int subdivisions) {
        return ((subdivisions) * (subdivisions)) * 3;

    }

    public float[] positionsGen(float size, int subdivisions) {

        float tilewidth = size / (float)(subdivisions - 1);


        int vertexCount = vertexCount(subdivisions);


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
        int count = subdivisions * subdivisions;
        float[] tex = new float[count * 2];



        int i = 0;
        for (int z = 0; z < subdivisions; z++) {
            for (int x = 0; x < subdivisions; x++) {
                float u = (float) (x*size) / (subdivisions - 1);
                float v = (float) (z*size) / (subdivisions - 1);

                tex[i++] = u;
                tex[i++] = v;
            }
        }
        return tex;
    }

    // Indices Generation
    public int[] indicesGen(int subdivisions) {

        int inxSize = (subdivisions - 1) * (subdivisions * 2 + 2);
        int[] inx = new int[inxSize];

        int i = 0;
        for (int r = 0; r < subdivisions - 1; ++r) {
            inx[i++] = r * subdivisions;
            for (int c = 0; c < subdivisions; ++c) {
                inx[i++] = r * subdivisions + c;
                inx[i++] = (r + 1) * subdivisions + c;
            }
            inx[i++] = (r + 1) * subdivisions + (subdivisions - 1);
        }

       return inx;
    }
}

