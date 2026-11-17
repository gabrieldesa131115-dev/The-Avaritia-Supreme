package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ArmaduraSupremaItem extends ArmorItem {
    public final boolean isIrrelevante;
    private final String nomeCustom;

    public static final ArmorMaterial MATERIAL_SUPREMO = new ArmorMaterial() {
        @Override public int getDurabilityForType(Type type) { return 0; }
        @Override public int getDefenseForType(Type type) { return 9999; }
        @Override public int getEnchantmentValue() { return 100; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
        @Override public String getName() { return ExampleMod.MODID + ":supremo"; }
        @Override public float getToughness() { return 9999.0F; }
        @Override public float getKnockbackResistance() { return 10.0F; }
    };

    public ArmaduraSupremaItem(Type type, boolean isIrrelevante, String nomeCustom) {
        super(MATERIAL_SUPREMO, type, new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
        this.nomeCustom = nomeCustom;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(nomeCustom).withStyle(isIrrelevante ? ChatFormatting.RED : ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (this.type == Type.HELMET) {
            tooltip.add(Component.literal("• Revela invisíveis e espectadores").withStyle(ChatFormatting.AQUA));
        } else if (this.type == Type.CHESTPLATE) {
            tooltip.add(Component.literal("• Voo Livre & Imortalidade Total").withStyle(ChatFormatting.AQUA));
        } else if (this.type == Type.LEGGINGS) {
            tooltip.add(Component.literal("• Velocidade Extrema, Salto & Fome Infinita").withStyle(ChatFormatting.AQUA));
        } else if (this.type == Type.BOOTS) {
            tooltip.add(Component.literal("• Purificação Total & Imunidade a Queda/Knockback").withStyle(ChatFormatting.AQUA));
        }
    }
}

