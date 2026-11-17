package com.example.examplemod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";

    public ExampleMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // 1. IMORTALIDADE TOTAL: Bloqueia qualquer dano (inclusive /kill e explosões)
    @SubscribeEvent
    public void onPlayerHurt(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.getMainHandItem().is(Items.NETHERITE_SWORD)) {
                event.setCanceled(true);
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    // 2. APAGAR DA EXISTÊNCIA: Bateu em qualquer chefão ou titã, deleta da memória na hora
    @SubscribeEvent
    public void onAbsoluteStrike(AttackEntityEvent event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (!player.level().isClientSide() && player.getMainHandItem().is(Items.NETHERITE_SWORD)) {
            target.discard();
        }
    }

    // 3. VOO CRIATIVO REAL: Permite voar livremente no Sobrevivência segurando a espada
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (!player.level().isClientSide()) {
            boolean holdingSword = player.getMainHandItem().is(Items.NETHERITE_SWORD);
            if (holdingSword && !player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.onUpdateAbilities();
            } else if (!holdingSword && !player.isCreative() && !player.isSpectator() && player.getAbilities().mayfly) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }
    }
}
