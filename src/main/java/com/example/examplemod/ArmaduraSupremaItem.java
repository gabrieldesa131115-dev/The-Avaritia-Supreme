package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class ArmaduraSupremaItem extends ArmorItem {
    public final boolean isIrrelevante;
    private final String nomeExibicao;
    private static final Random RAND = new Random();

    public static final ArmorMaterial MATERIAL_SUPREMO = new ArmorMaterial() {
        @Override public int getDurabilityForType(Type type) { return 0; }
        @Override public int getDefenseForType(Type type) { return 999999; }
        @Override public int getEnchantmentValue() { return 100; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
        @Override public String getName() { return "examplemod:suprema"; }
        @Override public float getToughness() { return 999999.0F; }
        @Override public float getKnockbackResistance() { return 10.0F; }
    };

    public ArmaduraSupremaItem(Type type, boolean isIrrelevante, String nomeExibicao) {
        super(MATERIAL_SUPREMO, type, new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
        this.nomeExibicao = nomeExibicao;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }
    @Override public boolean isDamageable(ItemStack stack) { return false; }

    @Override
    public Component getName(ItemStack stack) {
        return VisualEAdaptacao.textoBrilhante((isIrrelevante ? "❖ " : "✦ ") + nomeExibicao + (isIrrelevante ? " ❖" : " ✦"));
    }

    public static String getNomeModo(int m) {
        return switch (m) {
            case 0 -> "Modo 1: Intangibilidade Livre + Visao Raio-X";
            case 1 -> "Modo 2: Atravessar Paredes com Chao Solido";
            case 2 -> "Modo 3: Escudo Repulsor (Sem Dano)";
            case 3 -> "Modo 4: Efeito Blip / Esquiva Cosmica";
            case 4 -> "Modo 5: Teleporte Dimensional da Calca";
            default -> "Modo 6: Invisibilidade Absoluta";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoArmadura");
        tooltip.add(Component.literal("Modo Atual: " + getNomeModo(modo)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("• [Shift + Clique Direito] Trocar Modo (1 a 6)").withStyle(ChatFormatting.YELLOW));
        if (this.type == Type.CHESTPLATE) {
            tooltip.add(Component.literal("• [Peitoral] Libera Voo Criativo Livre").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        }
        if (this.type == Type.LEGGINGS) {
            tooltip.add(Component.literal("• [Calca - Modo 5] Clique Direito para Teleportar").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int modo = stack.getOrCreateTag().getInt("ModoArmadura");

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                int prox = (modo + 1) % 6;
                sincronizarModo(player, prox);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        // Modo 5: Teleporte da Calca ao clicar com botao direito no ar
        if (modo == 4 && this.type == Type.LEGGINGS) {
            if (!level.isClientSide() && level instanceof ServerLevel sl) {
                double dist = isIrrelevante ? 60.0D : 30.0D;
                Vec3 destino = player.getEyePosition().add(player.getLookAngle().normalize().scale(dist));
                sl.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.5, 1.0, 0.5, 0.2);
                player.teleportTo(destino.x, destino.y, destino.z);
                player.fallDistance = 0.0F;
                sl.sendParticles(ParticleTypes.REVERSE_PORTAL, destino.x, destino.y + 1.0, destino.z, 40, 0.5, 1.0, 0.5, 0.2);
                sl.playSound(null, destino.x, destino.y, destino.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 1.2F);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("⚡ Teleporte da Calça (" + (int) dist + "m)!"), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return super.use(level, player, hand);
    }
        public static void sincronizarModo(Player player, int novoModo) {
        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.getItem() instanceof ArmaduraSupremaItem) {
                armor.getOrCreateTag().putInt("ModoArmadura", novoModo);
            }
        }
        ItemStack mao = player.getMainHandItem();
        if (mao.getItem() instanceof ArmaduraSupremaItem) {
            mao.getOrCreateTag().putInt("ModoArmadura", novoModo);
        }
        player.displayClientMessage(VisualEAdaptacao.textoBrilhante("🛡 Armadura: " + getNomeModo(novoModo)), true);
    }

    public static void alternarModoPelaTecla(Player player) {
        int atual = getModoAtual(player);
        sincronizarModo(player, (atual + 1) % 6);
    }

    public static int getModoAtual(Player p) {
        for (ItemStack armor : p.getArmorSlots()) {
            if (armor.getItem() instanceof ArmaduraSupremaItem) {
                return armor.getOrCreateTag().getInt("ModoArmadura");
            }
        }
        return -1;
    }

    private static void executarEfeitoBlip(Player p) {
        if (!(p.level() instanceof ServerLevel sl)) return;
        sl.sendParticles(ParticleTypes.ASH, p.getX(), p.getY() + 1.0, p.getZ(), 70, 0.5, 1.0, 0.5, 0.08);
        sl.sendParticles(ParticleTypes.SMOKE, p.getX(), p.getY() + 1.0, p.getZ(), 35, 0.4, 0.9, 0.4, 0.05);
        sl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.4, 0.9, 0.4, 0.1);
        double dx = (RAND.nextDouble() - 0.5) * 14.0;
        double dz = (RAND.nextDouble() - 0.5) * 14.0;
        double nx = p.getX() + dx;
        double nz = p.getZ() + dz;
        double ny = p.getY();
        p.teleportTo(nx, ny, nz);
        p.fallDistance = 0.0F;
        sl.sendParticles(ParticleTypes.ASH, nx, ny + 1.0, nz, 70, 0.5, 1.0, 0.5, 0.08);
        sl.sendParticles(ParticleTypes.PORTAL, nx, ny + 1.0, nz, 40, 0.5, 1.0, 0.5, 0.15);
        sl.playSound(null, nx, ny, nz, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.2F, 0.7F);
        p.displayClientMessage(VisualEAdaptacao.textoBrilhante("✨ Blip Cósmico ativado!"), true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player p = event.player;

        // Voo exclusivo do Peitoral Supremo
        boolean temPeitoral = p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmaduraSupremaItem;
        if (temPeitoral) {
            if (!p.getAbilities().mayfly) {
                p.getAbilities().mayfly = true;
                if (!p.level().isClientSide()) p.onUpdateAbilities();
            }
        } else if (!p.isCreative() && !p.isSpectator() && p.getAbilities().mayfly) {
            p.getAbilities().mayfly = false;
            p.getAbilities().flying = false;
            if (!p.level().isClientSide()) p.onUpdateAbilities();
        }

        int modo = getModoAtual(p);
        if (modo == -1) {
            if (p.noPhysics && !p.isSpectator()) p.noPhysics = false;
            return;
        }

        // Modo 1 (0): Atravessar blocos livremente + visao de espectador
        if (modo == 0) {
            p.noPhysics = true;
            p.setOnGround(false);
            if (temPeitoral) p.getAbilities().flying = true;
        } else if (modo == 1) {
            // Modo 2 (1): Atravessa paredes e teto, mas mantem chao solido (e nao cai agachado!)
            BlockPos abaixo = BlockPos.containing(p.getX(), p.getY() - 0.15, p.getZ());
            boolean temChao = !p.level().getBlockState(abaixo).isAir();
            if (temChao || p.isShiftKeyDown()) {
                p.noPhysics = false;
                if (p.isShiftKeyDown() && p.getDeltaMovement().y < 0) {
                    Vec3 v = p.getDeltaMovement();
                    p.setDeltaMovement(v.x, 0.0, v.z);
                    p.fallDistance = 0.0F;
                }
            } else {
                p.noPhysics = true;
            }
        } else {
            if (p.noPhysics && !p.isSpectator()) p.noPhysics = false;
        }

        if (p.level().isClientSide()) return;

        p.setHealth(p.getMaxHealth());
        p.clearFire();
        p.setAirSupply(p.getMaxAirSupply());
        p.getFoodData().setFoodLevel(20);
        p.getFoodData().setSaturation(20.0F);
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 3, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));
        if (p.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()) != null) {
            p.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).setBaseValue(1.5D);
        }

        // Modo 6 (5): Invisibilidade Real
        if (modo == 5) {
            p.setInvisible(true);
            p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
        } else if (!p.hasEffect(MobEffects.INVISIBILITY)) {
            p.setInvisible(false);
        }

        // Modo 3 (2): Escudo que lanca pra longe todo mundo sem causar dano
        if (modo == 2 && p.level() instanceof ServerLevel sl) {
            for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(7.0D))) {
                if (e != p && !(e instanceof ItemEntity)) {
                    Vec3 empurrao = e.position().subtract(p.position());
                    if (empurrao.lengthSqr() < 0.01) empurrao = new Vec3(1, 0, 0);
                    empurrao = empurrao.normalize().scale(3.5D);
                    e.setDeltaMovement(empurrao.x, 0.9D, empurrao.z);
                    e.hurtMarked = true;
                }
            }
        }

        // Modo 4 (3): Animacao de Blip quando chegam perto
        if (modo == 3 && p.level() instanceof ServerLevel sl && p.tickCount % 15 == 0) {
            for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(3.5D))) {
                if (e != p && !(e instanceof ItemEntity) && e instanceof net.minecraft.world.entity.LivingEntity) {
                    executarEfeitoBlip(p);
                    break;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player p) {
            int modo = getModoAtual(p);
            if (modo != -1) {
                event.setCanceled(true);
                if (modo == 3) {
                    executarEfeitoBlip(p);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class ClientRenderArmadura {
        @SubscribeEvent
        public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
            if (getModoAtual(event.getEntity()) == 5) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderBlockOverlay(RenderBlockScreenEffectEvent event) {
            int modo = getModoAtual(event.getPlayer());
            if (modo == 0 || modo == 1) {
                event.setCanceled(true);
            }
        }
    }
}
