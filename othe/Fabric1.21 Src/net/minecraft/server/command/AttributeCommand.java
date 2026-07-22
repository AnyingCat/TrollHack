/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic3CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AttributeCommand {
    private static final DynamicCommandExceptionType ENTITY_FAILED_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.attribute.failed.entity", name));
    private static final Dynamic2CommandExceptionType NO_ATTRIBUTE_EXCEPTION = new Dynamic2CommandExceptionType((entityName, attributeName) -> Text.stringifiedTranslatable("commands.attribute.failed.no_attribute", entityName, attributeName));
    private static final Dynamic3CommandExceptionType NO_MODIFIER_EXCEPTION = new Dynamic3CommandExceptionType((entityName, attributeName, uuid) -> Text.stringifiedTranslatable("commands.attribute.failed.no_modifier", attributeName, entityName, uuid));
    private static final Dynamic3CommandExceptionType MODIFIER_ALREADY_PRESENT_EXCEPTION = new Dynamic3CommandExceptionType((entityName, attributeName, uuid) -> Text.stringifiedTranslatable("commands.attribute.failed.modifier_already_present", uuid, attributeName, entityName));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("attribute").requires(source -> source.hasPermissionLevel(2))).then(CommandManager.argument("target", EntityArgumentType.entity()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("attribute", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ATTRIBUTE)).then((ArgumentBuilder<ServerCommandSource, ?>)((LiteralArgumentBuilder)CommandManager.literal("get").executes(context -> AttributeCommand.executeValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), 1.0))).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes(context -> AttributeCommand.executeValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), DoubleArgumentType.getDouble(context, "scale")))))).then(((LiteralArgumentBuilder)CommandManager.literal("base").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("set").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("value", DoubleArgumentType.doubleArg()).executes(context -> AttributeCommand.executeBaseValueSet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), DoubleArgumentType.getDouble(context, "value")))))).then(((LiteralArgumentBuilder)CommandManager.literal("get").executes(context -> AttributeCommand.executeBaseValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), 1.0))).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes(context -> AttributeCommand.executeBaseValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), DoubleArgumentType.getDouble(context, "scale"))))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("modifier").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("add").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("id", IdentifierArgumentType.identifier()).then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("value", DoubleArgumentType.doubleArg()).then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("add_value").executes(context -> AttributeCommand.executeModifierAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), IdentifierArgumentType.getIdentifier(context, "id"), DoubleArgumentType.getDouble(context, "value"), EntityAttributeModifier.Operation.ADD_VALUE)))).then(CommandManager.literal("add_multiplied_base").executes(context -> AttributeCommand.executeModifierAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), IdentifierArgumentType.getIdentifier(context, "id"), DoubleArgumentType.getDouble(context, "value"), EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE)))).then(CommandManager.literal("add_multiplied_total").executes(context -> AttributeCommand.executeModifierAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), IdentifierArgumentType.getIdentifier(context, "id"), DoubleArgumentType.getDouble(context, "value"), EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))))))).then(CommandManager.literal("remove").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument("id", IdentifierArgumentType.identifier()).executes(context -> AttributeCommand.executeModifierRemove((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), IdentifierArgumentType.getIdentifier(context, "id")))))).then(CommandManager.literal("value").then((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("get").then((ArgumentBuilder<ServerCommandSource, ?>)((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).executes(context -> AttributeCommand.executeModifierValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), IdentifierArgumentType.getIdentifier(context, "id"), 1.0))).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes(context -> AttributeCommand.executeModifierValueGet((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity(context, "target"), RegistryEntryReferenceArgumentType.getEntityAttribute(context, "attribute"), IdentifierArgumentType.getIdentifier(context, "id"), DoubleArgumentType.getDouble(context, "scale")))))))))));
    }

    private static EntityAttributeInstance getAttributeInstance(Entity entity, RegistryEntry<EntityAttribute> attribute) throws CommandSyntaxException {
        EntityAttributeInstance entityAttributeInstance = AttributeCommand.getLivingEntity(entity).getAttributes().getCustomInstance(attribute);
        if (entityAttributeInstance == null) {
            throw NO_ATTRIBUTE_EXCEPTION.create(entity.getName(), AttributeCommand.getName(attribute));
        }
        return entityAttributeInstance;
    }

    private static LivingEntity getLivingEntity(Entity entity) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity)) {
            throw ENTITY_FAILED_EXCEPTION.create(entity.getName());
        }
        return (LivingEntity)entity;
    }

    private static LivingEntity getLivingEntityWithAttribute(Entity entity, RegistryEntry<EntityAttribute> attribute) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getLivingEntity(entity);
        if (!livingEntity.getAttributes().hasAttribute(attribute)) {
            throw NO_ATTRIBUTE_EXCEPTION.create(entity.getName(), AttributeCommand.getName(attribute));
        }
        return livingEntity;
    }

    private static int executeValueGet(ServerCommandSource source, Entity target, RegistryEntry<EntityAttribute> attribute, double multiplier) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getLivingEntityWithAttribute(target, attribute);
        double d = livingEntity.getAttributeValue(attribute);
        source.sendFeedback(() -> Text.translatable("commands.attribute.value.get.success", AttributeCommand.getName(attribute), target.getName(), d), false);
        return (int)(d * multiplier);
    }

    private static int executeBaseValueGet(ServerCommandSource source, Entity target, RegistryEntry<EntityAttribute> attribute, double multiplier) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getLivingEntityWithAttribute(target, attribute);
        double d = livingEntity.getAttributeBaseValue(attribute);
        source.sendFeedback(() -> Text.translatable("commands.attribute.base_value.get.success", AttributeCommand.getName(attribute), target.getName(), d), false);
        return (int)(d * multiplier);
    }

    private static int executeModifierValueGet(ServerCommandSource source, Entity target, RegistryEntry<EntityAttribute> attribute, Identifier id, double multiplier) throws CommandSyntaxException {
        LivingEntity livingEntity = AttributeCommand.getLivingEntityWithAttribute(target, attribute);
        AttributeContainer attributeContainer = livingEntity.getAttributes();
        if (!attributeContainer.hasModifierForAttribute(attribute, id)) {
            throw NO_MODIFIER_EXCEPTION.create(target.getName(), AttributeCommand.getName(attribute), id);
        }
        double d = attributeContainer.getModifierValue(attribute, id);
        source.sendFeedback(() -> Text.translatable("commands.attribute.modifier.value.get.success", Text.of(id), AttributeCommand.getName(attribute), target.getName(), d), false);
        return (int)(d * multiplier);
    }

    private static int executeBaseValueSet(ServerCommandSource source, Entity target, RegistryEntry<EntityAttribute> attribute, double value) throws CommandSyntaxException {
        AttributeCommand.getAttributeInstance(target, attribute).setBaseValue(value);
        source.sendFeedback(() -> Text.translatable("commands.attribute.base_value.set.success", AttributeCommand.getName(attribute), target.getName(), value), false);
        return 1;
    }

    private static int executeModifierAdd(ServerCommandSource source, Entity target, RegistryEntry<EntityAttribute> attribute, Identifier id, double value, EntityAttributeModifier.Operation operation) throws CommandSyntaxException {
        EntityAttributeInstance entityAttributeInstance = AttributeCommand.getAttributeInstance(target, attribute);
        EntityAttributeModifier entityAttributeModifier = new EntityAttributeModifier(id, value, operation);
        if (entityAttributeInstance.hasModifier(id)) {
            throw MODIFIER_ALREADY_PRESENT_EXCEPTION.create(target.getName(), AttributeCommand.getName(attribute), id);
        }
        entityAttributeInstance.addPersistentModifier(entityAttributeModifier);
        source.sendFeedback(() -> Text.translatable("commands.attribute.modifier.add.success", Text.of(id), AttributeCommand.getName(attribute), target.getName()), false);
        return 1;
    }

    private static int executeModifierRemove(ServerCommandSource source, Entity target, RegistryEntry<EntityAttribute> attribute, Identifier id) throws CommandSyntaxException {
        EntityAttributeInstance entityAttributeInstance = AttributeCommand.getAttributeInstance(target, attribute);
        if (entityAttributeInstance.removeModifier(id)) {
            source.sendFeedback(() -> Text.translatable("commands.attribute.modifier.remove.success", Text.of(id), AttributeCommand.getName(attribute), target.getName()), false);
            return 1;
        }
        throw NO_MODIFIER_EXCEPTION.create(target.getName(), AttributeCommand.getName(attribute), id);
    }

    private static Text getName(RegistryEntry<EntityAttribute> attribute) {
        return Text.translatable(attribute.value().getTranslationKey());
    }
}

