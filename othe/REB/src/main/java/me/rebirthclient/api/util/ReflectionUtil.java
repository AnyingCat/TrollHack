package me.rebirthclient.api.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.Objects;

public class ReflectionUtil {
   public static <T> T getField(Class<?> clazz, Object instance, int index) {
      try {
         Field field = clazz.getDeclaredFields()[index];
         field.setAccessible(true);
         return (T)field.get(instance);
      } catch (Exception var4) {
         throw new RuntimeException(var4);
      }
   }

   public static <F, T extends F> void copyOf(F from, T to, boolean ignoreFinal) throws NoSuchFieldException, IllegalAccessException {
      Objects.requireNonNull(from);
      Objects.requireNonNull(to);
      Class<?> clazz = from.getClass();

      for(Field field : clazz.getDeclaredFields()) {
         makePublic(field);
         if (!isStatic(field) && (!ignoreFinal || !isFinal(field))) {
            makeMutable(field);
            field.set(to, field.get(from));
         }
      }
   }

   public static <F, T extends F> void copyOf(F from, T to) throws NoSuchFieldException, IllegalAccessException {
      copyOf(from, to, false);
   }

   public static boolean isStatic(Member instance) {
      return (instance.getModifiers() & 8) != 0;
   }

   public static boolean isFinal(Member instance) {
      return (instance.getModifiers() & 16) != 0;
   }

   public static void makeAccessible(AccessibleObject instance, boolean accessible) {
      Objects.requireNonNull(instance);
      instance.setAccessible(accessible);
   }

   public static void makePublic(AccessibleObject instance) {
      makeAccessible(instance, true);
   }

   public static void makeMutable(Member instance) throws NoSuchFieldException, IllegalAccessException {
      Field modifiers = Field.class.getDeclaredField("modifiers");
      makePublic(modifiers);
      modifiers.setInt(instance, instance.getModifiers() & -17);
   }
}
