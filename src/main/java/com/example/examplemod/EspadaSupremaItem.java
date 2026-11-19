package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class EspadaSupremaItem extends SwordItem {
    public final boolean isIrrelevante;

    public EspadaSupremaItem(boolean isIrrelevante) {
        super(FerramentaSupremaItem.TIER_APICE, isIrrelevante ? 2147483646 : 999999999, 100.0F,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(isIrrelevante ? "❖ Espada do Cosmos Irrelevante ❖" : "✦ Espada do Cosmos Imensurável ✦")
                .withStyle(isIrrelevante ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("• Apaga qualquer criatura, Leviatã ou NPC imortal").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• [Clique Direito] Expurgo Cósmico em área (30 blocos)").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide()) {
            ExampleMod.executarAlvo(entity, player, this.isIrrelevante);
            VisualEAdaptacao.adaptarItem(stack, player, "Aniquilação");
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player p) {
            ExampleMod.executarAlvo(target, p, this.isIrrelevante);
            VisualEAdaptacao.adaptarItem(stack, p, "Aniquilação");
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && level instanceof ServerLevel sl) {
            double raio = isIrrelevante ? 40.0D : 25.0D;
            AABB area = player.getBoundingBox().inflate(raio);
            int apagados = 0;
            for (Entity alvo : sl.getEntities(player, area)) {
                if (alvo != player && !(alvo instanceof net.minecraft.world.entity.item.ItemEntity)) {
                    ExampleMod.executarAlvo(alvo, player, this.isIrrelevante);
                    apagados++;
                }
            }
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.5F);
            sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 40, 2.0, 1.0, 2.0, 0.1);
            if (apagados > 0) {
                VisualEAdaptacao.adaptarItem(stack, player, "Expurgo em Área");
                player.displayClientMessage(Component.literal("⚡ " + apagados + " entidades apagadas da existência!").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
