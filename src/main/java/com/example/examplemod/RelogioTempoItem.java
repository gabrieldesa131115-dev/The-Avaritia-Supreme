package com.example.examplemod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class RelogioTempoItem extends Item {
    public static double velocidade = 5.0D;
    public static boolean imuneDono = true;
    public static UUID donoUUID = null;
    public static int travaHora = 0;
    public static int travaClima = 0;
    public static boolean pararTudoAtivo = false;

    public static boolean imuneAnimais = false;
    public static boolean imuneMonstros = false;
    public static boolean imunePlayers = false;

    public static final Set CONGELADOS_UUID = new HashSet();
    public static final Set CONGELADOS_PLAYERS = new HashSet();
    public static boolean congelarTodosPlayers = false;

    public static final Set BLOCOS_CONGELADOS = new HashSet();
    public static final Map VEL_INDIVIDUAL = new HashMap();
    public static final Set PARAR_ANIM_INDIVIDUAL = new HashSet();
    public static final Set PARAR_ATAQUE_INDIVIDUAL = new HashSet();
    public static final Map HORA_PERCEBIDA = new HashMap();
    public static final Map ATRASO_PERCEPCAO = new HashMap();
    public static final Map TEMPO_REACAO_PENDENTE = new HashMap();

    public RelogioTempoItem() {
        super(new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return VisualEAdaptacao.textoBrilhante("⏳ Cronômetro do Tempo Absoluto ⏳");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Velocidade Global: " + velocidade + " (Normal: 5.0)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("• [Clique Esquerdo Rapido] Parar Mob, Player, Flecha ou Item").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("• [Segurar Atacar no Alvo] Menu Individual do Alvo").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.literal("• [Clique Direito] Menu Global | [Shift] Contorno de Blocos").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                HitResult hit = player.pick(20.0D, 0.0F, true);
                if (hit instanceof BlockHitResult bhr) {
                    BlockPos pos = bhr.getBlockPos();
                    if (BLOCOS_CONGELADOS.contains(pos)) {
                        BLOCOS_CONGELADOS.remove(pos);
                        player.displayClientMessage(VisualEAdaptacao.textoBrilhante("▶ Bloco descongelado no tempo!"), true);
                    } else {
                        BLOCOS_CONGELADOS.add(pos.immutable());
                        player.displayClientMessage(VisualEAdaptacao.textoBrilhante("❄ Bloco/Fluido congelado no tempo!"), true);
                    }
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> TelaRelogioTempo::abrir);
        } else {
            donoUUID = player.getUUID();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
        public static boolean estaImune(Entity e) {
        if (donoUUID != null && e.getUUID().equals(donoUUID) && imuneDono) return true;
        if (e instanceof Player) return imunePlayers;
        if (e.getType().getCategory() == MobCategory.MONSTER) return imuneMonstros;
        if (e instanceof LivingEntity) return imuneAnimais;
        return false;
    }

    public static boolean estaCongelado(Entity e) {
        if (donoUUID != null && e.getUUID().equals(donoUUID)) return false;
        if (estaImune(e)) return false;
        if (pararTudoAtivo) return true;
        if (CONGELADOS_UUID.contains(e.getUUID())) return true;
        if (e instanceof Player && (congelarTodosPlayers || CONGELADOS_PLAYERS.contains(e.getUUID()))) return true;
        return false;
    }

    public static void processarComando(ServerPlayer sp, String payload) {
        try {
            donoUUID = sp.getUUID();
            List p = Arrays.asList(payload.split("\\|", 6));
            String acao = (String) p.get(0);
            ServerLevel sl = sp.serverLevel();

            if ("VEL".equals(acao)) {
                velocidade = Double.parseDouble((String) p.get(1));
                imuneDono = "1".equals(p.get(2));
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("⏳ Velocidade Global: " + velocidade), true);
            } else if ("HORA".equals(acao)) {
                sl.setDayTime(Long.parseLong((String) p.get(1)));
            } else if ("TRAVAHORA".equals(acao)) {
                travaHora = Integer.parseInt((String) p.get(1));
            } else if ("CLIMA".equals(acao)) {
                travaClima = Integer.parseInt((String) p.get(1));
                aplicarClima(sl, travaClima);
            } else if ("IMUNES".equals(acao)) {
                imuneAnimais = "1".equals(p.get(1));
                imuneMonstros = "1".equals(p.get(2));
                imunePlayers = "1".equals(p.get(3));
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("🛡 Lista de Imunes atualizada!"), true);
            } else if ("FREEZEALL".equals(acao)) {
                pararTudoAtivo = true;
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("⏸ TEMPO 100% PARADO!"), true);
            } else if ("UNFREEZE".equals(acao)) {
                pararTudoAtivo = false;
                CONGELADOS_UUID.clear();
                CONGELADOS_PLAYERS.clear();
                congelarTodosPlayers = false;
                BLOCOS_CONGELADOS.clear();
                PARAR_ANIM_INDIVIDUAL.clear();
                PARAR_ATAQUE_INDIVIDUAL.clear();
                VEL_INDIVIDUAL.clear();
                for (Entity e : sl.getAllEntities()) {
                    e.setNoGravity(false);
                    if (e instanceof Mob m) m.setNoAi(false);
                }
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("▶ Todo o tempo foi retomado!"), true);
            } else if ("IND_TOGGLE".equals(acao)) {
                UUID id = UUID.fromString((String) p.get(1));
                if (CONGELADOS_UUID.contains(id) || CONGELADOS_PLAYERS.contains(id)) {
                    CONGELADOS_UUID.remove(id);
                    CONGELADOS_PLAYERS.remove(id);
                    sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("▶ Alvo descongelado!"), true);
                } else {
                    CONGELADOS_UUID.add(id);
                    CONGELADOS_PLAYERS.add(id);
                    sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("❄ Alvo congelado no tempo!"), true);
                }
            } else if ("IND_VEL".equals(acao)) {
                UUID id = UUID.fromString((String) p.get(1));
                double v = Double.parseDouble((String) p.get(2));
                VEL_INDIVIDUAL.put(id, v);
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("⏳ Velocidade do Alvo: " + v), true);
            } else if ("IND_FLAGS".equals(acao)) {
                UUID id = UUID.fromString((String) p.get(1));
                if ("1".equals(p.get(2))) PARAR_ANIM_INDIVIDUAL.add(id); else PARAR_ANIM_INDIVIDUAL.remove(id);
                if ("1".equals(p.get(3))) PARAR_ATAQUE_INDIVIDUAL.add(id); else PARAR_ATAQUE_INDIVIDUAL.remove(id);
                HORA_PERCEBIDA.put(id, Integer.parseInt((String) p.get(4)));
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("⚙ Configuracao individual aplicada!"), true);
            } else if ("IND_ATRASO".equals(acao)) {
                UUID id = UUID.fromString((String) p.get(1));
                double seg = Double.parseDouble((String) p.get(2));
                ATRASO_PERCEPCAO.put(id, (int) (seg * 20.0));
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("🧠 Atraso de percepcao: " + seg + "s"), true);
            } else if ("IND_STATS".equals(acao)) {
                UUID id = UUID.fromString((String) p.get(1));
                Entity alvo = sl.getEntity(id);
                if (alvo instanceof LivingEntity vivo) {
                    if (!"-".equals(p.get(2))) vivo.setHealth(Float.parseFloat((String) p.get(2)));
                    if (!"-".equals(p.get(4))) vivo.setAirSupply(Integer.parseInt((String) p.get(4)));
                    if (vivo instanceof Player pl && !"-".equals(p.get(3))) {
                        pl.getFoodData().setFoodLevel(Integer.parseInt((String) p.get(3)));
                    }
                    sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("✦ Status do alvo modificados!"), true);
                }
            } else if ("FREEZE".equals(acao)) {
                int cat = Integer.parseInt((String) p.get(1));
                double raio = Double.parseDouble((String) p.get(2));
                boolean todosSrv = "1".equals(p.get(3));
                Set escolhas = new HashSet();
                if (p.size() > 4 && !((String) p.get(4)).isEmpty()) {
                    escolhas.addAll(Arrays.asList(((String) p.get(4)).split(",")));
                }
                if (cat == 3 && todosSrv) {
                    congelarTodosPlayers = true;
                } else {
                    for (Entity e : sl.getEntities(sp, sp.getBoundingBox().inflate(raio))) {
                        if (e == sp) continue;
                        if (cat == 3 && e instanceof Player pl && escolhas.contains(pl.getGameProfile().getName())) {
                            CONGELADOS_PLAYERS.add(pl.getUUID());
                        } else if (cat != 3 && !(e instanceof Player)) {
                            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
                            if (id != null && escolhas.contains(id.toString())) {
                                CONGELADOS_UUID.add(e.getUUID());
                            }
                        }
                    }
                }
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("❄ Alvos selecionados congelados!"), true);
            }
        } catch (Exception ignored) {}
    }

    private static void aplicarClima(ServerLevel sl, int tipo) {
        if (tipo == 1) sl.setWeatherParameters(6000, 0, false, false);
        else if (tipo == 2) sl.setWeatherParameters(0, 6000, true, false);
        else if (tipo == 3) sl.setWeatherParameters(0, 6000, true, true);
                                                                               }
        @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (pararTudoAtivo || BLOCOS_CONGELADOS.contains(event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        if (pararTudoAtivo || BLOCOS_CONGELADOS.contains(event.getPos()) || BLOCOS_CONGELADOS.contains(event.getLiquidPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingTarget(LivingChangeTargetEvent event) {
        Entity e = event.getEntity();
        if (estaCongelado(e) || PARAR_ATAQUE_INDIVIDUAL.contains(e.getUUID())) {
            event.setCanceled(true);
            return;
        }
        Integer horaInd = (Integer) HORA_PERCEBIDA.get(e.getUUID());
        if (horaInd != null && horaInd == 1 && (e instanceof Spider || e instanceof EnderMan)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurtPercepcao(LivingHurtEvent event) {
        Entity vitima = event.getEntity();
        if (ATRASO_PERCEPCAO.containsKey(vitima.getUUID()) && event.getSource().getEntity() instanceof LivingEntity agressor) {
            int atraso = (Integer) ATRASO_PERCEPCAO.get(vitima.getUUID());
            if (atraso > 0) {
                TEMPO_REACAO_PENDENTE.put(vitima.getUUID(), new Object[]{vitima.level().getGameTime() + atraso, agressor});
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel sl)) return;

        if (!pararTudoAtivo) {
            if (travaHora == 1) {
                long dia = sl.getDayTime() % 24000L;
                if (dia > 11000L) sl.setDayTime(sl.getDayTime() - dia + 1000L);
            } else if (travaHora == 2) {
                long dia = sl.getDayTime() % 24000L;
                if (!(dia >= 13000L) || dia > 23000L) sl.setDayTime(sl.getDayTime() - dia + 14000L);
            }
            if (travaClima > 0 && sl.getGameTime() % 40L == 0L) {
                aplicarClima(sl, travaClima);
            }
        }

        for (Entity e : sl.getAllEntities()) {
            if (donoUUID != null && e.getUUID().equals(donoUUID)) continue;

            if (TEMPO_REACAO_PENDENTE.containsKey(e.getUUID())) {
                Object[] dados = (Object[]) TEMPO_REACAO_PENDENTE.get(e.getUUID());
                long horaAcordar = (Long) dados[0];
                if (sl.getGameTime() >= horaAcordar) {
                    TEMPO_REACAO_PENDENTE.remove(e.getUUID());
                    if (e instanceof Mob m) m.setTarget((LivingEntity) dados[1]);
                } else if (e instanceof Mob m) {
                    m.setTarget(null);
                }
            }

            Integer horaInd = (Integer) HORA_PERCEBIDA.get(e.getUUID());
            if (horaInd != null && horaInd == 2 && e instanceof Mob m && m.getTarget() == null) {
                Player perto = sl.getNearestPlayer(m, 16.0D);
                if (perto != null && (donoUUID == null || !perto.getUUID().equals(donoUUID))) {
                    m.setTarget(perto);
                }
            }

            if (estaCongelado(e)) {
                e.setDeltaMovement(Vec3.ZERO);
                e.fallDistance = 0.0F;
                e.setNoGravity(true);
                if (e instanceof Mob m) m.setNoAi(true);
                if (e instanceof Projectile proj) proj.hurtMarked = true;
                if (e instanceof ItemEntity item) item.setNeverPickUp();
                continue;
            } else {
                if (e.isNoGravity() && !(e instanceof Player)) e.setNoGravity(false);
                if (e instanceof Mob m && m.isNoAi()) m.setNoAi(false);
            }

            double vel = VEL_INDIVIDUAL.containsKey(e.getUUID()) ? (Double) VEL_INDIVIDUAL.get(e.getUUID()) : (estaImune(e) ? 5.0D : velocidade);
            if (vel != 5.0D) {
                double fator = Math.max(0.05D, vel / 5.0D);
                if (e instanceof Projectile || e instanceof ItemEntity) {
                    e.setDeltaMovement(e.getDeltaMovement().scale(fator));
                } else if (e instanceof LivingEntity vivo) {
                    if (!(vel >= 5.0D)) {
                        int nivel = (int) Math.min(6, Math.round((5.0D - vel) * 1.4D));
                        vivo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, nivel, false, false));
                        Vec3 v = vivo.getDeltaMovement();
                        vivo.setDeltaMovement(v.x * fator, v.y, v.z * fator);
                    } else {
                        int nivel = (int) Math.min(10, Math.round((vel - 5.0D) * 0.8D));
                        vivo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, nivel, false, false));
                    }
                }
            }
        }
    }
        @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class ClientControleTemporal {
        private static int segurandoTicks = 0;
        private static Entity alvoSegurado = null;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            boolean segurandoRelogio = mc.player.getMainHandItem().getItem() instanceof RelogioTempoItem;
            if (segurandoRelogio && mc.options.keyAttack.isDown()) {
                if (segurandoTicks == 0) {
                    alvoSegurado = getAlvoMirado(mc, 12.0D);
                }
                segurandoTicks++;
                if (segurandoTicks == 8 && mc.screen == null) {
                    if (alvoSegurado != null) {
                        TelaRelogioTempo.abrirIndividual(alvoSegurado);
                    } else {
                        TelaRelogioTempo.abrir();
                    }
                }
            } else {
                if (segurandoTicks > 0 && !(segurandoTicks >= 8) && segurandoRelogio && alvoSegurado != null && mc.screen == null) {
                    mc.player.connection.sendUnsignedCommand("relogiotempo IND_TOGGLE|" + alvoSegurado.getUUID());
                }
                segurandoTicks = 0;
                alvoSegurado = null;
            }
        }

        private static Entity getAlvoMirado(Minecraft mc, double dist) {
            Vec3 inicio = mc.player.getEyePosition(1.0F);
            Vec3 dir = mc.player.getViewVector(1.0F);
            Vec3 fim = inicio.add(dir.scale(dist));
            AABB caixa = mc.player.getBoundingBox().expandTowards(dir.scale(dist)).inflate(1.2D);
            Entity melhor = null;
            double menorDist = dist;
            for (Entity e : mc.level.getEntities(mc.player, caixa)) {
                if (e == mc.player) continue;
                AABB bb = e.getBoundingBox().inflate(0.35D);
                Optional hit = bb.clip(inicio, fim);
                if (hit.isPresent()) {
                    double d = inicio.distanceTo((Vec3) hit.get());
                    if (d < menorDist) {
                        menorDist = d;
                        melhor = e;
                    }
                }
            }
            return melhor;
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRenderLivingPre(RenderLivingEvent.Pre event) {
            LivingEntity vivo = event.getEntity();
            if (estaCongelado(vivo) || PARAR_ANIM_INDIVIDUAL.contains(vivo.getUUID())) {
                vivo.walkAnimation.setSpeed(0.0F);
                vivo.yBodyRot = vivo.yBodyRotO;
                vivo.yHeadRot = vivo.yHeadRotO;
                vivo.setXRot(vivo.xRotO);
                vivo.attackAnim = 0.0F;
                vivo.oAttackAnim = 0.0F;
            }
        }

        @SubscribeEvent
        public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
            if (!pararTudoAtivo && CONGELADOS_UUID.isEmpty() && CONGELADOS_PLAYERS.isEmpty()) return;
            Minecraft mc = Minecraft.getInstance();
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            GuiGraphics gui = event.getGuiGraphics();
            long t = System.currentTimeMillis() / 30L;
            int r = (int) (127 + 127 * Math.sin(t * 0.05));
            int g = (int) (127 + 127 * Math.sin(t * 0.05 + 2.0));
            int b = (int) (127 + 127 * Math.sin(t * 0.05 + 4.0));
            int alpha = pararTudoAtivo ? 42 : 20;
            int cor = (alpha << 24) | (r << 16) | (g << 8) | b;
            gui.fill(0, 0, w, h, cor);
        }

        @SubscribeEvent
        public static void onRenderWorldBlocks(RenderLevelStageEvent event) {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.level == null) return;
            if (!(mc.player.getMainHandItem().getItem() instanceof RelogioTempoItem) || !mc.player.isShiftKeyDown()) return;

            HitResult hit = mc.player.pick(20.0D, 0.0F, true);
            if (hit instanceof BlockHitResult bhr) {
                BlockPos pos = bhr.getBlockPos();
                Vec3 cam = event.getCamera().getPosition();
                var buf = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
                LevelRenderer.renderLineBox(event.getPoseStack(), buf,
                        pos.getX() - cam.x, pos.getY() - cam.y, pos.getZ() - cam.z,
                        pos.getX() + 1 - cam.x, pos.getY() + 1 - cam.y, pos.getZ() + 1 - cam.z,
                        0.0F, 1.0F, 1.0F, 1.0F);
                mc.renderBuffers().bufferSource().endBatch(RenderType.lines());
            }
        }
    }
}
