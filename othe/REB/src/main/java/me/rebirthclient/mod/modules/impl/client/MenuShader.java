package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import me.rebirthclient.api.util.render.shaders.ShaderProducer;
import me.rebirthclient.api.util.render.shaders.shaders.AquaShader;
import me.rebirthclient.api.util.render.shaders.shaders.BasicShader;
import me.rebirthclient.api.util.render.shaders.shaders.FlowShader;
import me.rebirthclient.api.util.render.shaders.shaders.GangGlShader;
import me.rebirthclient.api.util.render.shaders.shaders.PurpleShader;
import me.rebirthclient.api.util.render.shaders.shaders.SmokeShader;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class MenuShader extends Module {
   public static MenuShader INSTANCE;
   public final Setting<MenuShader.ShaderMode> shader = this.add(new Setting<>("Shader Mode", MenuShader.ShaderMode.FLOW));
   public final Setting<Boolean> animation = this.add(new Setting<>("Animation", true));
   public final Setting<Integer> animationSpeed = this.add(new Setting<>("Animation Speed", 1, 1, 10));

   public MenuShader() {
      super("MenuShader", "good render", Category.CLIENT);
      INSTANCE = this;
   }

   @Override
   public String getInfo() {
      return this.shader.getValue().getName();
   }

   public static enum ShaderMode {
      Aqua("Aqua", AquaShader::INSTANCE),
      FLOW("Flow", FlowShader::INSTANCE),
      SMOKE("Smoke", SmokeShader::INSTANCE),
      GANG("Gang", GangGlShader::INSTANCE),
      GAMER("Gamer", () -> BasicShader.INSTANCE("gamer.frag", 0.03F)),
      CODEX("Codex", () -> BasicShader.INSTANCE("codex.frag")),
      GALAXY("Galaxy", () -> BasicShader.INSTANCE("galaxy33.frag", 0.001F)),
      DDEV("Ddev", () -> BasicShader.INSTANCE("ddev.frag")),
      CRAZY("Crazy", () -> BasicShader.INSTANCE("crazy.frag", 0.01F)),
      SNOW("Snow", () -> BasicShader.INSTANCE("snow.frag", 0.01F)),
      TECHNO("Techno", () -> BasicShader.INSTANCE("techno.frag", 0.01F)),
      GOLDEN("Golden", () -> BasicShader.INSTANCE("golden.frag", 0.01F)),
      HOTSHIT("HotShit", () -> BasicShader.INSTANCE("hotshit.frag", 0.005F)),
      GUISHADER("GuiShader", () -> BasicShader.INSTANCE("clickguishader.frag", 0.02F)),
      HIDEF("Hidef", () -> BasicShader.INSTANCE("hidef.frag", 0.05F)),
      HOMIE("Homie", () -> BasicShader.INSTANCE("homie.frag", 0.001F)),
      KFC("KFC", () -> BasicShader.INSTANCE("kfc.frag", 0.01F)),
      OHMYLORD("Lord", () -> BasicShader.INSTANCE("ohmylord.frag", 0.01F)),
      SMOKY("Smoky", () -> BasicShader.INSTANCE("smoky.frag", 0.001F)),
      STROXINJAT("Stroxinjat", () -> BasicShader.INSTANCE("stroxinjat.frag")),
      WEIRD("Weird", () -> BasicShader.INSTANCE("weird.frag", 0.01F)),
      YIPPIEOWNS("YippieOwns", () -> BasicShader.INSTANCE("yippieOwns.frag")),
      PURPLE("Purple", PurpleShader::INSTANCE);

      private final String name;
      private final ShaderProducer shaderProducer;

      private ShaderMode(String name, ShaderProducer shaderProducer) {
         this.name = name;
         this.shaderProducer = shaderProducer;
      }

      public String getName() {
         return this.name;
      }

      public FramebufferShader getShader() {
         return this.shaderProducer.INSTANCE();
      }
   }
}
