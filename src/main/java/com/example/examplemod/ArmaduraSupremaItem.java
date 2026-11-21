package com.example.examplemod;
import net.minecraft.sounds.SoundEvent;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class ArmaduraSupremaItem extends ArmorItem {
    public final boolean isIrrelevante;
    private final String titulo;

    private static final DustParticleOptions AZUL_ESFERA = new DustParticleOptions(new Vector3f(0.1F, 0.75F, 1.0F), 1.8F);
    private static final DustParticleOptions ROXO_ESFERA = new DustParticleOptions(new Vector3f(0.8F, 0.1F, 1.0F), 1.8F);

    public static final ArmorMaterial MATERIAL_SUPREMO = new ArmorMaterial() {
        @Override public int getDurabilityForType(Type type) { return 0; }
        @Override public int getDefenseForType(Type type) { return 999999; }
        @Override public int getEnchantmentValue() { return 100; }
        @Override public SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
        @Override public String getName() { return "examplemod:armadura_suprema"; }
        @Override public float getToughness() { return 999999.0F; }
        @Override public float getKnockbackResistance() { return 100.0F; }
    };

    public ArmaduraSupremaItem(Type type, boolean isIrrelevante, String titulo) {
        super(MATERIAL_SUPREMO, type, new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
        this.titulo = titulo;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal((isIrrelevante ? "❖ " : "✦ ") + titulo + (isIrrelevante ? " ❖" : " ✦"))
                .withStyle(isIrrelevante ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.AQUA, ChatFormatting.BOLD);
    }

    public static String getNomeModo(int m) {
        return switch (m) {
            case 0 -> "Modo 1: Intangibilidade de Paredes & Velocidade";
            case 1 -> "Modo 2: Espectador Visível (Subsolo & Voo)";
            case 2 -> "Modo 3: Campo de Repulsão Cósmica";
            case 3 -> "Modo 4: Aura de Desintegração";
            default -> "Modo 5: Teleporte Cósmico (Esfera Azul/Roxa)";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoArmadura");
        tooltip.add(Component.literal(getNomeModo(modo)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("• Requer 3+ peças do Universo (ou 1 Irrelevante) para todos os modos").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("[Tecla B] Invisibilidade | [Shift + Clique Direito na Calça] Trocar Modos").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("[Clique Direito na Calça] Teleporte Esférico Azul/Roxo").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.type == Type.LEGGINGS && !level.isClientSide() && level instanceof ServerLevel sl) {
            int nivel = getNivelDefesa(player);
            if (player.isShiftKeyDown()) {
                if (nivel < 3) {
                    player.displayClientMessage(Component.literal("⚠ Equipe pelo menos 3 peças do Universo (ou 1 Irrelevante) para liberar os 5 modos!").withStyle(ChatFormatting.RED), true);
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }
                int prox = (getModoPlayer(player) + 1) % 5;
                setModoPlayer(player, prox);
                stack.getOrCreateTag().putInt("ModoArmadura", prox);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("❖ " + getNomeModo(prox) + " ❖"), true);
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            } else {
                // A calça sempre permite o Teleporte Cósmico com esfera azul e roxa
                gerarEsferaAzulRoxa(sl, player.position().add(0, 1.0, 0));
                Vec3 olho = player.getEyePosition();
                Vec3 destino = olho.add(player.getLookAngle().scale(40.0D));
                BlockHitResult hit = level.clip(new ClipContext(olho, destino, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                Vec3 posFinal = hit.getLocation();
                player.teleportTo(posFinal.x, posFinal.y + 0.5, posFinal.z);
                player.fallDistance = 0.0F;
                gerarEsferaAzulRoxa(sl, posFinal.add(0, 1.0, 0));
                sl.playSound(null, posFinal.x, posFinal.y, posFinal.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5F, 1.3F);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
        private static void gerarEsferaAzulRoxa(ServerLevel sl, Vec3 centro) {
        for (int lat = 0; lat <= 180; lat += 20) {
            double radLat = Math.toRadians(lat);
            double r = Math.sin(radLat) * 2.2D;
            double y = Math.cos(radLat) * 2.2D;
            for (int lon = 0; lon < 360; lon += 20) {
                double radLon = Math.toRadians(lon);
                DustParticleOptions cor = ((lat + lon) % 40 == 0) ? AZUL_ESFERA : ROXO_ESFERA;
                sl.sendParticles(cor, centro.x + Math.cos(radLon) * r, centro.y + y, centro.z + Math.sin(radLon) * r, 1, 0, 0, 0, 0);
            }
        }
    }

    public static int getNivelDefesa(Player player) {
        int pecasUni = 0;
        for (ItemStack s : player.getArmorSlots()) {
            Item item = s.getItem();
            if (item instanceof ArmaduraSupremaItem arm) {
                if (arm.isIrrelevante) return 3; // 1 peça Irrelevante = 100% Irrelevante + Todos os Modos
                pecasUni++;
            } else if (item instanceof ArmasEspeciaisItem.ElytraSuprema el) {
                if (el.isIrrelevante) return 3; // Elytra Irrelevante = 100% Irrelevante + Todos os Modos
                pecasUni++; // Elytra do Universo conta como peça para liberar os modos!
            }
        }
        if (pecasUni >= 3) return 3; // 3 ou 4 peças = 100% Imensurável + Todos os Modos
        if (pecasUni == 2) return 2; // 2 peças = 75% da Imensurável
        if (pecasUni == 1) return 1; // 1 peça = 75% do Re:Avaritia
        return 0;
    }

    public static int getModoPlayer(Player p) { return p.getPersistentData().getInt("ModoArmaduraGlobal"); }
    public static void setModoPlayer(Player p, int m) { p.getPersistentData().putInt("ModoArmaduraGlobal", m); }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player p = event.player;
        int nivel = getNivelDefesa(p);

        // Passivo do Capacete: Visão Noturna + Respiração + Fome Cheia
        if (p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmaduraSupremaItem) {
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
            p.setAirSupply(p.getMaxAirSupply());
            p.getFoodData().setFoodLevel(20);
            p.getFoodData().setSaturation(20.0F);
        }

        if (nivel < 3) {
            if (!p.isSpectator()) p.noPhysics = false;
            return;
        }

        // Imortalidade ao Void e Cura Constante (3+ peças do Universo ou 1 Irrelevante)
        p.setHealth(p.getMaxHealth());
        if (p.getY() < p.level().getMinBuildHeight() - 10) {
            p.setDeltaMovement(p.getDeltaMovement().x, 1.2D, p.getDeltaMovement().z);
            p.fallDistance = 0.0F;
        }

        // Invisibilidade pela Tecla B
        if (p.getPersistentData().getBoolean("InvisibilidadeCosmica")) {
            p.setInvisible(true);
            p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
        }

        int modo = getModoPlayer(p);
        if (modo == 0) {
            // Modo 1: Intangibilidade só de paredes + Super Velocidade
            p.noPhysics = false;
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 4, false, false));
            Vec3 look = p.getLookAngle();
            BlockPos frente = BlockPos.containing(p.getX() + look.x * 0.8, p.getY() + 0.5, p.getZ() + look.z * 0.8);
            if (p.horizontalCollision && !p.level().getBlockState(frente).isAir()) {
                p.setPos(p.getX() + look.x * 0.6, p.getY(), p.getZ() + look.z * 0.6);
            }
        } else if (modo == 1) {
            // Modo 2: Espectador Visível (atravessa chão e paredes; ao parar de agachar vai para baixo da terra e voa)
            p.noPhysics = true;
            if (!p.getAbilities().mayfly) {
                p.getAbilities().mayfly = true;
                p.onUpdateAbilities();
            }
            if (!p.isShiftKeyDown() && !p.level().getBlockState(p.blockPosition()).isAir()) {
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
            }
        } else {
            if (!p.isSpectator()) p.noPhysics = false;
            if (!p.level().isClientSide() && p.level() instanceof ServerLevel sl) {
                if (modo == 2) {
                    // Modo 3: Campo de Repulsão Cósmica
                    for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(8.0D))) {
                        if (e instanceof Enemy) {
                            Vec3 empurrao = e.position().subtract(p.position()).normalize().scale(2.5D);
                            e.setDeltaMovement(empurrao.x, 0.8D, empurrao.z);
                        }
                    }
                } else if (modo == 3) {
                    // Modo 4: Aura de Desintegração
                    for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(5.0D))) {
                        if (e instanceof Enemy) ExampleMod.executarAlvo(e, p, true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player p) {
            int nivel = getNivelDefesa(p);
            boolean usandoElytraUni = p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmasEspeciaisItem.ElytraSuprema el && !el.isIrrelevante;
            if (nivel >= 3) {
                event.setCanceled(true);
                event.setAmount(0.0F);
            } else if (nivel == 2) {
                // 2 peças: 75% da defesa Imensurável (Elytra protege um pouco menos que o peitoral)
                event.setAmount(event.getAmount() * (usandoElytraUni ? 0.12F : 0.05F));
            } else if (nivel == 1) {
                // 1 peça: 75% da defesa do Re:Avaritia (Elytra sozinha protege menos que o peitoral)
                event.setAmount(event.getAmount() * (usandoElytraUni ? 0.45F : 0.25F));
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player p && getNivelDefesa(p) >= 3) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player p && getNivelDefesa(p) >= 3) {
            event.setCanceled(true);
            p.setHealth(p.getMaxHealth());
        }
    }

    // ================= TECLA B PARA INVISIBILIDADE CÓSMICA =================
    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class KeyInit {
        public static final KeyMapping TECLA_INVIS = new KeyMapping("key.examplemod.invis", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.misc");
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) { event.register(TECLA_INVIS); }
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class KeyHandler {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (KeyInit.TECLA_INVIS.consumeClick()) {
                net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                if (mc.player != null && getNivelDefesa(mc.player) >= 3) {
                    boolean novo = !mc.player.getPersistentData().getBoolean("InvisibilidadeCosmica");
                    mc.player.getPersistentData().putBoolean("InvisibilidadeCosmica", novo);
                    mc.player.setInvisible(novo);
                    mc.player.displayClientMessage(VisualEAdaptacao.textoBrilhante(novo ? "❖ INVISIBILIDADE CÓSMICA: ATIVADA ❖" : "❖ INVISIBILIDADE CÓSMICA: DESATIVADA ❖"), true);
                }
            }
        }
    }
                }
