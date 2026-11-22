package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
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

    public static boolean travaMinhaVida = false;
    public static boolean travaMinhaFome = false;
    public static boolean travaMinhasBolhas = false;
    public static boolean autoAdaptarMods = true;
    public static boolean travaVidaOutros = false;
    public static boolean travaFomeOutros = false;
    public static boolean travaBolhasOutros = false;
    public static boolean travaPosOutros = false;

    public static final Set CONGELADOS_UUID = new HashSet();
    public static final Set CONGELADOS_PLAYERS = new HashSet();
    public static boolean congelarTodosPlayers = false;
    public static final Map VIDA_TRAVADA = new HashMap();
    public static final Map FOME_TRAVADA = new HashMap();
    public static final Map AR_TRAVADO = new HashMap();
    public static final Map POS_TRAVADA = new HashMap();
    public static final Map NBT_MODS_TRAVADOS = new HashMap();

    private static final List GOTAS_CONGELADAS = new ArrayList();
    private static boolean estavaChovendoAoParar = false;

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
        tooltip.add(Component.literal("Velocidade Atual: " + velocidade + " (Normal: 5.0)").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("• [Bater no Alvo] Congela / Descongela o Alvo").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("• [Clique Direito / Segurar Atacar] Painel Temporal").withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> TelaRelogioTempo::abrir);
        } else {
            donoUUID = player.getUUID();
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide() && entity != player) {
            donoUUID = player.getUUID();
            UUID id = entity.getUUID();
            if (entity instanceof Player pl) {
                if (CONGELADOS_PLAYERS.contains(id)) {
                    CONGELADOS_PLAYERS.remove(id);
                    player.displayClientMessage(VisualEAdaptacao.textoBrilhante("▶ " + pl.getGameProfile().getName() + " descongelado no tempo!"), true);
                } else {
                    CONGELADOS_PLAYERS.add(id);
                    player.displayClientMessage(VisualEAdaptacao.textoBrilhante("❄ " + pl.getGameProfile().getName() + " congelado no tempo!"), true);
                }
            } else {
                if (CONGELADOS_UUID.contains(id)) {
                    CONGELADOS_UUID.remove(id);
                    if (entity instanceof Mob m) m.setNoAi(false);
                    player.displayClientMessage(VisualEAdaptacao.textoBrilhante("▶ Entidade descongelada no tempo!"), true);
                } else {
                    CONGELADOS_UUID.add(id);
                    if (entity instanceof Mob m) m.setNoAi(true);
                    player.displayClientMessage(VisualEAdaptacao.textoBrilhante("❄ Entidade congelada no tempo!"), true);
                }
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.4F);
        }
        return true;
                                     }
        public static void processarComando(ServerPlayer sp, String payload) {
        try {
            donoUUID = sp.getUUID();
            List p = Arrays.asList(payload.split("\\|", 9));
            String acao = (String) p.get(0);
            ServerLevel sl = sp.serverLevel();

            if ("VEL".equals(acao)) {
                velocidade = Double.parseDouble((String) p.get(1));
                imuneDono = "1".equals(p.get(2));
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("⏳ Velocidade Temporal: " + velocidade + (imuneDono ? " (Você Imune)" : "")), true);
            } else if ("HORA".equals(acao)) {
                sl.setDayTime(Long.parseLong((String) p.get(1)));
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("☀ Horário alterado!"), true);
            } else if ("TRAVAHORA".equals(acao)) {
                travaHora = Integer.parseInt((String) p.get(1));
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante(travaHora == 1 ? "☀ Sempre Dia Ativado!" : (travaHora == 2 ? "🌙 Sempre Noite Ativado!" : "⏳ Horário Livre!")), true);
            } else if ("CLIMA".equals(acao)) {
                travaClima = Integer.parseInt((String) p.get(1));
                aplicarClima(sl, travaClima);
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("🌦 Clima atualizado!"), true);
            } else if ("STATUS".equals(acao)) {
                travaMinhaVida = "1".equals(p.get(1));
                travaMinhaFome = "1".equals(p.get(2));
                travaMinhasBolhas = "1".equals(p.get(3));
                autoAdaptarMods = "1".equals(p.get(4));
                travaVidaOutros = "1".equals(p.get(5));
                travaFomeOutros = "1".equals(p.get(6));
                travaBolhasOutros = "1".equals(p.get(7));
                travaPosOutros = "1".equals(p.get(8));
                VIDA_TRAVADA.clear();
                FOME_TRAVADA.clear();
                AR_TRAVADO.clear();
                POS_TRAVADA.clear();
                NBT_MODS_TRAVADOS.clear();
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("⏳ Travas de Status & Auto-Adaptação atualizadas!"), true);
            } else if ("FREEZEALL".equals(acao)) {
                pararTudoAtivo = true;
                congelarTodosPlayers = true;
                estavaChovendoAoParar = sl.isRaining();
                if (estavaChovendoAoParar) {
                    sl.setWeatherParameters(6000, 0, false, false);
                }
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("⏸ TEMPO 100% PARADO!"), true);
            } else if ("UNFREEZE".equals(acao)) {
                pararTudoAtivo = false;
                CONGELADOS_UUID.clear();
                CONGELADOS_PLAYERS.clear();
                congelarTodosPlayers = false;
                if (estavaChovendoAoParar) {
                    sl.setWeatherParameters(0, 6000, true, travaClima == 3);
                    estavaChovendoAoParar = false;
                }
                for (Entity e : sl.getAllEntities()) {
                    if (e instanceof Mob m) m.setNoAi(false);
                }
                sp.displayClientMessage(VisualEAdaptacao.textoBrilhante("▶ Tempo descongelado!"), true);
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
                        if (e == sp || e instanceof ItemEntity) continue;
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
            if (e instanceof ItemEntity) continue;
            boolean ehDono = (donoUUID != null && e.getUUID().equals(donoUUID));
            if (ehDono) continue;

            boolean congelado = pararTudoAtivo
                    || CONGELADOS_UUID.contains(e.getUUID())
                    || (e instanceof Player && (congelarTodosPlayers || CONGELADOS_PLAYERS.contains(e.getUUID())));

            if (congelado) {
                e.setDeltaMovement(Vec3.ZERO);
                e.fallDistance = 0.0F;
                e.setNoGravity(true);
                if (e instanceof Mob m) m.setNoAi(true);
                continue;
            } else {
                if (e.isNoGravity() && !(e instanceof Player)) e.setNoGravity(false);
                if (e instanceof Mob m && m.isNoAi()) m.setNoAi(false);
            }

            if (velocidade != 5.0D && e instanceof LivingEntity vivo) {
                if (!(velocidade >= 5.0D)) {
                    int nivel = (int) Math.min(6, Math.round((5.0D - velocidade) * 1.4D));
                    vivo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, nivel, false, false));
                    vivo.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10, nivel, false, false));
                    double fator = Math.max(0.05D, velocidade / 5.0D);
                    Vec3 v = vivo.getDeltaMovement();
                    vivo.setDeltaMovement(v.x * fator, v.y, v.z * fator);
                } else {
                    int nivel = (int) Math.min(10, Math.round((velocidade - 5.0D) * 0.8D));
                    vivo.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, nivel, false, false));
                    vivo.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 10, nivel, false, false));
                    if (vivo instanceof Player pl) {
                        pl.causeFoodExhaustion((float) ((velocidade - 5.0D) * 0.035D));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player pl = event.player;
        boolean ehDono = (donoUUID != null && pl.getUUID().equals(donoUUID));

        if (ehDono) {
            if (travaMinhaVida) {
                VIDA_TRAVADA.putIfAbsent(pl.getUUID(), pl.getHealth());
                pl.setHealth((Float) VIDA_TRAVADA.get(pl.getUUID()));
            }
            if (travaMinhaFome) {
                FOME_TRAVADA.putIfAbsent(pl.getUUID(), pl.getFoodData().getFoodLevel());
                pl.getFoodData().setFoodLevel((Integer) FOME_TRAVADA.get(pl.getUUID()));
            }
            if (travaMinhasBolhas) {
                AR_TRAVADO.putIfAbsent(pl.getUUID(), pl.getAirSupply());
                pl.setAirSupply((Integer) AR_TRAVADO.get(pl.getUUID()));
            }
            if (autoAdaptarMods) {
                adaptarStatusDeOutrosMods(pl, true);
            }
            if (imuneDono) return;
        } else {
            if (travaVidaOutros) {
                VIDA_TRAVADA.putIfAbsent(pl.getUUID(), pl.getHealth());
                pl.setHealth((Float) VIDA_TRAVADA.get(pl.getUUID()));
            }
            if (travaFomeOutros) {
                FOME_TRAVADA.putIfAbsent(pl.getUUID(), pl.getFoodData().getFoodLevel());
                pl.getFoodData().setFoodLevel((Integer) FOME_TRAVADA.get(pl.getUUID()));
            }
            if (travaBolhasOutros) {
                AR_TRAVADO.putIfAbsent(pl.getUUID(), pl.getAirSupply());
                pl.setAirSupply((Integer) AR_TRAVADO.get(pl.getUUID()));
            }
            if (travaPosOutros) {
                POS_TRAVADA.putIfAbsent(pl.getUUID(), pl.position());
                Vec3 p = (Vec3) POS_TRAVADA.get(pl.getUUID());
                pl.teleportTo(p.x, p.y, p.z);
            }
            if (autoAdaptarMods && pararTudoAtivo) {
                adaptarStatusDeOutrosMods(pl, false);
            }
        }

        if (velocidade > 5.0D) {
            int nivel = (int) Math.min(10, Math.round((velocidade - 5.0D) * 0.8D));
            pl.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, nivel, false, false));
            pl.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 10, nivel, false, false));
            pl.causeFoodExhaustion((float) ((velocidade - 5.0D) * 0.035D));
        } else if (!(velocidade >= 5.0D)) {
            int nivel = (int) Math.min(6, Math.round((5.0D - velocidade) * 1.4D));
            pl.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, nivel, false, false));
            pl.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 10, nivel, false, false));
        }
    }

    private static void adaptarStatusDeOutrosMods(Player pl, boolean restaurarMaximo) {
        try {
            if (restaurarMaximo) {
                List ruins = new ArrayList();
                for (MobEffectInstance ef : pl.getActiveEffects()) {
                    if (!ef.getEffect().isBeneficial()) ruins.add(ef.getEffect());
                }
                for (Object ef : ruins) {
                    pl.removeEffect((MobEffect) ef);
                }
            }
            CompoundTag tag = pl.getPersistentData();
            if (tag.contains("ForgeCaps")) {
                CompoundTag caps = tag.getCompound("ForgeCaps");
                if (!NBT_MODS_TRAVADOS.containsKey(pl.getUUID())) {
                    NBT_MODS_TRAVADOS.put(pl.getUUID(), caps.copy());
                } else {
                    CompoundTag salvo = (CompoundTag) NBT_MODS_TRAVADOS.get(pl.getUUID());
                    for (String chave : salvo.getAllKeys()) {
                        String min = chave.toLowerCase();
                        if (min.contains("thirst") || min.contains("sede") || min.contains("water") || min.contains("hydration") || min.contains("temp") || min.contains("stamina") || min.contains("mana")) {
                            caps.put(chave, salvo.get(chave).copy());
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class ClientChuvaTemporal {
        private static final Random RAND = new Random();
        private static int segurandoAtaqueTicks = 0;

        @SubscribeEvent
        public static void onClientTickChuva(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            if (mc.player.getMainHandItem().getItem() instanceof RelogioTempoItem && mc.options.keyAttack.isDown()) {
                segurandoAtaqueTicks++;
                if (segurandoAtaqueTicks == 10 && mc.screen == null) {
                    TelaRelogioTempo.abrir();
                }
            } else {
                segurandoAtaqueTicks = 0;
            }

            if (pararTudoAtivo && estavaChovendoAoParar) {
                if (GOTAS_CONGELADAS.isEmpty()) {
                    for (int i = 0; !(i >= 180); i++) {
                        double gx = mc.player.getX() + (RAND.nextDouble() - 0.5) * 24.0;
                        double gy = mc.player.getY() + RAND.nextDouble() * 14.0;
                        double gz = mc.player.getZ() + (RAND.nextDouble() - 0.5) * 24.0;
                        GOTAS_CONGELADAS.add(new Vec3(gx, gy, gz));
                    }
                }
                if (mc.player.tickCount % 4 == 0) {
                    for (Object obj : GOTAS_CONGELADAS) {
                        Vec3 g = (Vec3) obj;
                        mc.level.addParticle(ParticleTypes.DRIPPING_WATER, g.x, g.y, g.z, 0.0, 0.0, 0.0);
                    }
                }
            } else if (!GOTAS_CONGELADAS.isEmpty()) {
                GOTAS_CONGELADAS.clear();
            }
        }
    }
            }
