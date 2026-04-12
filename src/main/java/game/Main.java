package game;

import imgui.*;
import org.joml.*;
import engine.*;
import engine.graph.*;
import engine.scene.*;

import java.lang.Math;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private Entity planeEntity;
    private Mesh mesh;
    private List<Material> materialList;
    private Texture texture;
    private Model planeModel;
    private TerrainControls terrainControls;
    private Terrain terrain;


    public static void main(String[] args) {
        Main main = new Main();
        Engine gameEng = new Engine("3DTerrain", new Window.WindowOptions(), main);
        gameEng.start();
    }

    @Override
    public void cleanup() {
        // Nothing to be done yet
    }

    @Override
    public void init(Window window, Scene scene, Render render) {

        terrain = new Terrain( 0.3f, 0.5f, 0.5f, 2.0f, 8, 10, 10, 1.0f, 0.05f, 0.1f, 1000, 20.0f);
        scene.setTerrain(terrain);
        terrainControls = new TerrainControls(scene);


        float[] positions = positionsGen(terrain.getSize(), terrain.getSubdivisions());

        float[] textCoords = textureGen(terrain.getSize(), terrain.getSubdivisions());

        int[] indices = indicesGen(terrain.getSubdivisions());

        texture = scene.getTextureCache().createTexture("tex/13105.jpg");
        Material material = new Material();
        material.setTexturePath(texture.getTexturePath());
        materialList = new ArrayList<>();
        materialList.add(material);

        mesh = new Mesh(positions, textCoords, indices);
        material.getMeshList().add(mesh);
        Model planeModel = new Model("plane-model", materialList);
        scene.addModel(planeModel);

        planeEntity = new Entity("plane-entity", planeModel.getId());
        planeEntity.setPosition(0, -10, -10);
        scene.addEntity(planeEntity);

;
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

        if (terrainControls.getConsumed()) {
            mesh.update(positionsGen(terrain.getSize(), terrain.getSubdivisions()), textureGen(terrain.getSize(), terrain.getSubdivisions()), indicesGen(terrain.getSubdivisions()));
        }
    }

    // Generation Data - should be a nested for loop

    /*
    Create a very dense plane, im talking a lot of subdivisions so I can have detail already imbeded
    Fbm algorithm should not be implemented in the vertex array buvertex shader
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

