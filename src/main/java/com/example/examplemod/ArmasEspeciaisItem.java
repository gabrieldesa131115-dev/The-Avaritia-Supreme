package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class ArmasEspeciaisItem {

    public static class ElytraSuprema extends ElytraItem {
        public final boolean isIrrelevante;
        public ElytraSuprema(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public boolean canElytraFly(ItemStack stack, LivingEntity entity) { return true; }
        @Override public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
            if (entity.isShiftKeyDown()) {
                Vec3 look = entity.getLookAngle();
                double vel = isIrrelevante ? 2.5 : 1.4;
                entity.setDeltaMovement(look.scale(vel));
            }
            return true;
        }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(isIrrelevante ? "❖ Asas da Singularidade Irrelevante ❖" : "✦ Asas do Cosmos Imensurável ✦")
                    .withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• Voo supersônico infinito (Segure Shift voando para acelerar)").withStyle(ChatFormatting.AQUA));
        }
    }

    public static class LancaSuprema extends TridentItem {
        public final boolean isIrrelevante;
        public LancaSuprema(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(isIrrelevante ? "❖ Lança da Singularidade Irrelevante ❖" : "✦ Lança do Cosmos Imensurável ✦")
                    .withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker instanceof Player p) ExampleMod.executarAlvo(target, p, isIrrelevante);
            return true;
        }
    }

    public static class MacaSuprema extends SwordItem {
        public final boolean isIrrelevante;
        public MacaSuprema(boolean isIrrelevante) {
            super(Tiers.NETHERITE, 99999999, -2.4F, new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(isIrrelevante ? "❖ Maça da Singularidade Irrelevante ❖" : "✦ Maça do Cosmos Imensurável ✦")
                    .withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        @Override public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker instanceof Player p && p.level() instanceof ServerLevel sl) {
                double raioImpacto = Math.max(8.0, p.fallDistance * (isIrrelevante ? 4.0 : 2.0));
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, target.getX(), target.getY(), target.getZ(), 3, 1.0, 0.5, 1.0, 0.1);
                for (Entity e : sl.getEntities(p, target.getBoundingBox().inflate(raioImpacto))) {
                    ExampleMod.executarAlvo(e, p, isIrrelevante);
                }
                p.fallDistance = 0.0F;
            }
            return true;
        }
    }

    public static class TotemSupremo extends Item {
        public TotemSupremo() {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return Component.literal("✦ Totem da Eternidade Absoluta ✦").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• Impede qualquer morte automaticamente sem ser consumido").withStyle(ChatFormatting.AQUA));
        }
    }
}

