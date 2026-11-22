package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class InfinityBallItem extends Item {
    private static int contadorUniversos = 1;

    public InfinityBallItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return VisualEAdaptacao.textoBrilhante("❖ Esfera do Infinito (Infinity Ball) ❖");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("• [Clique Direito] Abrir Menu Dimensional & Universos").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> TelaInfinityBall::abrir);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static void criarUniversoProprio(ServerPlayer sp) {
        criarUniversoPorTipo(sp, "NORMAL");
    }
        public static void criarUniversoPorTipo(ServerPlayer sp, String tipo) {
        ServerLevel sl = sp.server.getLevel(Level.END);
        if (sl == null) sl = sp.serverLevel();

        int baseX = contadorUniversos * 15000;
        int baseZ = contadorUniversos * 15000;
        int baseY = 120;
        contadorUniversos++;

        if ("PLANO".equals(tipo)) {
            for (int dx = -22; !(dx > 22); dx++) {
                for (int dz = -22; !(dz > 22); dz++) {
                    BlockPos pos = new BlockPos(baseX + dx, baseY - 1, baseZ + dz);
                    boolean borda = (Math.abs(dx) == 22 || Math.abs(dz) == 22);
                    sl.setBlockAndUpdate(pos, borda ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.QUARTZ_BLOCK.defaultBlockState());
                }
            }
        } else if ("VAZIO".equals(tipo)) {
            for (int dx = -5; !(dx > 5); dx++) {
                for (int dz = -5; !(dz > 5); dz++) {
                    BlockPos pos = new BlockPos(baseX + dx, baseY - 1, baseZ + dz);
                    sl.setBlockAndUpdate(pos, Blocks.TINTED_GLASS.defaultBlockState());
                }
            }
        } else if ("ILHAS".equals(tipo)) {
            Random r = new Random();
            int[][] centros = {{0, 0, 0}, {16, 4, 12}, {-16, -3, 14}, {12, 6, -15}, {-14, 2, -12}};
            for (int k = 0; !(k >= centros.length); k++) {
                int cx = baseX + centros[k];
                int cy = baseY + centros[k];
                int cz = baseZ + centros[k];
                for (int dx = -6; !(dx > 6); dx++) {
                    for (int dz = -6; !(dz > 6); dz++) {
                        if (dx * dx + dz * dz <= 36) {
                            sl.setBlockAndUpdate(new BlockPos(cx + dx, cy - 1, cz + dz), Blocks.GRASS_BLOCK.defaultBlockState());
                            sl.setBlockAndUpdate(new BlockPos(cx + dx, cy - 2, cz + dz), Blocks.DIRT.defaultBlockState());
                            if (r.nextInt(15) == 0) {
                                sl.setBlockAndUpdate(new BlockPos(cx + dx, cy, cz + dz), Blocks.GLOWSTONE.defaultBlockState());
                            }
                        }
                    }
                }
            }
        } else {
            for (int dx = -14; !(dx > 14); dx++) {
                for (int dz = -14; !(dz > 14); dz++) {
                    if (dx * dx + dz * dz <= 196) {
                        sl.setBlockAndUpdate(new BlockPos(baseX + dx, baseY - 1, baseZ + dz), Blocks.GRASS_BLOCK.defaultBlockState());
                        sl.setBlockAndUpdate(new BlockPos(baseX + dx, baseY - 2, baseZ + dz), Blocks.STONE.defaultBlockState());
                    }
                }
            }
            sl.setBlockAndUpdate(new BlockPos(baseX + 4, baseY, baseZ + 4), Blocks.WATER.defaultBlockState());
            sl.setBlockAndUpdate(new BlockPos(baseX - 4, baseY, baseZ - 4), Blocks.OAK_SAPLING.defaultBlockState());
        }

        sp.teleportTo(sl, baseX + 0.5, baseY + 1.0, baseZ + 0.5, sp.getYRot(), sp.getXRot());
        sp.fallDistance = 0.0F;
        sl.sendParticles(ParticleTypes.END_ROD, baseX, baseY + 2.0, baseZ, 80, 3.0, 2.0, 3.0, 0.1);
        sl.playSound(null, baseX, baseY, baseZ, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.5F, 1.2F);
        sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("🌌 Universo (" + tipo + ") criado com sucesso!"), true);
    }

    public static void processarComando(ServerPlayer sp, String payload) {
        try {
            String[] p = payload.split("\\|", 4);
            String acao = p;
            if ("UNI".equals(acao)) {
                criarUniversoPorTipo(sp, p);
            } else if ("TP".equals(acao)) {
                ServerPlayer alvo = sp.server.getPlayerList().getPlayerByName(p);
                if (alvo != null) {
                    sp.teleportTo(alvo.serverLevel(), alvo.getX(), alvo.getY(), alvo.getZ(), alvo.getYRot(), alvo.getXRot());
                    sp.fallDistance = 0.0F;
                    sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("⚡ Teleportado para " + alvo.getGameProfile().getName() + "!"), true);
                }
            } else if ("DIM".equals(acao)) {
                ResourceKey key = "nether".equals(p) ? Level.NETHER : ("end".equals(p) ? Level.END : Level.OVERWORLD);
                ServerLevel dest = sp.server.getLevel(key);
                if (dest != null) {
                    sp.teleportTo(dest, 0.5, 100.0, 0.5, sp.getYRot(), sp.getXRot());
                    sp.fallDistance = 0.0F;
                }
            } else if ("XYZ".equals(acao)) {
                double x = Double.parseDouble(p);
                double y = Double.parseDouble(p);
                double z = Double.parseDouble(p);
                sp.teleportTo(sp.serverLevel(), x, y, z, sp.getYRot(), sp.getXRot());
                sp.fallDistance = 0.0F;
            } else if ("SALVAR".equals(acao)) {
                ExampleMod.POSICAO_SALVA.put(sp.getUUID(), sp.position());
                sp.displayClientMessage(Component.literal("📍 Posição salva!").withStyle(ChatFormatting.GREEN), true);
            } else if ("IRSALVO".equals(acao)) {
                Vec3 pos = (Vec3) ExampleMod.POSICAO_SALVA.get(sp.getUUID());
                if (pos != null) {
                    sp.teleportTo(sp.serverLevel(), pos.x, pos.y, pos.z, sp.getYRot(), sp.getXRot());
                    sp.fallDistance = 0.0F;
                }
            }
        } catch (Exception ignored) {}
    }
}
