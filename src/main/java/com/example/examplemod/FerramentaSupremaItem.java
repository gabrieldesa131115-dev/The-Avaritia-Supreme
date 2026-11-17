package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class FerramentaSupremaItem extends DiggerItem {
    public final boolean isIrrelevante;
    private final String nomeCustom;
    public static final int[] RAIOS = {0, 2, 4, 8};
    public static final String[] NOMES_MODO = {"1x1 (Cirúrgico)", "5x5 (Cósmico)", "9x9 (Estelar)", "17x17 (Apagamento Total)"};

    public static final Tier TIER_APICE = new Tier() {
        @Override public int getUses() { return 0; }
        @Override public float getSpeed() { return Float.MAX_VALUE; }
        @Override public float getAttackDamageBonus() { return Float.MAX_VALUE; }
        @Override public int getLevel() { return 999; }
        @Override public int getEnchantmentValue() { return 999; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public FerramentaSupremaItem(boolean isIrrelevante, String nomeCustom) {
        super(100.0F, 0.0F, TIER_APICE, net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
        this.nomeCustom = nomeCustom;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public boolean isCorrectToolForDrops(BlockState state) { return true; }
    @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return Float.MAX_VALUE; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(nomeCustom).withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoMineracao");
        tooltip.add(Component.literal("Modo: " + NOMES_MODO[modo]).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.literal("[Shift + Clique Direito] Trocar Área").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("• SEM LIMITAÇÕES: Apaga Bedrock e blocos 'Não Coloque'").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player.isShiftKeyDown()) {
            int maxModos = isIrrelevante ? 4 : 3;
            int proximo = (stack.getOrCreateTag().getInt("ModoMineracao") + 1) % maxModos;
            stack.getOrCreateTag().putInt("ModoMineracao", proximo);
            player.displayClientMessage(Component.literal("Mineração: " + NOMES_MODO[proximo]).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    public static void quebrarSemLimitacoes(ServerLevel level, BlockPos pos, Player player, ItemStack stack) {
        int raio = RAIOS[stack.getOrCreateTag().getInt("ModoMineracao")];
        for (int x = -raio; x <= raio; x++) {
            for (int y = -raio; y <= raio; y++) {
                for (int z = -raio; z <= raio; z++) {
                    BlockPos alvo = pos.offset(x, y, z);
                    BlockState st = level.getBlockState(alvo);
                    if (!st.isAir()) {
                        ItemStack drop = new ItemStack(st.getBlock().asItem());
                        if (!drop.isEmpty()) {
                            level.addFreshEntity(new ItemEntity(level, alvo.getX() + 0.5, alvo.getY() + 0.5, alvo.getZ() + 0.5, drop));
                        }
                        level.removeBlockEntity(alvo);
                        level.setBlock(alvo, Blocks.AIR.defaultBlockState(), 11);
                    }
                }
            }
        }
    }
}
