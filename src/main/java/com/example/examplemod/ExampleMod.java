package com.example.examplemod;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final Map<UUID, ItemStack[]> ARMADURA_BLINDADA = new HashMap<>();

    public static final RegistryObject<Item> ESPADA_IMENSURAVEL = ITEMS.register("espada_imensuravel", () -> new EspadaSupremaItem(false));
    public static final RegistryObject<Item> ESPADA_IRRELEVANTE = ITEMS.register("espada_irrelevante", () -> new EspadaSupremaItem(true));
    public static final RegistryObject<Item> LANCA_IMENSURAVEL = ITEMS.register("lanca_imensuravel", () -> new ArmasEspeciaisItem.LancaSuprema(false));
    public static final RegistryObject<Item> LANCA_IRRELEVANTE = ITEMS.register("lanca_irrelevante", () -> new ArmasEspeciaisItem.LancaSuprema(true));
    public static final RegistryObject<Item> MACA_IMENSURAVEL = ITEMS.register("maca_imensuravel", () -> new ArmasEspeciaisItem.MacaSuprema(false));
    public static final RegistryObject<Item> MACA_IRRELEVANTE = ITEMS.register("maca_irrelevante", () -> new ArmasEspeciaisItem.MacaSuprema(true));
    public static final RegistryObject<Item> ARCO_IMENSURAVEL = ITEMS.register("arco_imensuravel", () -> new ArcoBestaSupremaItem.ArcoSupremo(false));
    public static final RegistryObject<Item> ARCO_IRRELEVANTE = ITEMS.register("arco_irrelevante", () -> new ArcoBestaSupremaItem.ArcoSupremo(true));
    public static final RegistryObject<Item> BESTA_IMENSURAVEL = ITEMS.register("besta_imensuravel", () -> new ArcoBestaSupremaItem.BestaSuprema(false));
    public static final RegistryObject<Item> BESTA_IRRELEVANTE = ITEMS.register("besta_irrelevante", () -> new ArcoBestaSupremaItem.BestaSuprema(true));
    public static final RegistryObject<Item> ESCUDO_IMENSURAVEL = ITEMS.register("escudo_imensuravel", () -> new ItensAvaritiaExtra.EscudoSupremo(false));
    public static final RegistryObject<Item> ESCUDO_IRRELEVANTE = ITEMS.register("escudo_irrelevante", () -> new ItensAvaritiaExtra.EscudoSupremo(true));
    public static final RegistryObject<Item> FERRAMENTA_IMENSURAVEL = ITEMS.register("ferramenta_imensuravel", () -> new FerramentaSupremaItem(false, "✦ Quebra-Mundos Imensurável ✦"));
    public static final RegistryObject<Item> FERRAMENTA_IRRELEVANTE = ITEMS.register("ferramenta_irrelevante", () -> new FerramentaSupremaItem(true, "❖ Quebra-Mundos Irrelevante ❖"));
    public static final RegistryObject<Item> ELYTRA_IMENSURAVEL = ITEMS.register("elytra_imensuravel", () -> new ArmasEspeciaisItem.ElytraSuprema(false));
    public static final RegistryObject<Item> ELYTRA_IRRELEVANTE = ITEMS.register("elytra_irrelevante", () -> new ArmasEspeciaisItem.ElytraSuprema(true));
    public static final RegistryObject<Item> CAPACETE_IMENSURAVEL = ITEMS.register("capacete_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.HELMET, false, "✦ Capacete Imensurável ✦"));
    public static final RegistryObject<Item> PEITORAL_IMENSURAVEL = ITEMS.register("peitoral_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.CHESTPLATE, false, "✦ Peitoral Imensurável ✦"));
    public static final RegistryObject<Item> CALCA_IMENSURAVEL = ITEMS.register("calca_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.LEGGINGS, false, "✦ Calças Imensuráveis ✦"));
    public static final RegistryObject<Item> BOTAS_IMENSURAVEL = ITEMS.register("botas_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.BOOTS, false, "✦ Botas Imensuráveis ✦"));
    public static final RegistryObject<Item> CAPACETE_IRRELEVANTE = ITEMS.register("capacete_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.HELMET, true, "❖ Capacete Irrelevante ❖"));
    public static final RegistryObject<Item> PEITORAL_IRRELEVANTE = ITEMS.register("peitoral_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.CHESTPLATE, true, "❖ Peitoral Irrelevante ❖"));
    public static final RegistryObject<Item> CALCA_IRRELEVANTE = ITEMS.register("calca_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.LEGGINGS, true, "❖ Calças Irrelevantes ❖"));
    public static final RegistryObject<Item> BOTAS_IRRELEVANTE = ITEMS.register("botas_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.BOOTS, true, "❖ Botas Irrelevantes ❖"));
    public static final RegistryObject<Item> TOTEM_ABSOLUTO = ITEMS.register("totem_absoluto", ArmasEspeciaisItem.TotemSupremo::new);
    public static final RegistryObject<Item> INFINITY_BALL = ITEMS.register("infinity_ball", InfinityBallItem::new);
    public static final RegistryObject<Item> RELOGIO_TEMPO = ITEMS.register("relogio_tempo", RelogioTempoItem::new);
    public static final RegistryObject<Item> ENSOPADO_SUPREMO = ITEMS.register("ensopado_supremo", () -> new ItensAvaritiaExtra.ComidaSuprema("✦ Ensopado Cósmico Infinito ✦", false));
    public static final RegistryObject<Item> ALMONDEGA_COSMICA = ITEMS.register("almondega_cosmica", () -> new ItensAvaritiaExtra.ComidaSuprema("❖ Almôndega da Singularidade ❖", true));
    public static final RegistryObject<Item> LINGOTE_INFINITO = ITEMS.register("lingote_infinito", () -> new ItensAvaritiaExtra.MaterialSupremo("✦ Lingote do Infinito Supremo ✦", "Forjado no núcleo das estrelas"));
    public static final RegistryObject<Item> SINGULARIDADE_SUPREMA = ITEMS.register("singularidade_suprema", () -> new ItensAvaritiaExtra.MaterialSupremo("❖ Singularidade Absoluta ❖", "Massa infinita comprimida"));

    public ExampleMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        bus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) ITEMS.getEntries().forEach(item -> event.accept(item.get()));
    }

    public static boolean temAlgumaArmaduraSuprema(Player p) {
        for (ItemStack stack : p.getArmorSlots()) if (stack.getItem() instanceof ArmaduraSupremaItem) return true;
        return false;
    }
        @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInstantBreak(PlayerInteractEvent.LeftClickBlock event) {
        Player p = event.getEntity();
        if (!p.level().isClientSide() && p.level() instanceof ServerLevel sl && p.getMainHandItem().getItem() instanceof FerramentaSupremaItem) {
            FerramentaSupremaItem.quebrarSemLimitacoes(sl, event.getPos(), p, p.getMainHandItem());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player p && temAlgumaArmaduraSuprema(p) && !event.getEffectInstance().getEffect().isBeneficial()) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("infinity")
                .then(Commands.literal("universo").executes(ctx -> { InfinityBallItem.criarUniversoProprio(ctx.getSource().getPlayerOrException()); return 1; }))
                .then(Commands.literal("dim").then(Commands.argument("nome", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException(); String n = StringArgumentType.getString(ctx, "nome");
                    ServerLevel dest = p.server.getLevel(n.equals("nether") ? Level.NETHER : n.equals("end") ? Level.END : Level.OVERWORLD);
                    if (dest != null) { p.teleportTo(dest, 0.5, 120.0, 0.5, p.getYRot(), p.getXRot()); p.fallDistance = 0.0F; } return 1;
                })))
                .then(Commands.literal("coords").then(Commands.argument("x", DoubleArgumentType.doubleArg()).then(Commands.argument("y", DoubleArgumentType.doubleArg()).then(Commands.argument("z", DoubleArgumentType.doubleArg()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    p.teleportTo(p.serverLevel(), DoubleArgumentType.getDouble(ctx, "x"), DoubleArgumentType.getDouble(ctx, "y"), DoubleArgumentType.getDouble(ctx, "z"), p.getYRot(), p.getXRot()); p.fallDistance = 0.0F; return 1;
                })))))
                .then(Commands.literal("player").then(Commands.argument("alvo", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException(); ServerPlayer alvo = p.server.getPlayerList().getPlayerByName(StringArgumentType.getString(ctx, "alvo"));
                    if (alvo != null) { p.teleportTo(alvo.serverLevel(), alvo.getX(), alvo.getY(), alvo.getZ(), p.getYRot(), p.getXRot()); p.fallDistance = 0.0F; } return 1;
                }))));

        event.getDispatcher().register(Commands.literal("relogio")
                .then(Commands.literal("cat").then(Commands.argument("id", IntegerArgumentType.integer(0, 2)).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException(); int id = IntegerArgumentType.getInteger(ctx, "id");
                    if (p.getMainHandItem().getItem() instanceof RelogioTempoItem) p.getMainHandItem().getOrCreateTag().putInt("CategoriaRelogio", id);
                    RelogioTempoItem.abrirMenuCategoria(p, id); return 1;
                })))
                .then(Commands.literal("hora").then(Commands.argument("t", IntegerArgumentType.integer(0)).executes(ctx -> { ctx.getSource().getLevel().setDayTime(IntegerArgumentType.getInteger(ctx, "t")); return 1; })))
                .then(Commands.literal("add").then(Commands.argument("t", IntegerArgumentType.integer(1)).executes(ctx -> { ServerLevel sl = ctx.getSource().getLevel(); sl.setDayTime(sl.getDayTime() + IntegerArgumentType.getInteger(ctx, "t")); return 1; })))
                .then(Commands.literal("clima").then(Commands.argument("tipo", StringArgumentType.word()).executes(ctx -> {
                    ServerLevel sl = ctx.getSource().getLevel(); String t = StringArgumentType.getString(ctx, "tipo");
                    if (t.equals("limpo")) sl.setWeatherParameters(6000, 0, false, false);
                    else if (t.equals("chuva")) sl.setWeatherParameters(0, 6000, true, false);
                    else sl.setWeatherParameters(0, 6000, true, true);
                    return 1;
                })))
                .then(Commands.literal("global").executes(ctx -> {
                    RelogioTempoItem.TEMPO_GLOBAL_PARADO = !RelogioTempoItem.TEMPO_GLOBAL_PARADO;
                    ctx.getSource().getPlayerOrException().displayClientMessage(Component.literal(RelogioTempoItem.TEMPO_GLOBAL_PARADO ? "⏸ TEMPO GLOBAL CONGELADO!" : "▶ TEMPO GLOBAL RETOMADO!"), true);
                    return 1;
                }))
                .then(Commands.literal("alvo").then(Commands.argument("acao", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException(); String acao = StringArgumentType.getString(ctx, "acao");
                    UUID id = RelogioTempoItem.ALVO_SELECIONADO.get(p.getUUID()); Entity alvo = id != null ? p.serverLevel().getEntity(id) : null;
                    if (alvo != null) {
                        if (acao.equals("toggle")) {
                            if (RelogioTempoItem.ENTIDADES_CONGELADAS.remove(id)) { alvo.setNoGravity(false); if (alvo instanceof Mob m) m.setNoAi(false); }
                            else { RelogioTempoItem.ENTIDADES_CONGELADAS.add(id); alvo.setNoGravity(true); if (alvo instanceof Mob m) m.setNoAi(true); }
                        } else if (acao.equals("voltar") && RelogioTempoItem.POSICAO_ANTERIOR.containsKey(id)) {
                            Vec3 pos = RelogioTempoItem.POSICAO_ANTERIOR.get(id); alvo.teleportTo(pos.x, pos.y, pos.z);
                        } else if (acao.equals("apagar")) { executarAlvo(alvo, p, true); RelogioTempoItem.ENTIDADES_CONGELADAS.remove(id); }
                    }
                    return 1;
                }))));
                        }
        public static void executarAlvo(Entity target, Player attacker, boolean isIrrelevante) {
        if (target == attacker || !(attacker.level() instanceof ServerLevel serverLevel)) return;
        if (target instanceof ServerPlayer victim) {
            ARMADURA_BLINDADA.remove(victim.getUUID()); victim.getInventory().armor.clear();
            if (victim.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || victim.isCreative()) victim.setGameMode(GameType.SURVIVAL);
            victim.getInventory().dropAll(); victim.getInventory().clearContent();
            if (isIrrelevante) { victim.setHealth(0.0F); victim.die(attacker.damageSources().fellOutOfWorld()); }
        }
        if (isIrrelevante) {
            target.setInvulnerable(false); target.setDeltaMovement(Vec3.ZERO); target.getPersistentData().getAllKeys().clear();
            if (target instanceof Mob mob) mob.setNoAi(true);
            if (target instanceof LivingEntity living) { living.removeAllEffects(); living.setHealth(0.0F); living.die(attacker.damageSources().fellOutOfWorld()); }
            if (!(target instanceof Player)) { target.remove(Entity.RemovalReason.DISCARDED); target.discard(); }
            serverLevel.sendParticles(ParticleTypes.END_ROD, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.1);
        } else {
            if (target instanceof LivingEntity living) { living.setInvulnerable(false); living.hurt(attacker.damageSources().fellOutOfWorld(), Float.MAX_VALUE); living.setHealth(0.0F); living.die(attacker.damageSources().fellOutOfWorld()); }
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0, 0, 0, 0);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerHurt(LivingAttackEvent event) {
        if (RelogioTempoItem.TEMPO_GLOBAL_PARADO || RelogioTempoItem.ENTIDADES_CONGELADAS.contains(event.getEntity().getUUID())) { event.setCanceled(true); return; }
        if (event.getEntity() instanceof Player p) {
            boolean escudoAtivo = p.isBlocking() && p.getUseItem().getItem() instanceof ItensAvaritiaExtra.EscudoSupremo;
            if (escudoAtivo && event.getSource().getEntity() != null) executarAlvo(event.getSource().getEntity(), p, ((ItensAvaritiaExtra.EscudoSupremo) p.getUseItem().getItem()).isIrrelevante);
            boolean protegido = escudoAtivo || temAlgumaArmaduraSuprema(p) || p.getMainHandItem().getItem() instanceof EspadaSupremaItem || p.getInventory().contains(new ItemStack(TOTEM_ABSOLUTO.get()));
            if (protegido) { event.setCanceled(true); p.setHealth(p.getMaxHealth()); }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player p && (temAlgumaArmaduraSuprema(p) || p.getInventory().contains(new ItemStack(TOTEM_ABSOLUTO.get())))) {
            event.setCanceled(true); p.setHealth(p.getMaxHealth());
            if (p.level() instanceof ServerLevel sl) sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, p.getX(), p.getY() + 1.0, p.getZ(), 40, 0.5, 0.8, 0.5, 0.2);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player p && temAlgumaArmaduraSuprema(p)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAbsoluteStrike(AttackEntityEvent event) {
        Player p = event.getEntity();
        if (!p.level().isClientSide() && p instanceof ServerPlayer sp) {
            if (p.getMainHandItem().getItem() instanceof RelogioTempoItem) { RelogioTempoItem.congelarInstantaneo(event.getTarget(), sp); event.setCanceled(true); }
            else if (p.getMainHandItem().getItem() instanceof EspadaSupremaItem espada) executarAlvo(event.getTarget(), p, espada.isIrrelevante);
        }
    }
        @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide() && event.level instanceof ServerLevel sl) {
            for (Entity e : sl.getAllEntities()) {
                if (e instanceof Player pl && (temAlgumaArmaduraSuprema(pl) || pl.getMainHandItem().getItem() instanceof RelogioTempoItem)) continue;
                if (RelogioTempoItem.TEMPO_GLOBAL_PARADO || RelogioTempoItem.ENTIDADES_CONGELADAS.contains(e.getUUID())) {
                    e.setDeltaMovement(Vec3.ZERO); e.hurtMarked = true; e.setPos(e.xo, e.yo, e.zo);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player p = event.player;
        if (!p.level().isClientSide()) {
            if (p.containerMenu == p.inventoryMenu) {
                ItemStack[] atual = new ItemStack[4];
                for (int i = 0; i < 4; i++) atual[i] = p.getInventory().armor.get(i).copy();
                ARMADURA_BLINDADA.put(p.getUUID(), atual);
            } else if (ARMADURA_BLINDADA.containsKey(p.getUUID())) {
                ItemStack[] salva = ARMADURA_BLINDADA.get(p.getUUID());
                for (int i = 0; i < 4; i++) if (salva[i].getItem() instanceof ArmaduraSupremaItem && p.getInventory().armor.get(i).isEmpty()) p.getInventory().armor.set(i, salva[i].copy());
            }
        }
        if (temAlgumaArmaduraSuprema(p) || p.getMainHandItem().getItem() instanceof RelogioTempoItem) {
            if (event.isCanceled()) event.setCanceled(false);
            p.canUpdate(true); p.setTicksFrozen(0); p.clearFire(); p.fallDistance = 0.0F; p.hurtMarked = true;
            if (p.getAttribute(Attributes.MOVEMENT_SPEED) != null && p.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() < 0.1D) p.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.1D);
        }
        if (p.level().isClientSide() || !(p.level() instanceof ServerLevel sl)) return;
        if (p.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ArmaduraSupremaItem cap) {
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 220, 0, false, false));
            for (Entity e : sl.getEntities(p, p.getBoundingBox().inflate(cap.isIrrelevante ? 150.0 : 60.0))) if (e instanceof LivingEntity l) { l.setGlowingTag(true); if (l.isInvisible()) l.setInvisible(false); }
        }
        Item peitItem = p.getItemBySlot(EquipmentSlot.CHEST).getItem();
        if (peitItem instanceof ArmaduraSupremaItem || peitItem instanceof ArmasEspeciaisItem.ElytraSuprema) {
            p.getAbilities().mayfly = true;
            boolean irr = (peitItem instanceof ArmaduraSupremaItem a && a.isIrrelevante) || (peitItem instanceof ArmasEspeciaisItem.ElytraSuprema e && e.isIrrelevante);
            p.getAbilities().setFlyingSpeed(irr ? 0.25F : 0.12F); p.onUpdateAbilities();
        } else if (!p.isCreative() && !p.isSpectator() && p.getAbilities().mayfly) {
            p.getAbilities().mayfly = false; p.getAbilities().flying = false; p.getAbilities().setFlyingSpeed(0.05F); p.onUpdateAbilities();
        }
        if (p.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ArmaduraSupremaItem cal) {
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, cal.isIrrelevante ? 6 : 3, false, false));
            p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, cal.isIrrelevante ? 4 : 2, false, false));
            p.getFoodData().setFoodLevel(20); p.getFoodData().setSaturation(20.0F);
        }
        if (p.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ArmaduraSupremaItem bot) {
            p.getActiveEffects().removeIf(eff -> !eff.getEffect().isBeneficial());
            if (bot.isIrrelevante && p.tickCount % 3 == 0) sl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY(), p.getZ(), 2, 0.3, 0.1, 0.3, 0.02);
        }
    }
}
