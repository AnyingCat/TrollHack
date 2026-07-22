package me.rebirthclient.api.util.troll;

import net.minecraft.util.math.RayTraceResult;

public abstract class RayTraceAction {
   private RayTraceAction() {
   }

   public static final class Calc extends RayTraceAction {
      public static final RayTraceAction.Calc INSTANCE = new RayTraceAction.Calc();

      private Calc() {
      }
   }

   public static final class Null extends RayTraceAction {
      public static final RayTraceAction.Null INSTANCE = new RayTraceAction.Null();

      private Null() {
      }
   }

   public static final class Result extends RayTraceAction {
      private final RayTraceResult rayTraceResult;

      public Result(RayTraceResult rayTraceResult) {
         this.rayTraceResult = rayTraceResult;
      }

      public RayTraceResult getRayTraceResult() {
         return this.rayTraceResult;
      }
   }

   public static final class Skip extends RayTraceAction {
      public static final RayTraceAction.Skip INSTANCE = new RayTraceAction.Skip();

      private Skip() {
      }
   }
}
