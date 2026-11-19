package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FerramentaSupremaItem extends DiggerItem {
    public final boolean isIrrelevante;
    private final String titulo;

    public static final Tier TIER_APICE = new Tier() {
        @Override public int getUses() { return 0; }
        @Override public float getSpeed() { return 999999.0F; }
        @Override public float getAttackDamageBonus() { return 5000.0F; }
        @Override public int getLevel() { return 100; }
        @Override public int getEnchantmentValue() { return 100; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public FerramentaSupremaItem(boolean isIrrelevante, String titulo) {
        super(100.0F, 100.0F, TIER_APICE, BlockTags.MINEABLE_WITH_PICKAXE,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
        this.titulo = titulo;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return 999999.0F; }
    @Override public boolean isCorrectToolForDrops(BlockState state) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal((isIrrelevante ? "❖ " : "✦ ") + titulo + (isIrrelevante ? " ❖" : " ✦"))
                .withStyle(isIrrelevante ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    private static String getNomeModo(int m, boolean irr) {
        return switch (m) {
            case 0 -> "Modo 1: Precisão (1x1)";
            case 1 -> "Modo 2: Túnel (3x3)";
            case 2 -> "Modo 3: Escavação Suprema (9x9)";
            default -> irr ? "Modo 4: Apocalipse (25x25)" : "Modo 4: Martelo Cósmico (16x16)";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoPicareta");
        tooltip.add(Component.literal("Modo Atual: " + getNomeModo(modo, isIrrelevante)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("[Shift + Clique Direito] Trocar Área de Mineração").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("• Quebra até Bedrock e junta todos os drops").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player.isShiftKeyDown()) {
            int proximo = (stack.getOrCreateTag().getInt("ModoPicareta") + 1) % 4;
            stack.getOrCreateTag().putInt("ModoPicareta", proximo);
            player.displayClientMessage(VisualEAdaptacao.textoBrilhante("⛏ " + getNomeModo(proximo, isIrrelevante) + " ⛏"), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide() && level instanceof ServerLevel sl && entity instanceof Player player) {
            int modo = stack.getOrCreateTag().getInt("ModoPicareta");
            int raio = switch (modo) {
                case 0 -> 0;
                case 1 -> 1;
                case 2 -> 4;
                default -> isIrrelevante ? 12 : 8;
            };
            if (raio == 0) return true;

            List<ItemStack> todosDrops = new ArrayList<>();
            for (int x = -raio; x <= raio; x++) {
                for (int y = -raio; y <= raio; y++) {
                    for (int z = -raio; z <= raio; z++) {
                        BlockPos alvo = pos.offset(x, y, z);
                        BlockState st = sl.getBlockState(alvo);
                        if (!st.isAir() && st.getBlock() != Blocks.WATER && st.getBlock() != Blocks.LAVA) {
                            todosDrops.addAll(Block.getDrops(st, sl, alvo, sl.getBlockEntity(alvo), player, stack));
                            sl.setBlock(alvo, Blocks.AIR.defaultBlockState(), 2);
                        }
                    }
                }
            }
            for (ItemStack drop : todosDrops) {
                if (!drop.isEmpty()) {
                    sl.addFreshEntity(new ItemEntity(sl, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
                }
            }
            VisualEAdaptacao.adaptarItem(stack, player, "Escavação");
        }
        return true;
    }
}

