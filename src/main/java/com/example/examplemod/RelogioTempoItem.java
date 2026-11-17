package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class RelogioTempoItem extends Item {
    public static boolean TEMPO_GLOBAL_PARADO = false;
    public static final Set<UUID> ENTIDADES_CONGELADAS = new HashSet<>();
    public static final Map<UUID, Vec3> POSICAO_ANTERIOR = new HashMap<>();
    public static final Map<UUID, UUID> ALVO_SELECIONADO = new HashMap<>();

    public RelogioTempoItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("⏳ Cronômetro da Eternidade Absoluta ⏳").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int cat = stack.getOrCreateTag().getInt("CategoriaRelogio");
        String[] nomes = {"1: Clima & Tempo Mundial", "2: Parada Total do Tempo", "3: Controle Individual (Bater no Alvo)"};
        tooltip.add(Component.literal("Categoria Atual: " + nomes[cat]).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.literal("[Clique Direito] Abrir Menu Temporal").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("[Shift + Clique Direito] Trocar Categoria").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            int cat = stack.getOrCreateTag().getInt("CategoriaRelogio");
            if (sp.isShiftKeyDown()) {
                cat = (cat + 1) % 3;
                stack.getOrCreateTag().putInt("CategoriaRelogio", cat);
                level.playSound(null, sp.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
            abrirMenuCategoria(sp, cat);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static void abrirMenuCategoria(ServerPlayer sp, int cat) {
        sp.sendSystemMessage(Component.literal("══════ ⏳ CONTROLE TEMPORAL ⏳ ══════").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        sp.sendSystemMessage(btn("[ 1. Clima/Tempo ]", "/relogio cat 0", cat == 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY)
                .append(btn("  [ 2. Parar Tudo ]", "/relogio cat 1", cat == 1 ? ChatFormatting.RED : ChatFormatting.GRAY))
                .append(btn("  [ 3. Alvo Único ]", "/relogio cat 2", cat == 2 ? ChatFormatting.AQUA : ChatFormatting.GRAY)));

        if (cat == 0) {
            sp.sendSystemMessage(btn("[ ☀ Dia ]", "/relogio hora 1000", ChatFormatting.YELLOW)
                    .append(btn(" [ 🌤 Meio-Dia ]", "/relogio hora 6000", ChatFormatting.GOLD))
                    .append(btn(" [ 🌇 Tarde ]", "/relogio hora 12500", ChatFormatting.RED))
                    .append(btn(" [ 🌙 Noite ]", "/relogio hora 18000", ChatFormatting.DARK_PURPLE)));
            sp.sendSystemMessage(btn("[ +500 Ticks ]", "/relogio add 500", ChatFormatting.AQUA)
                    .append(btn(" [ +1000 Ticks ]", "/relogio add 1000", ChatFormatting.AQUA))
                    .append(btn(" [ +5000 Ticks ]", "/relogio add 5000", ChatFormatting.AQUA)));
            sp.sendSystemMessage(btn("[ 🌈 Limpo ]", "/relogio clima limpo", ChatFormatting.GREEN)
                    .append(btn(" [ 🌧 Chuva ]", "/relogio clima chuva", ChatFormatting.BLUE))
                    .append(btn(" [ ⛈ Tempestade ]", "/relogio clima tempestade", ChatFormatting.DARK_RED)));
        } else if (cat == 1) {
            sp.sendSystemMessage(btn(TEMPO_GLOBAL_PARADO ? "[ ▶ RETOMAR O TEMPO GLOBAL ]" : "[ ⏸ PARAR TUDO AGORA (ZA WARUDO) ]",
                    "/relogio global", TEMPO_GLOBAL_PARADO ? ChatFormatting.GREEN : ChatFormatting.RED));
            sp.sendSystemMessage(btn("[ ⏭ Avançar +20 Ticks no Tempo Parado ]", "/relogio add 20", ChatFormatting.YELLOW));
        } else {
            UUID alvoId = ALVO_SELECIONADO.get(sp.getUUID());
            if (alvoId == null) {
                sp.sendSystemMessage(Component.literal("Bata em qualquer entidade, flecha ou item para travar e controlar!").withStyle(ChatFormatting.YELLOW));
            } else {
                sp.sendSystemMessage(btn("[ ⏸ Congelar/Soltar Alvo ]", "/relogio alvo toggle", ChatFormatting.AQUA)
                        .append(btn(" [ ⏪ Voltar Posição ]", "/relogio alvo voltar", ChatFormatting.GOLD))
                        .append(btn(" [ 💥 Apagar Alvo ]", "/relogio alvo apagar", ChatFormatting.DARK_RED)));
            }
        }
        sp.sendSystemMessage(Component.literal("══════════════════════════════════").withStyle(ChatFormatting.GOLD));
    }

    public static void congelarInstantaneo(Entity target, ServerPlayer player) {
        POSICAO_ANTERIOR.putIfAbsent(target.getUUID(), target.position());
        ENTIDADES_CONGELADAS.add(target.getUUID());
        ALVO_SELECIONADO.put(player.getUUID(), target.getUUID());
        target.setDeltaMovement(Vec3.ZERO);
        target.setNoGravity(true);
        if (target instanceof Mob mob) mob.setNoAi(true);
        abrirMenuCategoria(player, 2);
    }

    private static MutableComponent btn(String texto, String cmd, ChatFormatting cor) {
        return Component.literal(texto).withStyle(s -> s.withColor(cor).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd)));
    }
  }
