package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

public class EnxadaSupremaItem extends HoeItem {

    public EnxadaSupremaItem() {
        super(FerramentaSupremaItem.TIER_APICE, 100, 0.0F,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("✦ Enxada da Gênese Imensurável ✦").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("• Ara uma área 9x9 instantaneamente").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• Faz qualquer planta crescer na hora com Mutação Colorida").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("• Colheitas mutadas dão Vida, Fome e Poderes Extremos").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos centro = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            boolean arouAlgo = false;
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = centro.offset(x, 0, z);
                    BlockState st = sl.getBlockState(pos);
                    if (st.is(Blocks.GRASS_BLOCK) || st.is(Blocks.DIRT) || st.is(Blocks.DIRT_PATH) || st.is(Blocks.COARSE_DIRT)) {
                        sl.setBlock(pos, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7), 3);
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 2, 0.2, 0.1, 0.2, 0.05);
                        arouAlgo = true;
                    } else if (st.getBlock() instanceof CropBlock crop) {
                        colherComMutacao(sl, pos, st, crop, player);
                        arouAlgo = true;
                    }
                }
            }
            if (arouAlgo) {
                sl.playSound(null, centro, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.4F);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void colherComMutacao(ServerLevel sl, BlockPos pos, BlockState st, CropBlock crop, @Nullable Player p) {
        List<ItemStack> drops = Block.getDrops(crop.getStateForAge(crop.getMaxAge()), sl, pos, null);
        for (ItemStack drop : drops) {
            drop.setCount(drop.getCount() * 4);
            drop.getOrCreateTag().putBoolean("MutacaoCosmica", true);
            String nomeOriginal = drop.getHoverName().getString();
            drop.setHoverName(VisualEAdaptacao.textoBrilhante("✦ " + nomeOriginal + " (Mutação Cósmica) ✦"));
            sl.addFreshEntity(new ItemEntity(sl, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, drop));
        }
        sl.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 15, 0.3, 0.3, 0.3, 0.05);
        sl.removeBlock(pos, false);
    }

    // Crescimento instantâneo + mutação ao plantar ou quebrar perto da enxada
    @SubscribeEvent
    public void onPlantar(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player p && !p.level().isClientSide() && p.level() instanceof ServerLevel sl) {
            boolean temEnxada = p.getMainHandItem().getItem() instanceof EnxadaSupremaItem
                    || p.getOffhandItem().getItem() instanceof EnxadaSupremaItem
                    || p.getInventory().contains(new ItemStack(ExampleMod.ENXADA_IMENSURAVEL.get()));
            if (temEnxada && event.getPlacedBlock().getBlock() instanceof CropBlock crop) {
                colherComMutacao(sl, event.getPos(), event.getPlacedBlock(), crop, p);
            }
        }
    }

    // Descrição brilhante na comida mutada
    @SubscribeEvent
    public void onTooltipMutada(ItemTooltipEvent event) {
        if (event.getItemStack().hasTag() && event.getItemStack().getTag().getBoolean("MutacaoCosmica")) {
            event.getToolTip().add(VisualEAdaptacao.textoBrilhante("⚡ MUTAÇÃO CÓSMICA ATIVA ⚡"));
            event.getToolTip().add(Component.literal("• Restaura 100% da Fome, Vida Máxima e dá Poderes Supremos").withStyle(ChatFormatting.AQUA));
            event.getToolTip().add(Component.literal("• Compatível com todas as receitas normais!").withStyle(ChatFormatting.GRAY));
        }
    }

    // Efeitos supremos ao comer a comida mutada
    @SubscribeEvent
    public void onComerMutada(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        if (stack.hasTag() && stack.getTag().getBoolean("MutacaoCosmica") && event.getEntity() instanceof LivingEntity living) {
            if (living instanceof Player p) {
                p.getFoodData().setFoodLevel(20);
                p.getFoodData().setSaturation(20.0F);
            }
            living.setHealth(living.getMaxHealth());
            living.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 9, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2400, 4, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 4, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 3, false, false));
            living.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 3, false, false));
        }
    }
    }

