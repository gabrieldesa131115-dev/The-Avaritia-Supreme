package com.example.examplemod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class ArmaduraSupremaItem extends ArmorItem {
    public final boolean isIrrelevante;
    private final String nomeExibicao;

    public static boolean invisibilidadeAtiva = false;
    public static boolean verParedesAtivo = false;
    public static boolean verChaoAtivo = false;

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
            case 0 -> "Modo 1: Atravessar Paredes (Peitoral)";
            case 1 -> "Modo 2: Intangivel Total + Chao (Peitoral)";
            case 2 -> "Modo 3: Escudo Cosmico Azul Repulsor (Capacete)";
            case 3 -> "Modo 4: Escudo Cosmico Roxo Evaporador (Capacete)";
            default -> "Modo 5: Teleporte Dimensional (Calca)";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoArmadura");
        tooltip.add(Component.literal("Modo Atual: " + getNomeModo(modo)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("• [Shift + Clique Direito] Trocar Modo (1 a 5)").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("• [Tecla B] Invisibilidade | [Y] Ver Paredes | [K] Ver Chao").withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    @Override
    public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int modo = stack.getOrCreateTag().getInt("ModoArmadura");

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                int prox = (modo + 1) % 5;
                sincronizarModo(player, prox);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (modo == 4 && (this.type == Type.LEGGINGS || temCalca(player))) {
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
        public static boolean temAlgumaPeca(Player p) {
        for (ItemStack s : p.getArmorSlots()) {
            if (s.getItem() instanceof ArmaduraSupremaItem) return true;
        }
        return false;
    }

    public static boolean temCapacete(Player p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmaduraSupremaItem;
    }

    public static boolean temPeitoral(Player p) {
        return p.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmaduraSupremaItem;
    }

    public static boolean temCalca(Player p) {
        return p.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmaduraSupremaItem;
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
        invisibilidadeAtiva = !invisibilidadeAtiva;
        player.displayClientMessage(VisualEAdaptacao.textoBrilhante(invisibilidadeAtiva ? "👻 Invisibilidade Absoluta: LIGADA" : "👁 Invisibilidade Absoluta: DESLIGADA"), true);
    }

    public static int getModoAtual(Player p) {
        for (ItemStack armor : p.getArmorSlots()) {
            if (armor.getItem() instanceof ArmaduraSupremaItem) {
                return armor.getOrCreateTag().getInt("ModoArmadura");
            }
        }
        return -1;
    }

    private static void evaporarAlvo(ServerLevel sl, Entity alvo, Player dono) {
        if (alvo == null || alvo == dono || alvo instanceof ItemEntity) return;
        sl.sendParticles(ParticleTypes.ASH, alvo.getX(), alvo.getY() + 1.0, alvo.getZ(), 65, 0.5, 1.0, 0.5, 0.08);
        sl.sendParticles(ParticleTypes.SMOKE, alvo.getX(), alvo.getY() + 1.0, alvo.getZ(), 35, 0.4, 0.9, 0.4, 0.05);
        sl.sendParticles(ParticleTypes.PORTAL, alvo.getX(), alvo.getY() + 1.0, alvo.getZ(), 30, 0.4, 0.9, 0.4, 0.15);
        sl.playSound(null, alvo.getX(), alvo.getY(), alvo.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 0.6F);
        ExampleMod.executarAlvo(alvo, dono, true);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        int modo = getModoAtual(p);
        boolean peito = temPeitoral(p);

        if (peito && (modo == 0 || modo == 1)) {
            if (modo == 0) {
                BlockPos abaixo = BlockPos.containing(p.getX(), p.getY() - 0.15, p.getZ());
                boolean temChao = !p.level().getBlockState(abaixo).isAir();
                p.noPhysics = !temChao && !p.isShiftKeyDown();
            } else {
                p.noPhysics = true;
                if (!p.isShiftKeyDown() && p.getAbilities().mayfly && !p.onGround()) {
                    p.getAbilities().flying = true;
                }
            }
        } else if (p.noPhysics && !p.isSpectator()) {
            p.noPhysics = false;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player p = event.player;

        boolean peito = temPeitoral(p);
        if (peito) {
            if (!p.getAbilities().mayfly) {
                p.getAbilities().mayfly = true;
                if (!p.level().isClientSide()) p.onUpdateAbilities();
            }
        } else if (!p.isCreative() && !p.isSpectator() && p.getAbilities().mayfly) {
            p.getAbilities().mayfly = false;
            p.getAbilities().flying = false;
            if (!p.level().isClientSide()) p.onUpdateAbilities();
        }

        if (!temAlgumaPeca(p)) {
            invisibilidadeAtiva = false;
            return;
        }

        if (p.level().isClientSide()) return;

        p.setHealth(p.getMaxHealth());
        p.clearFire();
        p.setAirSupply(p.getMaxAirSupply());
        p.getFoodData().setFoodLevel(20);
        p.getFoodData().setSaturation(20.0F);
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 3, false, false));

        if (temCapacete(p)) {
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false));
        }

        if (p.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()) != null) {
            p.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get()).setBaseValue(1.5D);
        }

        if (invisibilidadeAtiva) {
            p.setInvisible(true);
            p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 40, 0, false, false));
        } else if (!p.hasEffect(MobEffects.INVISIBILITY)) {
            p.setInvisible(false);
        }

        int modo = getModoAtual(p);
        if (temCapacete(p) && p.level() instanceof ServerLevel sl) {
            if (modo == 2) {
                for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(4.5D))) {
                    if (e != p && !(e instanceof ItemEntity)) {
                        Vec3 emp = e.position().subtract(p.position());
                        if (!(emp.lengthSqr() >= 0.01)) emp = new Vec3(1, 0, 0);
                        emp = emp.normalize().scale(3.2D);
                        e.setDeltaMovement(emp.x, 0.8D, emp.z);
                        e.hurtMarked = true;
                    }
                }
            } else if (modo == 3) {
                for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(4.0D))) {
                    if (e != p && !(e instanceof ItemEntity) && e instanceof LivingEntity) {
                        evaporarAlvo(sl, e, p);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player p && temAlgumaPeca(p)) {
            event.setCanceled(true);
            int modo = getModoAtual(p);
            Entity agressor = event.getSource().getEntity();
            if (agressor != null && temCapacete(p) && p.level() instanceof ServerLevel sl) {
                if (modo == 2) {
                    Vec3 emp = agressor.position().subtract(p.position()).normalize().scale(3.5D);
                    agressor.setDeltaMovement(emp.x, 0.9D, emp.z);
                    agressor.hurtMarked = true;
                } else if (modo == 3) {
                    evaporarAlvo(sl, agressor, p);
                }
            }
        }
    }
        @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class ClientArmaduraEVisao {
        public static final KeyMapping KEY_INVIS = new KeyMapping("key.examplemod.invis", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.examplemod");
        public static final KeyMapping KEY_PAREDES = new KeyMapping("key.examplemod.paredes", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, "key.categories.examplemod");
        public static final KeyMapping KEY_CHAO = new KeyMapping("key.examplemod.chao", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, "key.categories.examplemod");

        @SubscribeEvent
        public static void onClientTickKeys(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || !temAlgumaPeca(mc.player)) return;

            while (KEY_INVIS.consumeClick()) {
                invisibilidadeAtiva = !invisibilidadeAtiva;
                mc.player.displayClientMessage(VisualEAdaptacao.textoBrilhante(invisibilidadeAtiva ? "👻 Invisibilidade: ATIVA" : "👁 Invisibilidade: DESATIVADA"), true);
            }
            while (KEY_PAREDES.consumeClick()) {
                verParedesAtivo = !verParedesAtivo;
                mc.player.displayClientMessage(VisualEAdaptacao.textoBrilhante(verParedesAtivo ? "👁 Visao Atraves de Paredes [Y]: ATIVA" : "👁 Visao Paredes: OFF"), true);
            }
            while (KEY_CHAO.consumeClick()) {
                verChaoAtivo = !verChaoAtivo;
                mc.player.displayClientMessage(VisualEAdaptacao.textoBrilhante(verChaoAtivo ? "⛏ Visao Atraves do Chao [K]: ATIVA" : "⛏ Visao Chao: OFF"), true);
            }
        }

        @SubscribeEvent
        public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
            if (event.getEntity() == Minecraft.getInstance().player && invisibilidadeAtiva) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderBlockOverlay(RenderBlockScreenEffectEvent event) {
            Player p = event.getPlayer();
            if (temAlgumaPeca(p)) {
                event.setCanceled(true);
            }
        }

        // Capacete tira 100% da neblina laranja da Lava e azul/escura da Agua
        @SubscribeEvent
        public static void onFogDensity(ViewportEvent.RenderFog event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && temCapacete(mc.player)) {
                if (event.getType() == FogType.LAVA || event.getType() == FogType.WATER) {
                    event.setNearPlaneDistance(0.0F);
                    event.setFarPlaneDistance(250.0F);
                    event.setCanceled(true);
                }
            }
        }

        // Escudo Cosmico Translucido Azul (Modo 3) e Roxo (Modo 4) + Visao Atraves de Paredes [Y] e Chao [K]
        @SubscribeEvent
        public static void onRenderLevel(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null || !temAlgumaPeca(mc.player)) return;

            Vec3 cam = event.getCamera().getPosition();
            PoseStack pose = event.getPoseStack();

            // Visao Raio-X Atraves de Paredes (Y) e/ou Chao (K)
            if (verParedesAtivo || verChaoAtivo) {
                var buf = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
                if (verParedesAtivo) {
                    for (Entity e : mc.level.entitiesForRendering()) {
                        if (e != mc.player && e instanceof LivingEntity && !(e.distanceTo(mc.player) > 48.0F)) {
                            AABB bb = e.getBoundingBox().move(-cam.x, -cam.y, -cam.z);
                            LevelRenderer.renderLineBox(pose, buf, bb, 0.0F, 1.0F, 1.0F, 1.0F);
                        }
                    }
                }
                if (verChaoAtivo) {
                    BlockPos centro = mc.player.blockPosition();
                    for (int dx = -12; !(dx > 12); dx += 2) {
                        for (int dy = -20; !(dy > -1); dy += 2) {
                            for (int dz = -12; !(dz > 12); dz += 2) {
                                BlockPos bp = centro.offset(dx, dy, dz);
                                BlockState st = mc.level.getBlockState(bp);
                                if (st.is(Blocks.DIAMOND_ORE) || st.is(Blocks.DEEPSLATE_DIAMOND_ORE) || st.is(Blocks.ANCIENT_DEBRIS) || st.is(Blocks.GOLD_ORE) || st.is(Blocks.CHEST) || st.is(Blocks.SPAWNER)) {
                                    LevelRenderer.renderLineBox(pose, buf,
                                            bp.getX() - cam.x, bp.getY() - cam.y, bp.getZ() - cam.z,
                                            bp.getX() + 1 - cam.x, bp.getY() + 1 - cam.y, bp.getZ() + 1 - cam.z,
                                            1.0F, 0.2F, 1.0F, 1.0F);
                                }
                            }
                        }
                    }
                }
                mc.renderBuffers().bufferSource().endBatch(RenderType.lines());
            }

            // Escudo Cosmico Translucido ao redor do jogador (Modo 3 Azul / Modo 4 Roxo)
            int modo = getModoAtual(mc.player);
            if (temCapacete(mc.player) && (modo == 2 || modo == 3)) {
                pose.pushPose();
                pose.translate(mc.player.getX() - cam.x, mc.player.getY() + 1.0 - cam.y, mc.player.getZ() - cam.z);
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                RenderSystem.disableCull();
                RenderSystem.depthMask(false);
                RenderSystem.setShader(GameRenderer::getPositionColorShader);

                Tesselator tess = Tesselator.getInstance();
                BufferBuilder bb = tess.getBuilder();
                bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
                Matrix4f mat = pose.last().pose();

                int r = (modo == 2) ? 30 : 190;
                int g = (modo == 2) ? 180 : 30;
                int b = 255;
                int a = 55;
                float rad = 2.4F;

                for (int lat = 0; !(lat >= 10); lat++) {
                    double t1 = Math.PI * lat / 10.0;
                    double t2 = Math.PI * (lat + 1) / 10.0;
                    for (int lon = 0; !(lon >= 16); lon++) {
                        double p1 = 2.0 * Math.PI * lon / 16.0;
                        double p2 = 2.0 * Math.PI * (lon + 1) / 16.0;
                        bb.vertex(mat, (float) (rad * Math.sin(t1) * Math.cos(p1)), (float) (rad * Math.cos(t1)), (float) (rad * Math.sin(t1) * Math.sin(p1))).color(r, g, b, a).endVertex();
                        bb.vertex(mat, (float) (rad * Math.sin(t2) * Math.cos(p1)), (float) (rad * Math.cos(t2)), (float) (rad * Math.sin(t2) * Math.sin(p1))).color(r, g, b, a).endVertex();
                        bb.vertex(mat, (float) (rad * Math.sin(t2) * Math.cos(p2)), (float) (rad * Math.cos(t2)), (float) (rad * Math.sin(t2) * Math.sin(p2))).color(r, g, b, a).endVertex();
                        bb.vertex(mat, (float) (rad * Math.sin(t1) * Math.cos(p2)), (float) (rad * Math.cos(t1)), (float) (rad * Math.sin(t1) * Math.sin(p2))).color(r, g, b, a).endVertex();
                    }
                }
                tess.end();
                RenderSystem.depthMask(true);
                RenderSystem.enableCull();
                RenderSystem.defaultBlendFunc();
                pose.popPose();
            }
        }
    }
                                                           }
