package engine.graph;

import engine.Window;
import engine.scene.*;

import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private ShaderProgram shaderProgram;
    private UniformMap uniformMap;

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
        uniformMap.createUniform("terrain.frequency");
        uniformMap.createUniform("terrain.amplitude");
        uniformMap.createUniform("terrain.gain");
        uniformMap.createUniform("terrain.lacunarity");
        uniformMap.createUniform("terrain.octaves");
        uniformMap.createUniform("terrain.max");
        uniformMap.createUniform("terrain.min");
    }

    public void render(Scene scene) {
        shaderProgram.bind();

        uniformMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());

        uniformMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());

        uniformMap.setUniform("txtSampler", 0);

        updateTerrain(scene);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : models) {
            List<Entity> entities = model.getEntitesList();

            for (Material material : model.getMaterialList()) {
                Texture texture = textureCache.getTexture(material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();
                // change here to strip
                for (Mesh mesh : material.getMeshList()) {
                    glBindVertexArray(mesh.getVaoId());
                    for (Entity entity : entities) {
                        uniformMap.setUniform("modelMatrix", entity.getModelMatrix());
                        glDrawElements(GL_TRIANGLE_STRIP, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                        //glDrawElements(GL_TRIANGLE_STRIP, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
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

}
