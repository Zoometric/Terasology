package org.terasology.entitySystem.pojo;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.pojo.persistence.EntityPersister;
import org.terasology.entitySystem.pojo.persistence.EntityPersisterImpl;
import org.terasology.entitySystem.pojo.persistence.FieldInfo;
import org.terasology.entitySystem.pojo.persistence.SerializationInfo;
import org.terasology.entitySystem.pojo.persistence.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Vector3f;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntitySerializationTest {

    PojoEntityManager entityManager;
    EntityPersister entityPersister;
    PrefabManager prefabManager;

    @Before
    public void setup() {
        entityManager = new PojoEntityManager();
        entityManager.registerTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        entityManager.registerComponentClass(IntegerComponent.class);
        entityManager.registerComponentClass(StringComponent.class);
        prefabManager = new PojoPrefabManager();
        entityManager.setPrefabManager(prefabManager);
        entityPersister = entityManager.getEntityPersister();
    }

    @Test
    public void testGetterSetterUtilization() throws Exception {
        SerializationInfo info = new SerializationInfo(GetterSetterComponent.class);
        info.addField(new FieldInfo(GetterSetterComponent.class.getDeclaredField("value"), GetterSetterComponent.class, new Vector3fTypeHandler()));

        GetterSetterComponent comp = new GetterSetterComponent();
        GetterSetterComponent newComp = (GetterSetterComponent) info.deserialize(info.serialize(comp));
        assertTrue(comp.getterUsed);
        assertTrue(newComp.setterUsed);
    }

    @Test
    public void testDeltaNoUnchangedComponents() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);

        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity);

        assertEquals(1, entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(0, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
    }

    @Test
    public void testDeltaAddNewComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);
        entity.addComponent(new IntegerComponent(1));

        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity);

        assertEquals(1, entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(1, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
        EntityData.Component componentData = entityData.getComponent(0);
        assertEquals("Integer", componentData.getType());
        assertEquals(1, componentData.getFieldCount());
        EntityData.NameValue field = componentData.getField(0);
        assertEquals("value", field.getName());
        assertEquals(1, field.getValue().getInteger(0));
    }

    @Test
    public void testDeltaRemoveComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);
        entity.removeComponent(StringComponent.class);

        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity);

        assertEquals(1, entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(0, entityData.getComponentCount());
        assertEquals(Lists.newArrayList("String"), entityData.getRemovedComponentList());
    }

    @Test
    public void testDeltaChangedComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);
        entity.getComponent(StringComponent.class).value = "Delta";
        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity);

        assertEquals(1, entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(1, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
        EntityData.Component componentData = entityData.getComponent(0);
        assertEquals("String", componentData.getType());
        assertEquals(Lists.newArrayList(EntityData.NameValue.newBuilder().setName("value").setValue(EntityData.Value.newBuilder().addString("Delta").build()).build()), componentData.getFieldList());
    }

    @Test
    public void testDeltaLoadNoChange() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create("Test");
        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity);
        EntityRef loadedEntity = entityPersister.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Value", loadedEntity.getComponent(StringComponent.class).value);
    }

    @Test
    public void testDeltaLoadAddedComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create("Test");
        entity.addComponent(new IntegerComponent(2));
        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity);
        EntityRef loadedEntity = entityPersister.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Value", loadedEntity.getComponent(StringComponent.class).value);
        assertTrue(loadedEntity.hasComponent(IntegerComponent.class));
        assertEquals(2, loadedEntity.getComponent(IntegerComponent.class).value);
    }

    @Test
    public void testDeltaLoadRemovedComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create("Test");
        entity.removeComponent(StringComponent.class);
        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity);
        EntityRef loadedEntity = entityPersister.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertFalse(loadedEntity.hasComponent(StringComponent.class));
    }

    @Test
    public void testDeltaLoadChangedComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create("Test");
        entity.getComponent(StringComponent.class).value = "Delta";
        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity);
        EntityRef loadedEntity = entityPersister.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Delta", loadedEntity.getComponent(StringComponent.class).value);
    }

}
