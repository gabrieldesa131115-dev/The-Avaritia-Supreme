package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class PaEMachadoSupremoItem {

    // ================= PÁ SUPREMA =================
    public static class PaSuprema extends ShovelItem {
        public final boolean isIrrelevante;

        public PaSuprema(boolean isIrrelevante) {
            super(FerramentaSupremaItem.TIER_APICE, 50000000.0F, 100.0F,
                    new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }

        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return 999999.0F; }
        @Override public boolean isCorrectToolForDrops(BlockState state) { return true; }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (!attacker.level().isClientSide() && attacker instanceof Player p) {
                float dano = this.isIrrelevante ? 100000000000000.0F : 50000000.0F;
                target.hurt(p.damageSources().playerAttack(p), dano);
                VisualEAdaptacao.adaptarItem(stack, p, "Impacto Sísmico");
            }
            return true;
        }

        @Override
        public Component getName(ItemStack stack) {
            return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Pá Multiversal ❖" : "✦ Pá do Universo ✦");
        }

        private static String getNomeModo(int m, boolean irr) {
            return switch (m) {
                case 0 -> "Modo 1: Precisão (1x1) & Caminho Normal";
                case 1 -> "Modo 2: Túnel (3x3)";
                case 2 -> "Modo 3: Escavação Suprema (9x9)";
                default -> irr ? "Modo 4: Apocalipse (25x25)" : "Modo 4: Martelo Cósmico (16x16)";
            };
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            int modo = stack.getOrCreateTag().getInt("ModoPa");
            tooltip.add(Component.literal("Modo Atual: " + getNomeModo(modo, isIrrelevante)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            tooltip.add(Component.literal("[Shift + Clique Direito] Trocar Modo da Pá").withStyle(ChatFormatting.YELLOW));
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide() && player.isShiftKeyDown()) {
                int prox = (stack.getOrCreateTag().getInt("ModoPa") + 1) % 4;
                stack.getOrCreateTag().putInt("ModoPa", prox);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("⛏ Pá: " + getNomeModo(prox, isIrrelevante) + " ⛏"), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        @Override
        public InteractionResult useOn(UseOnContext ctx) {
            if (ctx.getPlayer() != null && ctx.getPlayer().isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            return super.useOn(ctx);
        }

        @Override
        public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
            if (!level.isClientSide() && level instanceof ServerLevel sl && entity instanceof Player p) {
                int modo = stack.getOrCreateTag().getInt("ModoPa");
                int raio = switch (modo) { case 0 -> 0; case 1 -> 1; case 2 -> 4; default -> isIrrelevante ? 12 : 8; };
                if (raio == 0) return true;
                List<ItemStack> drops = new ArrayList<>();
                for (int x = -raio; x <= raio; x++) {
                    for (int y = -raio; y <= raio; y++) {
                        for (int z = -raio; z <= raio; z++) {
                            BlockPos alvo = pos.offset(x, y, z);
                            BlockState st = sl.getBlockState(alvo);
                            if (st.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                                drops.addAll(Block.getDrops(st, sl, alvo, null, p, stack));
                                sl.setBlock(alvo, Blocks.AIR.defaultBlockState(), 2);
                            }
                        }
                    }
                }
                for (ItemStack d : drops) {
                    if (!d.isEmpty()) sl.addFreshEntity(new ItemEntity(sl, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, d));
                }
                VisualEAdaptacao.adaptarItem(stack, p, "Terraplanagem");
            }
            return true;
        }
    }
        // ================= MACHADO SUPREMO =================
    public static class MachadoSupremo extends AxeItem {
        public final boolean isIrrelevante;

        public MachadoSupremo(boolean isIrrelevante) {
            super(FerramentaSupremaItem.TIER_APICE, 500000000.0F, 100.0F,
                    new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }

        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return 999999.0F; }
        @Override public boolean isCorrectToolForDrops(BlockState state) { return true; }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (!attacker.level().isClientSide() && attacker instanceof Player p) {
                float dano = this.isIrrelevante ? 10000000000000000.0F : 500000000.0F;
                target.hurt(p.damageSources().playerAttack(p), dano);
                VisualEAdaptacao.adaptarItem(stack, p, "Golpe Colossal");
            }
            return true;
        }

        @Override
        public Component getName(ItemStack stack) {
            return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Machado Multiversal ❖" : "✦ Machado do Universo ✦");
        }

        private static String getNomeModo(int m) {
            return switch (m) {
                case 0 -> "Modo 1: Quebrar Normal";
                case 1 -> "Modo 2: Descascar Madeira em Área";
                case 2 -> "Modo 3: Derrubar Árvore Inteira";
                default -> "Modo 4: Derrubar Árvore (Já Descascada)";
            };
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            int modo = stack.getOrCreateTag().getInt("ModoMachado");
            tooltip.add(Component.literal(getNomeModo(modo)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
            tooltip.add(Component.literal("[Shift + Clique Direito] Trocar Modo do Machado").withStyle(ChatFormatting.YELLOW));
        }

        private static Block getBlocoDescascado(Block original) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(original);
            if (id != null && !id.getPath().startsWith("stripped_")) {
                Block stripped = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id.getNamespace(), "stripped_" + id.getPath()));
                if (stripped != null && stripped != Blocks.AIR) return stripped;
            }
            return original;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide() && player.isShiftKeyDown()) {
                int prox = (stack.getOrCreateTag().getInt("ModoMachado") + 1) % 4;
                stack.getOrCreateTag().putInt("ModoMachado", prox);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("🪓 " + getNomeModo(prox) + " 🪓"), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        @Override
        public InteractionResult useOn(UseOnContext ctx) {
            ItemStack stack = ctx.getItemInHand();
            int modo = stack.getOrCreateTag().getInt("ModoMachado");
            if (!ctx.getLevel().isClientSide() && ctx.getLevel() instanceof ServerLevel sl && ctx.getPlayer() != null && !ctx.getPlayer().isShiftKeyDown() && modo == 1) {
                BlockPos c = ctx.getClickedPos();
                int r = isIrrelevante ? 8 : 4;
                boolean descascou = false;
                for (int x = -r; x  fila = new LinkedList<>();
                Set<BlockPos> visitados = new HashSet<>();
                fila.add(pos);
                visitados.add(pos);

                if (descascada) {
                    Block stripInicial = getBlocoDescascado(state.getBlock());
                    if (stripInicial != state.getBlock()) {
                        sl.addFreshEntity(new ItemEntity(sl, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(stripInicial)));
                    }
                }

                while (!fila.isEmpty() && visitados.size() < 500) {
                    BlockPos atual = fila.poll();
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = 0; dy <= 1; dy++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                BlockPos viz = atual.offset(dx, dy, dz);
                                if (!visitados.contains(viz)) {
                                    BlockState st = sl.getBlockState(viz);
                                    if (st.is(BlockTags.LOGS) || st.is(BlockTags.LEAVES)) {
                                        visitados.add(viz);
                                        fila.add(viz);
                                        if (st.is(BlockTags.LOGS)) {
                                            if (descascada) {
                                                sl.addFreshEntity(new ItemEntity(sl, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(getBlocoDescascado(st.getBlock()))));
                                            } else {
                                                Block.dropResources(st, sl, pos, null, p, stack);
                                            }
                                        }
                                        sl.setBlock(viz, Blocks.AIR.defaultBlockState(), 2);
                                    }
                                }
                            }
                        }
                    }
                }
                VisualEAdaptacao.adaptarItem(stack, p, descascada ? "Corte Descascado" : "Corte Supremo");
            }
            return true;
        }
    }
                }
