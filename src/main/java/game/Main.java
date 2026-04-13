package game;

import engine.scene.lights.*;
import org.joml.*;
import engine.*;
import engine.graph.*;
import engine.scene.*;

import java.lang.Math;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {

    private Entity planeEntity;
    private Entity cube1Entity;
    private Entity cube2Entity;
    private Mesh planeMesh;
    private Mesh cubeMesh1;
    private Mesh cubeMesh2;
    private List<Material> planeMaterialList;
    private List<Material> cube1MaterialList;
    private List<Material> cube2MaterialList;
    private Texture planeTexture;
    private Texture cubeTexture;
    private Terrain terrain;
    private SceneLights sceneLights;
    private GuiManager guiManager;


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
        guiManager = new GuiManager();

        terrain = new Terrain( 0.3f, 0.5f, 0.5f, 2.0f, 8, 10, 10, 1.0f, 0.05f, 0.1f, 1000, 20.0f);
        scene.setTerrain(terrain);

        // CubE
        float[] cubePositions = new float[] {
                // Front face
                -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,
                // Back face
                -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f, -0.5f, -0.5f,
                // Top face
                -0.5f,  0.5f, -0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,
                // Bottom face
                -0.5f, -0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f, -0.5f, -0.5f,  0.5f,
                // Right face
                0.5f, -0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f,  0.5f,  0.5f,  0.5f, -0.5f,  0.5f,
                // Left face
                -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,  0.5f, -0.5f,  0.5f,  0.5f, -0.5f,  0.5f, -0.5f
        };

        float[] cubeTextCoords = new float[] {
                // Front
                0.0f, 0.0f,  1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f,
                // Back
                1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f,  0.0f, 0.0f,
                // Top
                0.0f, 1.0f,  0.0f, 0.0f,  1.0f, 0.0f,  1.0f, 1.0f,
                // Bottom
                1.0f, 1.0f,  0.0f, 1.0f,  0.0f, 0.0f,  1.0f, 0.0f,
                // Right
                1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f,  0.0f, 0.0f,
                // Left
                0.0f, 0.0f,  1.0f, 0.0f,  1.0f, 1.0f,  0.0f, 1.0f
        };

        int[] cubeIndices = new int[] {
                0,  1,  2,      0,  2,  3,    // Front
                4,  5,  6,      4,  6,  7,    // Back
                8,  9,  10,     8,  10, 11,   // Top
                12, 13, 14,     12, 14, 15,   // Bottom
                16, 17, 18,     16, 18, 19,   // Right
                20, 21, 22,     20, 22, 23    // Left
        };

        // Plane
        float[] planePositions = positionsGen(terrain.getSize(), terrain.getSubdivisions());

        float[] planeTextCoords = textureGen(terrain.getSize(), terrain.getSubdivisions());

        int[] planeIndices = indicesGen(terrain.getSubdivisions());

        planeTexture = scene.getTextureCache().createTexture("tex/13105.jpg");
        cubeTexture = scene.getTextureCache().createTexture("tex/white.jpg");
        Material planeMaterial = new Material();
        Material cube1Material = new Material();
        Material cube2Material = new Material();
        planeMaterial.setTexturePath(planeTexture.getTexturePath());
        cube1Material.setTexturePath(cubeTexture.getTexturePath());
        cube2Material.setTexturePath(cubeTexture.getTexturePath());
        planeMaterialList = new ArrayList<>();
        planeMaterialList.add(planeMaterial);
        cube1MaterialList = new ArrayList<>();
        cube1MaterialList.add(cube1Material);
        cube2MaterialList = new ArrayList<>();
        cube2MaterialList.add(cube1Material);

        planeMesh = new Mesh(planePositions, planeTextCoords, planeIndices);
        cubeMesh1 = new Mesh(cubePositions, cubeTextCoords, cubeIndices);
        cubeMesh2 = new Mesh(cubePositions, cubeTextCoords, cubeIndices);
        planeMaterial.getMeshList().add(planeMesh);
        cube1Material.getMeshList().add(cubeMesh1);
        cube2Material.getMeshList().add(cubeMesh2);
        Model planeModel = new Model("plane-model", planeMaterialList);
        Model cubeModel1 = new Model("cube1-model", cube1MaterialList);
        Model cubeModel2 = new Model("cube2-model", cube2MaterialList);
        scene.addModel(planeModel);
        scene.addModel(cubeModel1);
        scene.addModel(cubeModel2);

        planeEntity = new Entity("plane-entity", planeModel.getId());
        planeEntity.setPosition(0, 0, 0);
        planeEntity.updateModelMatrix();
        scene.addEntity(planeEntity);

        scene.getCamera().setPosition(5,10,15);

        sceneLights = new SceneLights();
        sceneLights.getAmbientLight().setIntensity(0.3f);
        sceneLights.getDirLight().setPosition(1.0f, 1.0f, 1.0f);
        scene.setSceneLights(sceneLights);
        sceneLights.getPointLights().add(new PointLight(new Vector3f(1, 1, 1),
                new Vector3f(15, 10, 5), 1.0f));

        cube1Entity = new Entity("cube1-entity", cubeModel1.getId());
        cube1Entity.setPosition(sceneLights.getPointLights().get(0).getPosition().x,sceneLights.getPointLights().get(0).getPosition().y,sceneLights.getPointLights().get(0).getPosition().z);
        cube1Entity.setScale(0.5f);
        cube1Entity.updateModelMatrix();
        scene.addEntity(cube1Entity);


        Vector3f coneDir = new Vector3f(0, 0, -1);
        sceneLights.getSpotLights().add(new SpotLight(new PointLight(new Vector3f(1, 1, 1),
                new Vector3f(15, 10, 10), 1.0f), coneDir, 140.0f));

        cube2Entity = new Entity("cube2-entity", cubeModel1.getId());
        cube2Entity.setPosition(sceneLights.getSpotLights().get(0).getPointLight().getPosition().x,sceneLights.getSpotLights().get(0).getPointLight().getPosition().y,sceneLights.getSpotLights().get(0).getPointLight().getPosition().z);
        cube2Entity.setScale(0.5f);
        cube2Entity.updateModelMatrix();
        scene.addEntity(cube2Entity);

        guiManager.addGui(new LightControls(scene));
        guiManager.addGui(new TerrainControls(scene));

        scene.setGuiInstance(guiManager);
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
        cube1Entity.setPosition(sceneLights.getPointLights().get(0).getPosition().x,sceneLights.getPointLights().get(0).getPosition().y,sceneLights.getPointLights().get(0).getPosition().z);
        cube1Entity.updateModelMatrix();
        cube2Entity.setPosition(sceneLights.getSpotLights().get(0).getPointLight().getPosition().x,sceneLights.getSpotLights().get(0).getPointLight().getPosition().y,sceneLights.getSpotLights().get(0).getPointLight().getPosition().z);
        cube2Entity.updateModelMatrix();

        IGuiInstance gui = scene.getGuiInstance();

        if (gui != null && gui.getConsumed()) {
            planeMesh.update(positionsGen(terrain.getSize(), terrain.getSubdivisions()), textureGen(terrain.getSize(), terrain.getSubdivisions()), indicesGen(terrain.getSubdivisions()));
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
                float u = (x*size) / (subdivisions - 1);
                float v = (z*size) / (subdivisions - 1);

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

