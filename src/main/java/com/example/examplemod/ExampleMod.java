package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

public class EspadaSupremaItem extends SwordItem {
    public final boolean isIrrelevante;

    private static final DustParticleOptions AZUL_COSMICO = new DustParticleOptions(new Vector3f(0.1F, 0.7F, 1.0F), 2.2F);
    private static final DustParticleOptions ROXO_COSMICO = new DustParticleOptions(new Vector3f(0.75F, 0.15F, 1.0F), 2.2F);

    public EspadaSupremaItem(boolean isIrrelevante) {
        super(FerramentaSupremaItem.TIER_APICE, isIrrelevante ? 2147483646 : 999999999, 100.0F,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(isIrrelevante ? "❖ Espada do Cosmos Irrelevante ❖" : "✦ Espada do Cosmos Imensurável ✦")
                .withStyle(isIrrelevante ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoEspada") + 1;
        tooltip.add(Component.literal("Modo Atual: " + modo + " — " + getNomeModo(modo - 1)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("[Shift + Clique Direito] Trocar entre os 7 Modos").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("[Clique Direito] Executar Poder Cósmico").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    private static String getNomeModo(int m) {
        return switch (m) {
            case 0 -> "Modo 1: Mobs Hostis (Raio Grande)";
            case 1 -> "Modo 2: Mobs Passivos (Raio Grande)";
            case 2 -> "Modo 3: Players Próximos (Raio Grande)";
            case 3 -> "Modo 4: TODOS Perto (Inclui NPCs e Leviatãs)";
            case 4 -> "Modo 5: TODOS os Players do Server Inteiro";
            case 5 -> "Modo 6: Cortes Cósmicos Azuis e Roxos";
            default -> "Modo 7: Onda Cósmica Expansiva ao Redor";
        };
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide()) {
            ExampleMod.executarAlvo(entity, player, this.isIrrelevante);
            VisualEAdaptacao.adaptarItem(stack, player, "Aniquilação");
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player p) {
            ExampleMod.executarAlvo(target, p, this.isIrrelevante);
            VisualEAdaptacao.adaptarItem(stack, p, "Aniquilação");
        }
        return true;
    }
        @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            if (player.isShiftKeyDown()) {
                int proximo = (stack.getOrCreateTag().getInt("ModoEspada") + 1) % 7;
                stack.getOrCreateTag().putInt("ModoEspada", proximo);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("❖ " + getNomeModo(proximo) + " ❖"), true);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.6F);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }

            int modo = stack.getOrCreateTag().getInt("ModoEspada");
            double raio = isIrrelevante ? 80.0D : 60.0D;
            AABB area = player.getBoundingBox().inflate(raio);
            int apagados = 0;

            if (modo == 4) {
                // Modo 5: Todos os players do server inteiro
                for (ServerPlayer sp : sl.getServer().getPlayerList().getPlayers()) {
                    if (sp != player) {
                        ExampleMod.executarAlvo(sp, player, this.isIrrelevante);
                        apagados++;
                    }
                }
            } else if (modo == 5) {
                // Modo 6: Cortes brilhantes e realistas azuis e roxos cósmicos
                Vec3 olho = player.getEyePosition();
                Vec3 dir = player.getLookAngle().normalize();
                Vec3 lado = dir.cross(new Vec3(0, 1, 0)).normalize();
                for (int d = 2; d <= 36; d++) {
                    Vec3 centro = olho.add(dir.scale(d));
                    for (double s = -3.0; s <= 3.0; s += 0.35) {
                        Vec3 ponto = centro.add(lado.scale(s)).add(0, Math.sin(s * 0.6) * 0.8, 0);
                        sl.sendParticles((Math.abs((int)(s * 10)) % 2 == 0) ? AZUL_COSMICO : ROXO_COSMICO, ponto.x, ponto.y, ponto.z, 2, 0.05, 0.05, 0.05, 0.0);
                    }
                    sl.sendParticles(ParticleTypes.END_ROD, centro.x, centro.y, centro.z, 2, 0.4, 0.4, 0.4, 0.02);
                    for (Entity alvo : sl.getEntities(player, new AABB(centro.x - 3.5, centro.y - 3.5, centro.z - 3.5, centro.x + 3.5, centro.y + 3.5, centro.z + 3.5))) {
                        if (alvo != player && !(alvo instanceof ItemEntity)) {
                            ExampleMod.executarAlvo(alvo, player, this.isIrrelevante);
                            apagados++;
                        }
                    }
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 2.0F, 0.6F);
            } else if (modo == 6) {
                // Modo 7: Onda cósmica expansiva em volta de você
                for (int r = 2; r <= 24; r += 2) {
                    for (int ang = 0; ang < 360; ang += 8) {
                        double rad = Math.toRadians(ang);
                        double px = player.getX() + Math.cos(rad) * r;
                        double pz = player.getZ() + Math.sin(rad) * r;
                        sl.sendParticles((ang % 16 == 0) ? AZUL_COSMICO : ROXO_COSMICO, px, player.getY() + 0.8, pz, 1, 0.1, 0.2, 0.1, 0.0);
                        if (r % 6 == 0) sl.sendParticles(ParticleTypes.END_ROD, px, player.getY() + 1.2, pz, 1, 0.0, 0.1, 0.0, 0.02);
                    }
                }
                for (Entity alvo : sl.getEntities(player, player.getBoundingBox().inflate(24.0D))) {
                    if (alvo != player && !(alvo instanceof ItemEntity)) {
                        ExampleMod.executarAlvo(alvo, player, this.isIrrelevante);
                        apagados++;
                    }
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 2.0F, 1.4F);
            } else {
                // Modos 1, 2, 3 e 4 em raio grande
                for (Entity alvo : sl.getEntities(player, area)) {
                    if (alvo == player || alvo instanceof ItemEntity) continue;
                    boolean mata = switch (modo) {
                        case 0 -> alvo instanceof Enemy;
                        case 1 -> alvo instanceof LivingEntity && !(alvo instanceof Enemy) && !(alvo instanceof Player);
                        case 2 -> alvo instanceof Player;
                        default -> true;
                    };
                    if (mata) {
                        ExampleMod.executarAlvo(alvo, player, this.isIrrelevante);
                        apagados++;
                    }
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.5F);
            }

            if (apagados > 0) {
                VisualEAdaptacao.adaptarItem(stack, player, getNomeModo(modo));
                player.displayClientMessage(Component.literal("⚡ " + apagados + " alvos eliminados (" + getNomeModo(modo) + ")").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
                            }
