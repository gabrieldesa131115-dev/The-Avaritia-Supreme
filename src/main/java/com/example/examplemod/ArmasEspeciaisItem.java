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
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
        @Override public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) { return true; }

        @Override
        public Component getName(ItemStack stack) {
            return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Elytra Multiversal ❖" : "✦ Elytra do Universo ✦");
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• Voo Livre do Criativo (Aperte Pulo 2x no Sobrevivência)").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("• Propulsão Infinita sem precisar de Fogos").withStyle(ChatFormatting.GOLD));
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
                        player.setDeltaMovement(look.scale(isIrrelevante ? 2.4D : 1.7D));
                        player.fallDistance = 0.0F;
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

    public static class LancaSuprema extends TridentItem {
        public final boolean isIrrelevante;
        public LancaSuprema(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Lança Multiversal ❖" : "✦ Lança do Universo ✦");
        }
        @Override public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
            if (!player.level().isClientSide()) ExampleMod.executarAlvo(entity, player, isIrrelevante);
            return true;
        }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
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
        public static class MacaSuprema extends SwordItem {
        public final boolean isIrrelevante;
        public MacaSuprema(boolean isIrrelevante) {
            super(FerramentaSupremaItem.TIER_APICE, 999999999, 100.0F, new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Maça Multiversal ❖" : "✦ Maça do Universo ✦");
        }
        @Override public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
            if (!player.level().isClientSide()) ExampleMod.executarAlvo(entity, player, isIrrelevante);
            return true;
        }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide() && level instanceof ServerLevel sl) {
                for (Entity alvo : sl.getEntities(player, player.getBoundingBox().inflate(isIrrelevante ? 35.0D : 22.0D))) {
                    ExampleMod.executarAlvo(alvo, player, isIrrelevante);
                }
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 5, 3.0, 0.5, 3.0, 0.1);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 0.7F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    public static class TotemSupremo extends Item {
        public final boolean isIrrelevante;
        public TotemSupremo(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Totem da Yggdrasil ❖" : "✦ Totem da Vida Universal ✦");
        }
    }

    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            boolean temYgg = player.getMainHandItem().is(ExampleMod.TOTEM_YGGDRASIL.get())
                    || player.getOffhandItem().is(ExampleMod.TOTEM_YGGDRASIL.get())
                    || player.getInventory().contains(new ItemStack(ExampleMod.TOTEM_YGGDRASIL.get()));
            boolean temUni = player.getMainHandItem().is(ExampleMod.TOTEM_ABSOLUTO.get())
                    || player.getOffhandItem().is(ExampleMod.TOTEM_ABSOLUTO.get())
                    || player.getInventory().contains(new ItemStack(ExampleMod.TOTEM_ABSOLUTO.get()));
            if (temYgg || temUni) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
                player.removeAllEffects();
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 4));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 9));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 4));
                if (temYgg && player.level() instanceof ServerLevel sl) {
                    for (Entity e : sl.getEntities(player, player.getBoundingBox().inflate(30.0D))) {
                        if (e instanceof Enemy) ExampleMod.executarAlvo(e, player, true);
                    }
                }
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.5F, 1.0F);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante(temYgg ? "❖ TOTEM DA YGGDRASIL ATIVADO! ❖" : "✦ TOTEM DA VIDA UNIVERSAL ATIVADO! ✦"), true);
            }
        }
    }
}
