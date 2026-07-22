package github.trollhack.utils.render.shader.satin.impl;

import github.trollhack.utils.render.WindowResizeCallback;
import github.trollhack.utils.render.shader.satin.api.managed.ManagedCoreShader;
import github.trollhack.utils.render.shader.satin.api.managed.ManagedShaderEffect;
import github.trollhack.utils.render.shader.satin.api.managed.ShaderEffectManager;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.function.Consumer;

public final class ReloadableShaderEffectManager implements ShaderEffectManager {
    public static final ReloadableShaderEffectManager INSTANCE = new ReloadableShaderEffectManager();

    private final Set<ResettableManagedShaderBase<?>> managedShaders = new ReferenceOpenHashSet<>();

    public ReloadableShaderEffectManager() {
        WindowResizeCallback.EVENT.register((client, window) -> {
            onResolutionChanged(window.getFramebufferWidth(), window.getFramebufferHeight());
        });
    }

    @Override
    public ManagedShaderEffect manage(Identifier location) {
        return manage(location, s -> {
        });
    }

    @Override
    public ManagedShaderEffect manage(Identifier location, Consumer<ManagedShaderEffect> initCallback) {
        ResettableManagedShaderEffect ret = new ResettableManagedShaderEffect(location, initCallback);
        managedShaders.add(ret);
        return ret;
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location) {
        return manageCoreShader(location, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location, VertexFormat vertexFormat) {
        return manageCoreShader(location, vertexFormat, (s) -> {
        });
    }

    @Override
    public ManagedCoreShader manageCoreShader(Identifier location, VertexFormat vertexFormat, Consumer<ManagedCoreShader> initCallback) {
        ResettableManagedCoreShader ret = new ResettableManagedCoreShader(location, vertexFormat, initCallback);
        managedShaders.add(ret);
        return ret;
    }

    public void reload(ResourceFactory shaderResources) {
        for (ResettableManagedShaderBase<?> ss : managedShaders) {
            ss.initializeOrLog(shaderResources);
        }
    }

    public void onResolutionChanged(int newWidth, int newHeight) {
        if (!managedShaders.isEmpty()) {
            for (ResettableManagedShaderBase<?> ss : managedShaders) {
                if (ss.isInitialized()) {
                    ss.setup(newWidth, newHeight);
                }
            }
        }
    }
}
