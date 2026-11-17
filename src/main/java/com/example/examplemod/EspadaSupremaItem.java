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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class EspadaSupremaItem extends SwordItem {
    public final boolean isIrrelevante;

    public static final String[] MODOS = {
            "Onda Expansiva Cósmica",
            "Cortes Cósmicos à Distância",
            "Expurgo: Mobs Agressivos",
            "Expurgo: Mobs Passivos",
            "Expurgo: Todos os Mobs",
            "Expurgo: Todos os Jogadores",
            "ANIQUILAÇÃO TOTAL (Tudo)"
    };

    public EspadaSupremaItem(boolean isIrrelevante) {
        super(Tiers.NETHERITE, 99999999, isIrrelevante ? -1.0F : -2.0F, new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return isIrrelevante
                ? Component.literal("❖ Espada da Singularidade Irrelevante ❖").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                : Component.literal("✦ Espada do Cosmos Imensurável ✦").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoEspada");
        tooltip.add(Component.literal("Modo: " + MODOS[modo]).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.literal("[Shift + Clique Direito] Trocar Modo").withStyle(ChatFormatting.GRAY));
        if (isIrrelevante) {
            tooltip.add(Component.literal("+Ø IRRELEVANTE Dano de Ataque").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        } else {
            tooltip.add(Component.literal("+∞ IMENSURÁVEL Dano de Ataque").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            int modoAtual = stack.getOrCreateTag().getInt("ModoEspada");

            if (player.isShiftKeyDown()) {
                int proximo = (modoAtual + 1) % MODOS.length;
                stack.getOrCreateTag().putInt("ModoEspada", proximo);
                player.displayClientMessage(Component.literal("Modo: " + MODOS[proximo]).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
                return InteractionResultHolder.success(stack);
            }

            double raio = isIrrelevante ? 80.0 : 35.0;
            if (modoAtual == 0) {
                for (double r = 2.0; r <= 8.0; r += 2.0) {
                    for (int i = 0; i < 24; i++) {
                        double ang = 2 * Math.PI * i / 24;
                        serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX() + Math.cos(ang) * r, player.getY() + 0.5, player.getZ() + Math.sin(ang) * r, 1, 0, 0, 0, 0.02);
                    }
                }
                for (Entity e : level.getEntities(player, player.getBoundingBox().inflate(raio))) {
                    ExampleMod.executarAlvo(e, player, isIrrelevante);
                }
            } else if (modoAtual == 1) {
                Vec3 ini = player.getEyePosition(), dir = player.getLookAngle();
                for (double d = 1.5; d <= (isIrrelevante ? 120.0 : 50.0); d += 1.5) {
                    Vec3 p = ini.add(dir.scale(d));
                    serverLevel.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 2, 0.2, 0.2, 0.2, 0.01);
                    for (Entity e : level.getEntities(player, new AABB(p.x - 2, p.y - 2, p.z - 2, p.x + 2, p.y + 2, p.z + 2))) {
                        ExampleMod.executarAlvo(e, player, isIrrelevante);
                    }
                }
            } else {
                for (Entity e : level.getEntities(player, player.getBoundingBox().inflate(raio))) {
                    boolean agressivo = e instanceof Mob m && m.getTarget() != null || e instanceof net.minecraft.world.entity.monster.Enemy;
                    boolean passivo = e instanceof LivingEntity && !(e instanceof Player) && !agressivo;
                    boolean jogador = e instanceof Player;
                    if ((modoAtual == 2 && agressivo) || (modoAtual == 3 && passivo) || (modoAtual == 4 && (agressivo || passivo)) || (modoAtual == 5 && jogador) || modoAtual == 6) {
                        ExampleMod.executarAlvo(e, player, isIrrelevante);
                    }
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
          }
          
