
package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class ArcoBestaSupremaItem {

    public static class ArcoSupremo extends BowItem {
        public final boolean isIrrelevante;
        public ArcoSupremo(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(isIrrelevante ? "❖ Arco dos Céus Irrelevante ❖" : "✦ Arco dos Céus Imensurável ✦")
                    .withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• Flechas Infinitas & Tempestade de Flechas do Céu ao errar").withStyle(ChatFormatting.AQUA));
        }
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        @Override
        public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
            if (!level.isClientSide() && entity instanceof Player player && level instanceof ServerLevel sl) {
                Vec3 ini = player.getEyePosition(), dir = player.getLookAngle();
                double alcance = isIrrelevante ? 120.0 : 60.0;
                boolean acertouAlvo = false;

                for (double d = 2.0; d <= alcance; d += 2.0) {
                    Vec3 p = ini.add(dir.scale(d));
                    sl.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 2, 0.2, 0.2, 0.2, 0.01);
                    for (Entity e : level.getEntities(player, new AABB(p.x - 2.5, p.y - 2.5, p.z - 2.5, p.x + 2.5, p.y + 2.5, p.z + 2.5))) {
                        ExampleMod.executarAlvo(e, player, isIrrelevante);
                        acertouAlvo = true;
                    }
                }

                if (!acertouAlvo) {
                    HitResult hit = level.clip(new ClipContext(ini, ini.add(dir.scale(alcance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
                    Vec3 centro = hit.getLocation();
                    double raioChuva = isIrrelevante ? 18.0 : 10.0;

                    for (int i = 0; i < 36; i++) {
                        double ang = 2 * Math.PI * i / 36;
                        sl.sendParticles(ParticleTypes.END_ROD, centro.x + Math.cos(ang) * raioChuva, centro.y + 0.5, centro.z + Math.sin(ang) * raioChuva, 1, 0, 0, 0, 0.05);
                    }
                    int qtdFlechas = isIrrelevante ? 60 : 30;
                    for (int i = 0; i < qtdFlechas; i++) {
                        double ox = (level.random.nextDouble() - 0.5) * 2.0 * raioChuva;
                        double oz = (level.random.nextDouble() - 0.5) * 2.0 * raioChuva;
                        Arrow flechaCeu = new Arrow(level, player);
                        flechaCeu.setPos(centro.x + ox, centro.y + 25.0 + level.random.nextDouble() * 10.0, centro.z + oz);
                        flechaCeu.setDeltaMovement(0, -3.5, 0);
                        flechaCeu.setBaseDamage(99999999.0D);
                        flechaCeu.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        level.addFreshEntity(flechaCeu);
                    }
                    for (Entity e : level.getEntities(player, new AABB(centro.x - raioChuva, centro.y - 5, centro.z - raioChuva, centro.x + raioChuva, centro.y + 15, centro.z + raioChuva))) {
                        ExampleMod.executarAlvo(e, player, isIrrelevante);
                    }
                }
                level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
        }
    }

    public static class BestaSuprema extends CrossbowItem {
        public final boolean isIrrelevante;
        public BestaSuprema(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(isIrrelevante ? "❖ Besta da Singularidade Irrelevante ❖" : "✦ Besta do Cosmos Imensurável ✦")
                    .withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• Disparo Múltiplo Instantâneo Infinito").withStyle(ChatFormatting.AQUA));
        }
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide()) {
                int qtd = isIrrelevante ? 9 : 5;
                for (int i = 0; i < qtd; i++) {
                    float angulo = (i - qtd / 2.0F) * 6.0F;
                    Arrow flecha = new Arrow(level, player);
                    flecha.shootFromRotation(player, player.getXRot(), player.getYRot() + angulo, 0.0F, 5.0F, 0.0F);
                    flecha.setBaseDamage(99999999.0D);
                    flecha.setNoGravity(true);
                    flecha.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    level.addFreshEntity(flecha);
                }
                level.playSound(null, player.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }
        }
