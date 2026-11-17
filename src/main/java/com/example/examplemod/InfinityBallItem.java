
package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nullable;
import java.util.List;

public class InfinityBallItem extends Item {
    public InfinityBallItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("🔮 The Infinity Ball 🔮").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("• Teleporte sem dano (Jogadores, Coordenadas e Dimensões)").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("• Capaz de criar um Universo Próprio").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("[Clique Direito] Abrir Menu do Infinito").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            sp.sendSystemMessage(Component.literal("══════ 🔮 THE INFINITY BALL 🔮 ══════").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            sp.sendSystemMessage(criarBotao("[ ✦ CRIAR E IR PARA MEU UNIVERSO ✦ ]", "/infinity universo", ChatFormatting.GOLD));
            sp.sendSystemMessage(criarBotao("[ 🌍 Dimensão: Overworld ]", "/infinity dim overworld", ChatFormatting.GREEN)
                    .append(criarBotao("  [ 🔥 Nether ]", "/infinity dim nether", ChatFormatting.RED))
                    .append(criarBotao("  [ 🌌 The End ]", "/infinity dim end", ChatFormatting.DARK_PURPLE)));

            MutableComponent coordBtn = Component.literal("[ 📍 Ir para Coordenadas (Clique e digite X Y Z) ]")
                    .withStyle(s -> s.withColor(ChatFormatting.AQUA).withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/infinity coords 0 120 0")));
            sp.sendSystemMessage(coordBtn);

            if (sp.getServer() != null) {
                for (ServerPlayer alvo : sp.getServer().getPlayerList().getPlayers()) {
                    if (alvo != sp) {
                        sp.sendSystemMessage(criarBotao("[ 👤 Teleportar até: " + alvo.getName().getString() + " ]",
                                "/infinity player " + alvo.getName().getString(), ChatFormatting.YELLOW));
                    }
                }
            }
            sp.sendSystemMessage(Component.literal("══════════════════════════════════").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    private static MutableComponent criarBotao(String texto, String comando, ChatFormatting cor) {
        return Component.literal(texto).withStyle(s -> s.withColor(cor).withBold(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, comando)));
    }

    public static void criarUniversoProprio(ServerPlayer player) {
        ServerLevel endLevel = player.server.getLevel(Level.END);
        if (endLevel == null) endLevel = player.serverLevel();
        int baseX = ((int) (Math.abs(player.getUUID().getLeastSignificantBits()) % 500) + 100) * 100;
        int baseZ = ((int) (Math.abs(player.getUUID().getMostSignificantBits()) % 500) + 100) * 100;
        BlockPos centro = new BlockPos(baseX, 200, baseZ);

        for (int x = -12; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                BlockPos pos = centro.offset(x, 0, z);
                boolean borda = Math.abs(x) == 12 || Math.abs(z) == 12;
                endLevel.setBlockAndUpdate(pos, borda ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.CRYING_OBSIDIAN.defaultBlockState());
            }
        }
        endLevel.setBlockAndUpdate(centro, Blocks.BEACON.defaultBlockState());
        player.teleportTo(endLevel, baseX + 0.5, 202.0, baseZ + 0.5, player.getYRot(), player.getXRot());
        player.fallDistance = 0.0F;
        endLevel.playSound(null, centro, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0F, 1.2F);
        player.displayClientMessage(Component.literal("✦ Universo Próprio criado nas coordenadas " + baseX + ", 200, " + baseZ + "! ✦")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
    }
}
