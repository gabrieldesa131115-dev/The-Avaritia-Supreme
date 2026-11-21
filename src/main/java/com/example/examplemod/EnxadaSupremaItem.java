package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class EnxadaSupremaItem extends HoeItem {
    public final boolean isIrrelevante;

    public EnxadaSupremaItem(boolean isIrrelevante) {
        super(FerramentaSupremaItem.TIER_APICE, 25000000, 100.0F,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Enxada Multiversal ❖" : "✦ Enxada do Universo ✦");
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player p) {
            float dano = this.isIrrelevante ? 50000000000000.0F : 25000000.0F;
            target.hurt(p.damageSources().playerAttack(p), dano);
            VisualEAdaptacao.adaptarItem(stack, p, "Ceifa Cósmica");
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("• Ara, umidifica e planta sementes instantaneamente").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("[Clique Direito] Mutação Cósmica (Gera Frutos Dourados e Cósmicos)").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (!level.isClientSide() && level instanceof ServerLevel sl && player != null) {
            BlockPos centro = ctx.getClickedPos();
            int r = isIrrelevante ? 12 : 7;
            Block[] culturas = {Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS};
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos p = centro.offset(x, 0, z);
                    BlockState st = sl.getBlockState(p);
                    if (st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.DIRT) || st.is(Blocks.COARSE_DIRT) || st.is(Blocks.FARMLAND)) {
                        sl.setBlock(p, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7), 3);
                        if (sl.getBlockState(p.above()).isAir()) {
                            Block escolhida = culturas[sl.random.nextInt(culturas.length)];
                            sl.setBlock(p.above(), escolhida.defaultBlockState().setValue(CropBlock.AGE, ((CropBlock) escolhida).getMaxAge()), 3);
                        }
                    } else if (st.getBlock() instanceof CropBlock crop) {
                        sl.setBlock(p, crop.getStateForAge(crop.getMaxAge()), 3);
                    }
                }
            }
            sl.playSound(null, centro, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.2F, 1.3F);
            sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, centro.getX(), centro.getY() + 1.0, centro.getZ(), 35, r * 0.5, 0.5, r * 0.5, 0.05);
            VisualEAdaptacao.adaptarItem(ctx.getItemInHand(), player, "Gênese Agrícola");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            BlockPos c = player.blockPosition();
            int r = isIrrelevante ? 20 : 12;
            int mutacoes = 0;
            for (int x = -r; x <= r; x++) {
                for (int y = -3; y <= 5; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos p = c.offset(x, y, z);
                        BlockState st = sl.getBlockState(p);
                        if (st.getBlock() instanceof CropBlock crop) {
                            sl.setBlock(p, crop.getStateForAge(crop.getMaxAge()), 3);
                            if (sl.random.nextFloat() < (isIrrelevante ? 0.35F : 0.15F)) {
                                Item dropMutante = switch (sl.random.nextInt(4)) {
                                    case 0 -> Items.ENCHANTED_GOLDEN_APPLE;
                                    case 1 -> Items.GOLDEN_CARROT;
                                    case 2 -> ExampleMod.ALMONDEGA_COSMICA.get();
                                    default -> ExampleMod.ENSOPADO_SUPREMO.get();
                                };
                                sl.addFreshEntity(new ItemEntity(sl, p.getX() + 0.5, p.getY() + 0.8, p.getZ() + 0.5, new ItemStack(dropMutante)));
                                sl.sendParticles(ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 0.7, p.getZ() + 0.5, 4, 0.2, 0.3, 0.2, 0.02);
                                mutacoes++;
                            }
                        }
                    }
                }
            }
            sl.playSound(null, c, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.4F);
            if (mutacoes > 0) {
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("✦ MUTAÇÃO CÓSMICA! " + mutacoes + " frutos raros gerados! ✦"), true);
                VisualEAdaptacao.adaptarItem(stack, player, "Mutação Cósmica");
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    }
