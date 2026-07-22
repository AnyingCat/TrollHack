package github.trollhack.modules.impl.render;

import github.trollhack.events.impl.PacketEvent;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.ColorSetting;
import github.trollhack.settings.impl.FloatSetting;
import github.trollhack.settings.impl.IntegerSetting;
import github.trollhack.utils.render.Render3DUtil;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NewChunks extends Module {
    public static final NewChunks INSTANCE = new NewChunks();

    private final BooleanSetting relative = booleanSetting("Relative", false);
    private final IntegerSetting yOffset = integerSetting("Y Offset", 0, -256, 256, 1);
    private final ColorSetting color = colorSetting("Color", new Color(255, 64, 64, 200));
    private final FloatSetting thickness = floatSetting("Thickness", 1.5f, 0.1f, 4.0f, 0.1f);
    private final IntegerSetting renderRange = integerSetting("Render Range", 512, 64, 2048, 32);

    private final Set<ChunkPos> chunks = ConcurrentHashMap.newKeySet();
    private static final int MAX_CHUNKS = 8192;

    public NewChunks() {
        super("New Chunks", Category.RENDER);
    }

    @Override
    public String getHudInfo() {
        return String.valueOf(chunks.size());
    }

    @Override
    public void onDisable() {
        chunks.clear();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof ChunkDataS2CPacket packet) {
            ChunkPos pos = new ChunkPos(packet.getChunkX(), packet.getChunkZ());
            if (chunks.add(pos) && chunks.size() > MAX_CHUNKS) {
                if (mc.player != null) {
                    int playerX = (int) mc.player.getX();
                    int playerZ = (int) mc.player.getZ();
                    ChunkPos farthest = null;
                    long maxDist = Long.MIN_VALUE;
                    for (ChunkPos cp : chunks) {
                        long dist = Math.abs(playerX - cp.x) + Math.abs(playerZ - cp.z);
                        if (dist > maxDist) {
                            maxDist = dist;
                            farthest = cp;
                        }
                    }
                    if (farthest != null) {
                        chunks.remove(farthest);
                    }
                }
            }
        }
    }

    @Override
    public void onRender3D(MatrixStack matrices) {
        if (nullCheck()) return;

        Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);

        double y = yOffset.getValue();
        if (relative.getValue()) {
            y += mc.player.getLerpedPos(tickDelta).y;
        }

        double rangeSq = (double) renderRange.getValue() * (double) renderRange.getValue();
        float lw = thickness.getValue();
        Color c = color.getValue();
        float alpha = c.getAlpha() / 255f;

        Iterator<ChunkPos> it = chunks.iterator();
        while (it.hasNext()) {
            ChunkPos pos = it.next();
            double centerX = pos.getCenterX();
            double centerZ = pos.getCenterZ();
            double dx = mc.player.getX() - centerX;
            double dz = mc.player.getZ() - centerZ;
            if (dx * dx + dz * dz > rangeSq) continue;

            double xStart = pos.getStartX() - cameraPos.x;
            double xEnd = pos.getEndX() + 1.0 - cameraPos.x;
            double zStart = pos.getStartZ() - cameraPos.z;
            double zEnd = pos.getEndZ() + 1.0 - cameraPos.z;
            double yy = y - cameraPos.y;

            Render3DUtil.drawFlatRect(matrices, xStart, yy, zStart, xEnd, zEnd, c, alpha, lw);
        }
    }
}
