package github.trollhack.modules.impl.movement;

import github.trollhack.events.impl.PacketEvent;
import github.trollhack.events.impl.TickEvent;
import github.trollhack.mixins.accessors.IEntityVelocityUpdateS2CPacket;
import github.trollhack.mixins.accessors.IExplosionS2CPacket;
import github.trollhack.modules.Category;
import github.trollhack.modules.Module;
import github.trollhack.settings.impl.BooleanSetting;
import github.trollhack.settings.impl.EnumSetting;
import github.trollhack.settings.impl.FloatSetting;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.Direction;

public class Velocity extends Module {
    public static final Velocity INSTANCE = new Velocity();

    public enum Mode {
        CUSTOM,
        NCP
    }

    public final EnumSetting<Mode> mode = enumSetting("Mode", Mode.CUSTOM);
    public final FloatSetting horizontal = floatSetting("Horizontal", 0.0f, 0.0f, 100.0f, 1.0f, () -> mode.getValue() == Mode.CUSTOM);
    public final FloatSetting vertical = floatSetting("Vertical", 0.0f, 0.0f, 100.0f, 1.0f, () -> mode.getValue() == Mode.CUSTOM);
    public final BooleanSetting explosions = booleanSetting("Explosions", true);
    public final BooleanSetting pauseInLiquid = booleanSetting("PauseInLiquid", false);
    public final BooleanSetting pauseOnFlag = booleanSetting("PauseOnFlag", true);

    private boolean flag;
    private int flagCooldown;

    public Velocity() {
        super("Velocity", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        flag = false;
        flagCooldown = 0;
    }

    @Override
    public void onDisable() {
        flag = false;
        flagCooldown = 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPacketReceive(PacketEvent.Receive event) {
        if (nullCheck()) return;
        if (pauseInLiquid.getValue() && (mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava())) return;
        if (flagCooldown > 0 && pauseOnFlag.getValue()) {
            flagCooldown--;
            return;
        }
        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
            flagCooldown = 5;
            return;
        }

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
            if (packet.getId() != mc.player.getId()) return;
            if (mode.getValue() == Mode.CUSTOM) {
                IEntityVelocityUpdateS2CPacket accessor = (IEntityVelocityUpdateS2CPacket) packet;
                float h = horizontal.getValue() / 100.0f;
                float v = vertical.getValue() / 100.0f;
                if (h == 0.0f && v == 0.0f) {
                    accessor.setVelocityX(0);
                    accessor.setVelocityY(0);
                    accessor.setVelocityZ(0);
                } else {
                    accessor.setVelocityX((int) (packet.getVelocityX() * h));
                    accessor.setVelocityY((int) (packet.getVelocityY() * v));
                    accessor.setVelocityZ((int) (packet.getVelocityZ() * h));
                }
            } else {
                if (!flag) {
                    event.setCancelled(true);
                    flag = true;
                } else {
                    flag = false;
                    IEntityVelocityUpdateS2CPacket accessor = (IEntityVelocityUpdateS2CPacket) packet;
                    accessor.setVelocityX((int) (packet.getVelocityX() * -0.1));
                    accessor.setVelocityZ((int) (packet.getVelocityZ() * -0.1));
                }
            }
        }

        if (event.getPacket() instanceof ExplosionS2CPacket packet && explosions.getValue()) {
            IExplosionS2CPacket accessor = (IExplosionS2CPacket) packet;
            if (mode.getValue() == Mode.CUSTOM) {
                float h = horizontal.getValue() / 100.0f;
                float v = vertical.getValue() / 100.0f;
                accessor.setPlayerVelocityX(accessor.getPlayerVelocityX() * h);
                accessor.setPlayerVelocityY(accessor.getPlayerVelocityY() * v);
                accessor.setPlayerVelocityZ(accessor.getPlayerVelocityZ() * h);
            } else {
                accessor.setPlayerVelocityX(0);
                accessor.setPlayerVelocityY(0);
                accessor.setPlayerVelocityZ(0);
                flag = true;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent event) {
        if (nullCheck()) return;
        if (flag && mode.getValue() == Mode.NCP) {
            if (flagCooldown <= 0) {
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                        mc.player.getBlockPos(),
                        Direction.DOWN
                ));
            }
            flag = false;
        }
    }
}
