package com.nowhere.hysouls.appearance;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSkinComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Method;

/**
 * Changes player models by replacing the ModelComponent.
 * Internal classes (ModelAsset, Model, ModelComponent, CosmeticsModule) are accessed
 * via reflection since they aren't exposed in the plugin API.
 */
@SuppressWarnings("unchecked")
public final class PlayerModelChanger {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String HOLLOW_MODEL = "Player_Hollow";

    private static Class<?> modelAssetClass;
    private static Class<?> modelClass;
    private static Class<?> modelComponentClass;
    private static Class<?> cosmeticsModuleClass;
    private static Method getAssetMapMethod;
    private static Method getAssetMethod;
    private static Method createScaledModelMethod;
    private static Method getComponentTypeMethod;
    private static Method getCosmeticsModuleMethod;
    private static Method createModelMethod;
    private static boolean reflectionInitialized = false;

    private PlayerModelChanger() {}

    private static boolean initializeReflection() {
        if (reflectionInitialized) {
            return true;
        }

        try {
            modelAssetClass = Class.forName("com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset");
            modelClass = Class.forName("com.hypixel.hytale.server.core.asset.type.model.config.Model");
            modelComponentClass = Class.forName("com.hypixel.hytale.server.core.modules.entity.component.ModelComponent");
            cosmeticsModuleClass = Class.forName("com.hypixel.hytale.server.core.cosmetics.CosmeticsModule");

            getAssetMapMethod = modelAssetClass.getMethod("getAssetMap");
            Class<?> assetMapClass = getAssetMapMethod.getReturnType();
            getAssetMethod = assetMapClass.getMethod("getAsset", Object.class);
            createScaledModelMethod = modelClass.getMethod("createScaledModel", modelAssetClass, float.class);
            getComponentTypeMethod = modelComponentClass.getMethod("getComponentType");
            getCosmeticsModuleMethod = cosmeticsModuleClass.getMethod("get");
            createModelMethod = cosmeticsModuleClass.getMethod("createModel", com.hypixel.hytale.protocol.PlayerSkin.class);

            reflectionInitialized = true;
            LOGGER.atInfo().log("Model changing reflection initialized");
            return true;
        } catch (Exception e) {
            LOGGER.atSevere().log("Failed to initialize model changing reflection: %s", e.getMessage());
            return false;
        }
    }

    public static boolean applyHollowModel(Store<EntityStore> store, Ref<EntityStore> playerRef) {
        if (!initializeReflection()) {
            return false;
        }

        try {
            Object assetMap = getAssetMapMethod.invoke(null);
            Object modelAsset = getAssetMethod.invoke(assetMap, HOLLOW_MODEL);
            if (modelAsset == null) {
                LOGGER.atWarning().log("Hollow model asset '%s' not found", HOLLOW_MODEL);
                return false;
            }

            Object hollowModel = createScaledModelMethod.invoke(null, modelAsset, 1.0f);
            ComponentType modelComponentType = (ComponentType) getComponentTypeMethod.invoke(null);
            Component modelComponent = (Component) modelComponentClass.getConstructor(modelClass).newInstance(hollowModel);
            store.putComponent(playerRef, modelComponentType, modelComponent);

            LOGGER.atInfo().log("Applied hollow model");
            return true;
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to apply hollow model: %s", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean restoreOriginalModel(Store<EntityStore> store, Ref<EntityStore> playerRef) {
        if (!initializeReflection()) {
            return false;
        }

        try {
            PlayerSkinComponent skinComponent = store.getComponent(playerRef, PlayerSkinComponent.getComponentType());
            if (skinComponent == null) {
                LOGGER.atWarning().log("PlayerSkinComponent not found");
                return false;
            }

            Object cosmeticsModule = getCosmeticsModuleMethod.invoke(null);
            Object originalModel = createModelMethod.invoke(cosmeticsModule, skinComponent.getPlayerSkin());
            ComponentType modelComponentType = (ComponentType) getComponentTypeMethod.invoke(null);
            Component modelComponent = (Component) modelComponentClass.getConstructor(modelClass).newInstance(originalModel);
            store.putComponent(playerRef, modelComponentType, modelComponent);
            skinComponent.setNetworkOutdated();

            LOGGER.atInfo().log("Restored original model");
            return true;
        } catch (Exception e) {
            LOGGER.atWarning().log("Failed to restore original model: %s", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
