package com.example.examplemod;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
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
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            ITEMS.getEntries().forEach(item -> event.accept(item.get()));
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("infinity")
                .then(Commands.literal("universo").executes(ctx -> {
                    InfinityBallItem.criarUniversoProprio(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("dim").then(Commands.argument("nome", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    String nome = StringArgumentType.getString(ctx, "nome");
                    ServerLevel dest = p.server.getLevel(nome.equals("nether") ? Level.NETHER : nome.equals("end") ? Level.END : Level.OVERWORLD);
                    if (dest != null) { p.teleportTo(dest, 0.5, 120.0, 0.5, p.getYRot(), p.getXRot()); p.fallDistance = 0.0F; }
                    return 1;
                })))
                .then(Commands.literal("coords").then(Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                .then(Commands.argument("z", DoubleArgumentType.doubleArg()).executes(ctx -> {
                                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                                    p.teleportTo(p.serverLevel(), DoubleArgumentType.getDouble(ctx, "x"), DoubleArgumentType.getDouble(ctx, "y"), DoubleArgumentType.getDouble(ctx, "z"), p.getYRot(), p.getXRot());
                                    p.fallDistance = 0.0F;
                                    return 1;
                                })))))
                .then(Commands.literal("player").then(Commands.argument("alvo", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ServerPlayer alvo = p.server.getPlayerList().getPlayerByName(StringArgumentType.getString(ctx, "alvo"));
                    if (alvo != null) { p.teleportTo(alvo.serverLevel(), alvo.getX(), alvo.getY(), alvo.getZ(), p.getYRot(), p.getXRot()); p.fallDistance = 0.0F; }
                    return 1;
                }))));
    }

    public static void executarAlvo(Entity target, Player attacker, boolean isIrrelevante) {
        if (target == attacker || !(attacker.level() instanceof ServerLevel serverLevel)) return;
        if (target instanceof ServerPlayer victim) {
            if (victim.gameMode.getGameModeForPlayer() == GameType.SPECTATOR || victim.isCreative()) victim.setGameMode(GameType.SURVIVAL);
            victim.getInventory().dropAll();
            
