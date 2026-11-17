package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class FerramentaSupremaItem extends DiggerItem {
    public final boolean isIrrelevante;
    private final String nomeCustom;
    public static final int[] RAIOS = {0, 2, 4, 7};
    public static final String[] NOMES_MODO = {"1x1 (Preciso)", "5x5 (Área Média)", "9x9 (Área Grande)", "15x15 (Devastação)"};

    public static final Tier TIER_SUPREMO = new Tier() {
        @Override public int getUses() { return 0; }
        @Override public float getSpeed() { return 9999.0F; }
        @Override public float getAttackDamageBonus() { return 999999.0F; }
        @Override public int getLevel() { return 10; }
        @Override public int getEnchantmentValue() { return 100; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
    };

    public FerramentaSupremaItem(boolean isIrrelevante, String nomeCustom) {
        super(100.0F, 0.0F, TIER_SUPREMO, net.minecraft.tags.BlockTags.MINEABLE_WITH_PICKAXE,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
        this.nomeCustom = nomeCustom;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public boolean isCorrectToolForDrops(BlockState state) { return true; }
    @Override public float getDestroySpeed(ItemStack stack, BlockState state) { return 99999.0F; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(nomeCustom).withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoMineracao");
        tooltip.add(Component.literal("Modo: " + NOMES_MODO[modo]).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
        tooltip.add(Component.literal("[Shift + Clique Direito] Trocar Área").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("• Quebra qualquer bloco (inclusive Bedrock)").withStyle(ChatFormatting.AQUA));
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

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            int raio = RAIOS[stack.getOrCreateTag().getInt("ModoMineracao")];
            if (raio > 0) {
                for (int x = -raio; x <= raio; x++) {
                    for (int y = -raio; y <= raio; y++) {
                        for (int z = -raio; z <= raio; z++) {
                            BlockPos alvo = pos.offset(x, y, z);
                            BlockState st = serverLevel.getBlockState(alvo);
                            if (!st.isAir()) {
                                serverLevel.destroyBlock(alvo, true, player);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}

