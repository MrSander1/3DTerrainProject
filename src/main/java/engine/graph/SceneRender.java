package engine.graph;

import engine.Window;
import engine.scene.*;
import engine.scene.lights.*;

import org.joml.*;

import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private ShaderProgram shaderProgram;
    private UniformMap uniformMap;

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("src/shaders/scene.vert", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("src/shaders/scene.frag", GL_FRAGMENT_SHADER));
        shaderProgram = new ShaderProgram(shaderModuleDataList);
        createUniforms();
    }

    public void cleanup() {
        shaderProgram.cleanup();
    }

    private void createUniforms() {
        uniformMap = new UniformMap(shaderProgram.getProgramId());
        uniformMap.createUniform("projectionMatrix");
        uniformMap.createUniform("viewMatrix");
        uniformMap.createUniform("modelMatrix");
        uniformMap.createUniform("txtSampler");
        uniformMap.createUniform("shouldMutate");
        uniformMap.createUniform("terrain.frequency");
        uniformMap.createUniform("terrain.amplitude");
        uniformMap.createUniform("terrain.gain");
        uniformMap.createUniform("terrain.lacunarity");
        uniformMap.createUniform("terrain.octaves");
        uniformMap.createUniform("terrain.max");
        uniformMap.createUniform("terrain.min");

        uniformMap.createUniform("material.ambient");
        uniformMap.createUniform("material.diffuse");
        uniformMap.createUniform("material.specular");
        uniformMap.createUniform("material.reflectance");
        uniformMap.createUniform("ambientLight.factor");
        uniformMap.createUniform("ambientLight.color");

        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            String name = "pointLights[" + i + "]";
            uniformMap.createUniform(name + ".position");
            uniformMap.createUniform(name + ".color");
            uniformMap.createUniform(name + ".intensity");
            uniformMap.createUniform(name + ".att.constant");
            uniformMap.createUniform(name + ".att.linear");
            uniformMap.createUniform(name + ".att.exponent");
        }
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            String name = "spotLights[" + i + "]";
            uniformMap.createUniform(name + ".pl.position");
            uniformMap.createUniform(name + ".pl.color");
            uniformMap.createUniform(name + ".pl.intensity");
            uniformMap.createUniform(name + ".pl.att.constant");
            uniformMap.createUniform(name + ".pl.att.linear");
            uniformMap.createUniform(name + ".pl.att.exponent");
            uniformMap.createUniform(name + ".conedir");
            uniformMap.createUniform(name + ".cutoff");
        }

        uniformMap.createUniform("dirLight.color");
        uniformMap.createUniform("dirLight.direction");
        uniformMap.createUniform("dirLight.intensity");
    }

    public void render(Scene scene) {
        int i = 0;
        shaderProgram.bind();

        uniformMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());

        uniformMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());

        uniformMap.setUniform("txtSampler", 0);

        updateTerrain(scene);
        updateLights(scene);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : models) {
            List<Entity> entities = model.getEntitesList();

            for (Material material : model.getMaterialList()) {
                uniformMap.setUniform("material.ambient", material.getAmbientColor());
                uniformMap.setUniform("material.diffuse", material.getDiffuseColor());
                uniformMap.setUniform("material.specular", material.getSpecularColor());
                uniformMap.setUniform("material.reflectance", material.getReflectance());
                Texture texture = textureCache.getTexture(material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();
                // change here to strip
                for (Mesh mesh : material.getMeshList()) {
                    glBindVertexArray(mesh.getVaoId());
                    for (Entity entity : entities) {
                        boolean mutate = (i==2);
                        uniformMap.setUniform("modelMatrix", entity.getModelMatrix());
                        uniformMap.setUniform("shouldMutate", mutate);
                        glDrawElements(GL_TRIANGLE_STRIP, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                        ++i;
                    }
                }
            }
        }

        glBindVertexArray(0);

        shaderProgram.unbind();

    }

    private void updateTerrain(Scene scene) {
        Terrain terrain = scene.getTerrain();

        uniformMap.setUniform("terrain.frequency", terrain.getFrequency());
        uniformMap.setUniform("terrain.amplitude", terrain.getAmplitude());
        uniformMap.setUniform("terrain.gain", terrain.getGain());
        uniformMap.setUniform("terrain.lacunarity", terrain.getLacunarity());
        uniformMap.setUniform("terrain.octaves", terrain.getOctaves());
        uniformMap.setUniform("terrain.max", terrain.getMax());
        uniformMap.setUniform("terrain.min", terrain.getMin());
    }

    private void updateLights(Scene scene) {
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();

        SceneLights sceneLights = scene.getSceneLights();
        AmbientLight ambientLight = sceneLights.getAmbientLight();
        uniformMap.setUniform("ambientLight.factor", ambientLight.getIntensity());
        uniformMap.setUniform("ambientLight.color", ambientLight.getColor());

        DirLight dirLight = sceneLights.getDirLight();
        Vector4f auxDir = new Vector4f(dirLight.getDirection(), 0);
        auxDir.mul(viewMatrix);
        Vector3f dir = new Vector3f(auxDir.x, auxDir.y, auxDir.z);
        uniformMap.setUniform("dirLight.color", dirLight.getColor());
        uniformMap.setUniform("dirLight.direction", dir);
        uniformMap.setUniform("dirLight.intensity", dirLight.getIntensity());

        List<PointLight> pointLights = sceneLights.getPointLights();
        int numPointLights = pointLights.size();
        PointLight pointLight;
        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            if (i < numPointLights) {
                pointLight = pointLights.get(i);
            } else {
                pointLight = null;
            }
            String name = "pointLights[" + i + "]";
            updatePointLight(pointLight, name, viewMatrix);
        }


        List<SpotLight> spotLights = sceneLights.getSpotLights();
        int numSpotLights = spotLights.size();
        SpotLight spotLight;
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            if (i < numSpotLights) {
                spotLight = spotLights.get(i);
            } else {
                spotLight = null;
            }
            String name = "spotLights[" + i + "]";
            updateSpotLight(spotLight, name, viewMatrix);
        }
    }

    private void updatePointLight(PointLight pointLight, String prefix, Matrix4f viewMatrix) {
        Vector4f aux = new Vector4f();
        Vector3f lightPosition = new Vector3f();
        Vector3f color = new Vector3f();
        float intensity = 0.0f;
        float constant = 0.0f;
        float linear = 0.0f;
        float exponent = 0.0f;
        if (pointLight != null) {
            aux.set(pointLight.getPosition(), 1);
            aux.mul(viewMatrix);
            lightPosition.set(aux.x, aux.y, aux.z);
            color.set(pointLight.getColor());
            intensity = pointLight.getIntensity();
            PointLight.Attenuation attenuation = pointLight.getAttenuation();
            constant = attenuation.getConstant();
            linear = attenuation.getLinear();
            exponent = attenuation.getExponent();
        }
        uniformMap.setUniform(prefix + ".position", lightPosition);
        uniformMap.setUniform(prefix + ".color", color);
        uniformMap.setUniform(prefix + ".intensity", intensity);
        uniformMap.setUniform(prefix + ".att.constant", constant);
        uniformMap.setUniform(prefix + ".att.linear", linear);
        uniformMap.setUniform(prefix + ".att.exponent", exponent);
    }

    private void updateSpotLight(SpotLight spotLight, String prefix, Matrix4f viewMatrix) {
        PointLight pointLight = null;
        Vector3f coneDirection = new Vector3f();
        float cutoff = 0.0f;
        if (spotLight != null) {
            coneDirection = spotLight.getConeDirection();
            cutoff = spotLight.getCutOff();
            pointLight = spotLight.getPointLight();
        }

        uniformMap.setUniform(prefix + ".conedir", coneDirection);
        uniformMap.setUniform(prefix + ".cutoff", cutoff);
        updatePointLight(pointLight, prefix + ".pl", viewMatrix);
    }

}

