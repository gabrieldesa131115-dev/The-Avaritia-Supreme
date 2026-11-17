package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ItensAvaritiaExtra {

    public static class EscudoSupremo extends ShieldItem {
        public final boolean isIrrelevante;
        public EscudoSupremo(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(isIrrelevante ? "❖ Égide da Singularidade Irrelevante ❖" : "✦ Égide do Cosmos Imensurável ✦")
                    .withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• Defesa Absoluta (Reflete dano apagando o atacante)").withStyle(ChatFormatting.AQUA));
        }
    }

    public static class ComidaSuprema extends Item {
        private final String nomeCustom;
        private final boolean isAlmondega;
        public static final FoodProperties FOOD_SUPREMA = new FoodProperties.Builder()
                .nutrition(20).saturationMod(20.0F).alwaysEat().build();

        public ComidaSuprema(String nomeCustom, boolean isAlmondega) {
            super(new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.EPIC).food(FOOD_SUPREMA));
            this.nomeCustom = nomeCustom;
            this.isAlmondega = isAlmondega;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(nomeCustom).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        }
        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
            if (!level.isClientSide()) {
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 6000, 9));
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 9));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 4));
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, isAlmondega ? 19 : 9));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 3));
            }
            return stack;
        }
    }

    public static class MaterialSupremo extends Item {
        private final String nomeCustom;
        private final String descricao;
        public MaterialSupremo(String nomeCustom, String descricao) {
            super(new Item.Properties().stacksTo(64).fireResistant().rarity(Rarity.EPIC));
            this.nomeCustom = nomeCustom;
            this.descricao = descricao;
        }
        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public Component getName(ItemStack stack) {
            return Component.literal(nomeCustom).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
        }
        @Override public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal(descricao).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }
    }
}

