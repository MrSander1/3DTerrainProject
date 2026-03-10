package engine.graph;

import engine.scene.Entity;

import java.util.*;

public class Model {

    private final String id;
    private List<Entity> entitiesList;
    private List<Material> materialList;

    public Model(String id, List<Material> materialList) {
        this.id = id;
        this.materialList = materialList;
        entitiesList = new ArrayList<>();
    }

    public void cleanup() {
        materialList.forEach(Material::cleanup);
    }

    public List<Entity> getEntitesList() {
        return entitiesList;
    }

    public String getId() {
        return id;
    }

    public List<Material> getMaterialList() {
        return materialList;
    }
}
