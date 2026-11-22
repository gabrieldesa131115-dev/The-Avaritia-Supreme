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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class ArcoBestaSupremaItem {
    private static final Random RAND = new Random();

    public static void invocarChuvaFlechas(ServerLevel sl, Vec3 centro, Player dono, boolean irr) {
        int qtd = irr ? 45 : 30;
        double raio = irr ? 9.0D : 6.5D;
        sl.sendParticles(ParticleTypes.END_ROD, centro.x, centro.y + 1.0, centro.z, 35, 2.5, 1.0, 2.5, 0.1);
        for (int i = 0; !(i >= qtd); i++) {
            double ang = RAND.nextDouble() * Math.PI * 2.0;
            double dist = RAND.nextDouble() * raio;
            double fx = centro.x + Math.cos(ang) * dist;
            double fz = centro.z + Math.sin(ang) * dist;
            double fy = centro.y + 18.0 + RAND.nextDouble() * 10.0;

            Arrow flecha = new Arrow(sl, fx, fy, fz);
            flecha.setOwner(dono);
            flecha.setBaseDamage(irr ? 999999999.0D : 500000.0D);
            flecha.setCritArrow(true);
            flecha.setSecondsOnFire(100);
            flecha.pickup = AbstractArrow.Pickup.DISALLOWED;
            flecha.getPersistentData().putBoolean("FlechaChuvaSuprema", true);
            flecha.getPersistentData().putBoolean("FlechaIrrelevante", irr);
            flecha.getPersistentData().putUUID("DonoFlechaSuprema", dono.getUUID());
            flecha.shoot(0.0D, -1.0D, 0.0D, 3.8F, 1.5F);
            sl.addFreshEntity(flecha);
        }
        for (Entity alvo : sl.getEntities(dono, dono.getBoundingBox().move(centro.subtract(dono.position())).inflate(raio))) {
            if (alvo != dono && !(alvo instanceof ItemEntity) && !(alvo instanceof AbstractArrow)) {
                ExampleMod.executarAlvo(alvo, dono, irr);
            }
        }
        sl.playSound(null, centro.x, centro.y, centro.z, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.5F, 0.6F);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (!arrow.getPersistentData().contains("DonoFlechaSuprema")) return;

        UUID donoId = arrow.getPersistentData().getUUID("DonoFlechaSuprema");
        Entity owner = arrow.getOwner();

        // Impede 100% que a flecha acerte o proprio dono
        if (event.getRayTraceResult() instanceof EntityHitResult ehr) {
            Entity atingido = ehr.getEntity();
            if (atingido.getUUID().equals(donoId) || (owner != null && atingido == owner)) {
                event.setCanceled(true);
                return;
            }
        }

        if (!(arrow.level() instanceof ServerLevel sl)) return;
        boolean irr = arrow.getPersistentData().getBoolean("FlechaIrrelevante");
        boolean jaEhChuva = arrow.getPersistentData().getBoolean("FlechaChuvaSuprema");

        if (event.getRayTraceResult() instanceof EntityHitResult ehr && owner instanceof Player p) {
            ExampleMod.executarAlvo(ehr.getEntity(), p, irr);
        }

        // Se for a flecha principal (acertando ou errando no chao), invoca a chuva estilo Re:Avaritia
        if (!jaEhChuva && owner instanceof Player p && event.getRayTraceResult().getType() != HitResult.Type.MISS) {
            invocarChuvaFlechas(sl, event.getRayTraceResult().getLocation(), p, irr);
        }

        if (jaEhChuva) {
            arrow.discard();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onArrowDamageOwner(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player p && event.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            if (arrow.getPersistentData().contains("DonoFlechaSuprema")) {
                UUID donoId = arrow.getPersistentData().getUUID("DonoFlechaSuprema");
                if (p.getUUID().equals(donoId)) {
                    event.setCanceled(true);
                    arrow.discard();
                }
            }
        }
    }
       public static class ArcoSupremo extends BowItem {
        public final boolean isIrrelevante;

        public ArcoSupremo(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }

        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public boolean isDamageable(ItemStack stack) { return false; }

        @Override
        public Component getName(ItemStack stack) {
            return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Arco da Chuva Irrelevante ❖" : "✦ Arco do Horizonte Cósmico ✦");
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• [Estilo Re:Avaritia] Acertar ou errar invoca Chuva de Flechas do Céu").withStyle(ChatFormatting.AQUA));
            tooltip.add(Component.literal("• [Imunidade Total] As flechas nunca atingem você").withStyle(ChatFormatting.GREEN));
        }

        @Override
        public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }

        @Override
        public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
            if (!(entity instanceof Player player)) return;
            if (!level.isClientSide() && level instanceof ServerLevel sl) {
                Arrow flecha = new Arrow(sl, player);
                flecha.setOwner(player);
                flecha.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 4.5F, 0.0F);
                flecha.setBaseDamage(isIrrelevante ? 999999999.0D : 999999.0D);
                flecha.setCritArrow(true);
                flecha.setSecondsOnFire(100);
                flecha.pickup = AbstractArrow.Pickup.DISALLOWED;
                flecha.getPersistentData().putBoolean("FlechaIrrelevante", isIrrelevante);
                flecha.getPersistentData().putUUID("DonoFlechaSuprema", player.getUUID());
                sl.addFreshEntity(flecha);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.2F, 1.2F);
                VisualEAdaptacao.adaptarItem(stack, player, "Chuva Celestial");
            }
        }
    }

    public static class BestaSuprema extends CrossbowItem {
        public final boolean isIrrelevante;

        public BestaSupremoHelper helper;

        public BestaSuprema(boolean isIrrelevante) {
            super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
            this.isIrrelevante = isIrrelevante;
        }

        @Override public boolean isFoil(ItemStack stack) { return true; }
        @Override public boolean isDamageable(ItemStack stack) { return false; }

        @Override
        public Component getName(ItemStack stack) {
            return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Besta do Colapso Irrelevante ❖" : "✦ Besta da Tempestade Cósmica ✦");
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List tooltip, TooltipFlag flag) {
            tooltip.add(Component.literal("• [Disparo Instantâneo] Rajada de 5 flechas com Chuva do Céu").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(Component.literal("• [Imunidade Total] Nunca acerta o atirador").withStyle(ChatFormatting.GREEN));
        }

        @Override
        public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide() && level instanceof ServerLevel sl) {
                float[] angulos = {-12.0F, -6.0F, 0.0F, 6.0F, 12.0F};
                for (int i = 0; !(i >= angulos.length); i++) {
                    Arrow flecha = new Arrow(sl, player);
                    flecha.setOwner(player);
                    flecha.shootFromRotation(player, player.getXRot(), player.getYRot() + angulos[i], 0.0F, 4.5F, 0.0F);
                    flecha.setBaseDamage(isIrrelevante ? 999999999.0D : 999999.0D);
                    flecha.setCritArrow(true);
                    flecha.setSecondsOnFire(100);
                    flecha.pickup = AbstractArrow.Pickup.DISALLOWED;
                    flecha.getPersistentData().putBoolean("FlechaIrrelevante", isIrrelevante);
                    flecha.getPersistentData().putUUID("DonoFlechaSuprema", player.getUUID());
                    sl.addFreshEntity(flecha);
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.3F, 1.0F);
                VisualEAdaptacao.adaptarItem(stack, player, "Rajada Estelar");
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
    }

    public static class BestaSupremoHelper {}
}
