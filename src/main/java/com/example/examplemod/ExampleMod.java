package com.example.examplemod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> ESPADA_IMENSURAVEL = ITEMS.register("espada_imensuravel", () -> new EspadaSupremaItem(false));
    public static final RegistryObject<Item> ESPADA_IRRELEVANTE = ITEMS.register("espada_irrelevante", () -> new EspadaSupremaItem(true));

    public static final RegistryObject<Item> CAPACETE_IMENSURAVEL = ITEMS.register("capacete_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.HELMET, false, "✦ Capacete Imensurável ✦"));
    public static final RegistryObject<Item> PEITORAL_IMENSURAVEL = ITEMS.register("peitoral_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.CHESTPLATE, false, "✦ Peitoral Imensurável ✦"));
    public static final RegistryObject<Item> CALCA_IMENSURAVEL = ITEMS.register("calca_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.LEGGINGS, false, "✦ Calças Imensuráveis ✦"));
    public static final RegistryObject<Item> BOTAS_IMENSURAVEL = ITEMS.register("botas_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.BOOTS, false, "✦ Botas Imensuráveis ✦"));

    public static final RegistryObject<Item> CAPACETE_IRRELEVANTE = ITEMS.register("capacete_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.HELMET, true, "❖ Capacete Irrelevante ❖"));
    public static final RegistryObject<Item> PEITORAL_IRRELEVANTE = ITEMS.register("peitoral_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.CHESTPLATE, true, "❖ Peitoral Irrelevante ❖"));
    public static final RegistryObject<Item> CALCA_IRRELEVANTE = ITEMS.register("calca_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.LEGGINGS, true, "❖ Calças Irrelevantes ❖"));
    public static final RegistryObject<Item> BOTAS_IRRELEVANTE = ITEMS.register("botas_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.BOOTS, true, "❖ Botas Irrelevantes ❖"));

    public ExampleMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        bus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            ITEMS.getEntries().forEach(item -> event.accept(item.get()));
        }
    }

    public static void executarAlvo(Entity target, Player attacker, boolean isIrrelevante) {
        if (target == attacker || !(attacker.level() instanceof ServerLevel serverLevel)) return;
        if (target instanceof ServerPlayer victim) {
            if (victim.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || victim.isCreative()) victim.setGameMode(GameType.SURVIVAL);
            victim.getInventory().dropAll();
            victim.getInventory().clearContent();
        }
        if (isIrrelevante) {
            target.setInvulnerable(false);
            target.setDeltaMovement(Vec3.ZERO);
            if (target instanceof Mob mob) mob.setNoAi(true);
            if (target instanceof LivingEntity living) {
                living.removeAllEffects();
                living.setHealth(0.0F);
                living.die(attacker.damageSources().fellOutOfWorld());
            }
            if (!(target instanceof Player)) { target.remove(Entity.RemovalReason.DISCARDED); target.discard(); }
            serverLevel.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.1);
        } else {
            if (target instanceof LivingEntity living) {
                living.setInvulnerable(false);
                living.hurt(attacker.damageSources().fellOutOfWorld(), Float.MAX_VALUE);
                living.setHealth(0.0F);
                living.die(attacker.damageSources().fellOutOfWorld());
            }
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0, 0, 0, 0);
        }
    }

    @SubscribeEvent
    public void onPlayerHurt(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player p && (p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmaduraSupremaItem || p.getMainHandItem().getItem() instanceof EspadaSupremaItem)) {
            event.setCanceled(true);
            p.setHealth(p.getMaxHealth());
        }
    }

    @SubscribeEvent
    public void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player p && p.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ArmaduraSupremaItem) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onAbsoluteStrike(AttackEntityEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().getMainHandItem().getItem() instanceof EspadaSupremaItem espada) {
            executarAlvo(event.getTarget(), event.getEntity(), espada.isIrrelevante);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player p = event.player;
        if (p.level().isClientSide() || !(p.level() instanceof ServerLevel sl)) return;
        if (p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmaduraSupremaItem cap) {
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false));
            for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(cap.isIrrelevante ? 150.0 : 60.0))) {
                if (e instanceof LivingEntity l) { l.setGlowingTag(true); if (l.isInvisible()) l.setInvisible(false); }
            }
        }
        if (p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmaduraSupremaItem peit) {
            p.getAbilities().mayfly = true;
            p.getAbilities().setFlyingSpeed(peit.isIrrelevante ? 0.25F : 0.12F);
            p.onUpdateAbilities();
        } else if (!p.isCreative() && !p.isSpectator() && p.getAbilities().mayfly) {
            p.getAbilities().mayfly = false; p.getAbilities().flying = false; p.getAbilities().setFlyingSpeed(0.05F); p.onUpdateAbilities();
        }
        if (p.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmaduraSupremaItem cal) {
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, cal.isIrrelevante ? 6 : 3, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, cal.isIrrelevante ? 4 : 2, false, false));
            p.getFoodData().setFoodLevel(20); p.getFoodData().setSaturation(20.0F);
        }
        if (p.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ArmaduraSupremaItem bot) {
            p.clearFire(); p.fallDistance = 0.0F;
            p.getActiveEffects().removeIf(eff -> !eff.getEffect().isBeneficial());
            if (bot.isIrrelevante && p.tickCount % 3 == 0) sl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY(), p.getZ(), 2, 0.3, 0.1, 0.3, 0.02);
        }
    }
                                                                            }
