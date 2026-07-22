package github.trollhack.utils.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public class ProjectionUtil {
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Vec3d worldToScreen(Vec3d worldPos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        if (camera == null) return null;

        float x = (float) (worldPos.x - camera.getPos().x);
        float y = (float) (worldPos.y - camera.getPos().y);
        float z = (float) (worldPos.z - camera.getPos().z);

        Vector4f pos = new Vector4f(x, y, z, 1.0f);
        pos.mul(lastWorldSpaceMatrix);

        Matrix4f mvp = new Matrix4f(lastProjMat);
        mvp.mul(lastModMat);

        Vector4f clip = new Vector4f(pos.x(), pos.y(), pos.z(), pos.w());
        mvp.transform(clip);

        if (clip.w <= 0.001f) return null;

        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        float ndcZ = clip.z / clip.w;

        if (ndcX < -1.0f || ndcX > 1.0f || ndcY < -1.0f || ndcY > 1.0f) {
            if (ndcX < -2.0f || ndcX > 2.0f || ndcY < -2.0f || ndcY > 2.0f) return null;
        }

        float scaleFactor = (float) mc.getWindow().getScaleFactor();
        float screenWidth = mc.getWindow().getFramebufferWidth() / scaleFactor;
        float screenHeight = mc.getWindow().getFramebufferHeight() / scaleFactor;

        float screenX = (ndcX + 1.0f) / 2.0f * screenWidth;
        float screenY = (1.0f - ndcY) / 2.0f * screenHeight;

        return new Vec3d(screenX, screenY, ndcZ);
    }
}
