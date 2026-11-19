package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

public class ArmasEspeciaisItem {

    // ================= ELYTRA SUPREMA (VOO DO CRIATIVO + SEM FOGOS) =================
    public static class ElytraSuprema extends ElytraItem {
        public final boolean isIrrelevante;

        public ElytraSuprema(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }

        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public boolean canElytraFly(ItemStack stack, LivingEntity entity) { return true; }
        @Override public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) { return true; }

        @Override
        public Component getName(ItemStack stack) {
            return Component.literal(isIrrelevante ? "❖ Elytra Irrelevante ❖" : "✦ Elytra do Universo ✦")
                    .withStyle(isIrrelevante ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA, ChatFormatting.BOLD);
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• Voo Livre do Criativo (Aperte Pulo 2x no Sobrevivência)").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("• Propulsão Infinita sem precisar de Fogos de Artifício").withStyle(ChatFormatting.GOLD));
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            if (entity instanceof Player player) {
                boolean equipada = player.getItemBySlot(EquipmentSlot.CHEST) == stack;
                if (equipada) {
                    if (!player.getAbilities().mayfly) {
                        player.getAbilities().mayfly = true;
                        player.onUpdateAbilities();
                    }
                    player.getAbilities().setFlyingSpeed(isIrrelevante ? 0.16F : 0.11F);
                    if (player.isFallFlying()) {
                        Vec3 look = player.getLookAngle();
                        double vel = isIrrelevante ? 2.4D : 1.7D;
                        player.setDeltaMovement(look.scale(vel));
                        player.fallDistance = 0.0F;
                        if (level instanceof ServerLevel sl && player.tickCount % 2 == 0) {
                            sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.5, player.getZ(), 2, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                } else if (!player.isCreative() && !player.isSpectator() && player.getAbilities().mayfly) {
                    Item peito = player.getItemBySlot(EquipmentSlot.CHEST).getItem();
                    if (!(peito instanceof ElytraSuprema) && !(peito instanceof ArmaduraSupremaItem)) {
                        player.getAbilities().mayfly = false;
                        player.getAbilities().flying = false;
                        player.onUpdateAbilities();
                    }
                }
            }
        }
    }

    // ================= LANÇA SUPREMA =================
    public static class LancaSuprema extends TridentItem {
        public final boolean isIrrelevante;

        public LancaSuprema(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }

        @Override public boolean isFoil(ItemStack stack) { return true; }

        @Override
        public Component getName(ItemStack stack) {
            return Component.literal(isIrrelevante ? "❖ Lança Irrelevante ❖" : "✦ Lança do Universo ✦")
                    .withStyle(isIrrelevante ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA, ChatFormatting.BOLD);
        }

        @Override
        public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
            if (!player.level().isClientSide()) ExampleMod.executarAlvo(entity, player, isIrrelevante);
            return true;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide() && level instanceof ServerLevel sl) {
                Vec3 dir = player.getLookAngle().normalize();
                Vec3 olho = player.getEyePosition();
                for (int d = 2; d <= 50; d++) {
                    Vec3 p = olho.add(dir.scale(d));
                    sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.01);
                    for (Entity alvo : sl.getEntities(player, new AABB(p.x - 2.0, p.y - 2.0, p.z - 2.0, p.x + 2.0, p.y + 2.0, p.z + 2.0))) {
                        ExampleMod.executarAlvo(alvo, player, isIrrelevante);
                    }
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.5F, 1.4F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }
    
