package com.example.examplemod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.RegisterCommandsEvent;
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
    public static final Map POSICAO_SALVA = new HashMap();

    public static final DeferredRegister ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject ESPADA_IMENSURAVEL = ITEMS.register("espada_imensuravel", () -> new EspadaSupremaItem(false));
    public static final RegistryObject ESPADA_IRRELEVANTE = ITEMS.register("espada_irrelevante", () -> new EspadaSupremaItem(true));
    public static final RegistryObject LANCA_IMENSURAVEL = ITEMS.register("lanca_imensuravel", () -> new ArmasEspeciaisItem.LancaSuprema(false));
    public static final RegistryObject LANCA_IRRELEVANTE = ITEMS.register("lanca_irrelevante", () -> new ArmasEspeciaisItem.LancaSuprema(true));
    public static final RegistryObject MACA_IMENSURAVEL = ITEMS.register("maca_imensuravel", () -> new ArmasEspeciaisItem.MacaSuprema(false));
    public static final RegistryObject MACA_IRRELEVANTE = ITEMS.register("maca_irrelevante", () -> new ArmasEspeciaisItem.MacaSuprema(true));
    public static final RegistryObject FERRAMENTA_IMENSURAVEL = ITEMS.register("ferramenta_imensuravel", () -> new FerramentaSupremaItem(false, "Picareta do Universo"));
    public static final RegistryObject FERRAMENTA_IRRELEVANTE = ITEMS.register("ferramenta_irrelevante", () -> new FerramentaSupremaItem(true, "Picareta Multiversal"));
    public static final RegistryObject ENXADA_IMENSURAVEL = ITEMS.register("enxada_imensuravel", () -> new EnxadaSupremaItem(false));
    public static final RegistryObject ENXADA_IRRELEVANTE = ITEMS.register("enxada_irrelevante", () -> new EnxadaSupremaItem(true));

    public static final RegistryObject PA_IMENSURAVEL = ITEMS.register("pa_imensuravel", () -> new PaEMachadoSupremoItem.PaSuprema(false));
    public static final RegistryObject PA_IRRELEVANTE = ITEMS.register("pa_irrelevante", () -> new PaEMachadoSupremoItem.PaSuprema(true));
    public static final RegistryObject MACHADO_IMENSURAVEL = ITEMS.register("machado_imensuravel", () -> new PaEMachadoSupremoItem.MachadoSupremo(false));
    public static final RegistryObject MACHADO_IRRELEVANTE = ITEMS.register("machado_irrelevante", () -> new PaEMachadoSupremoItem.MachadoSupremo(true));

    public static final RegistryObject ARCO_IMENSURAVEL = ITEMS.register("arco_imensuravel", () -> new ArcoBestaSupremaItem.ArcoSupremo(false));
    public static final RegistryObject ARCO_IRRELEVANTE = ITEMS.register("arco_irrelevante", () -> new ArcoBestaSupremaItem.ArcoSupremo(true));
    public static final RegistryObject BESTA_IMENSURAVEL = ITEMS.register("besta_imensuravel", () -> new ArcoBestaSupremaItem.BestaSuprema(false));
    public static final RegistryObject BESTA_IRRELEVANTE = ITEMS.register("besta_irrelevante", () -> new ArcoBestaSupremaItem.BestaSuprema(true));
    public static final RegistryObject ESCUDO_IMENSURAVEL = ITEMS.register("escudo_imensuravel", () -> new ItensAvaritiaExtra.EscudoSupremo(false));
    public static final RegistryObject ESCUDO_IRRELEVANTE = ITEMS.register("escudo_irrelevante", () -> new ItensAvaritiaExtra.EscudoSupremo(true));
    public static final RegistryObject ELYTRA_IMENSURAVEL = ITEMS.register("elytra_imensuravel", () -> new ArmasEspeciaisItem.ElytraSuprema(false));
    public static final RegistryObject ELYTRA_IRRELEVANTE = ITEMS.register("elytra_irrelevante", () -> new ArmasEspeciaisItem.ElytraSuprema(true));
        public static final RegistryObject CAPACETE_IMENSURAVEL = ITEMS.register("capacete_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.HELMET, false, "Capacete da Mente Multiversal"));
    public static final RegistryObject PEITORAL_IMENSURAVEL = ITEMS.register("peitoral_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.CHESTPLATE, false, "Peitoral da Resistência Multiversal"));
    public static final RegistryObject CALCA_IMENSURAVEL = ITEMS.register("calca_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.LEGGINGS, false, "Calças do Controle do Multiverso"));
    public static final RegistryObject BOTAS_IMENSURAVEL = ITEMS.register("botas_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.BOOTS, false, "Botas da Locomoção Multiversal"));

    public static final RegistryObject CAPACETE_IRRELEVANTE = ITEMS.register("capacete_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.HELMET, true, "Capacete Supremo do Omniverso"));
            public static final RegistryObject PEITORAL_IRRELEVANTE = ITEMS.register("peitoral_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.CHESTPLATE, true, "Peitoral da Supremacia Omniversal"));
    public static final RegistryObject CALCA_IRRELEVANTE = ITEMS.register("calca_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.LEGGINGS, true, "Calças da Fonte do Omnix"));
    public static final RegistryObject BOTAS_IRRELEVANTE = ITEMS.register("botas_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.BOOTS, true, "Botas Supremas Omniversais"));

    public static final RegistryObject<Item> TOTEM_ABSOLUTO = ITEMS.register("totem_absoluto", () -> new ArmasEspeciaisItem.TotemSupremo(false));
    public static final RegistryObject<Item> TOTEM_YGGDRASIL = ITEMS.register("totem_yggdrasil", () -> new ArmasEspeciaisItem.TotemSupremo(true));
    public static final RegistryObject<Item> INFINITY_BALL = ITEMS.register("infinity_ball", InfinityBallItem::new);
    public static final RegistryObject<Item> RELOGIO_TEMPO = ITEMS.register("relogio_tempo", RelogioTempoItem::new);
    public static final RegistryObject<Item> ENSOPADO_SUPREMO = ITEMS.register("ensopado_supremo", () -> new ItensAvaritiaExtra.ComidaSuprema("Ensopado Cósmico Supremo", true));
    public static final RegistryObject<Item> ALMONDEGA_COSMICA = ITEMS.register("almondega_cosmica", () -> new ItensAvaritiaExtra.ComidaSuprema("Almôndega Cósmica", false));
    
    public static final RegistryObject LINGOTE_INFINITO = ITEMS.register("lingote_infinito", () -> new ItensAvaritiaExtra.MaterialSupremo("Lingote do Infinito", "Forjado no Núcleo de Singularidades"));
    public static final RegistryObject SINGULARIDADE_SUPREMA = ITEMS.register("singularidade_suprema", () -> new ItensAvaritiaExtra.MaterialSupremo("Singularidade Suprema", "Massa Cósmica Absoluta"));

    public static final RegistryObject ABA_SUPREMA = CREATIVE_MODE_TABS.register("aba_suprema", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .title(Component.literal("❖ The Supreme Avaritia ❖"))
            .icon(() -> ((Item) ESPADA_IRRELEVANTE.get()).getDefaultInstance())
            .displayItems((parameters, output) -> ITEMS.getEntries().forEach(item -> output.accept((Item) ((RegistryObject) item).get())))
            .build());

    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new VisualEAdaptacao());
    }

    public static void executarAlvo(Entity alvo, Player atacante, boolean isIrrelevante) {
        if (alvo == null || alvo == atacante) return;
        if (alvo instanceof PartEntity parte && parte.getParent() != null) {
            alvo = parte.getParent();
        }
        if (alvo instanceof LivingEntity living) {
            living.setInvulnerable(false);
            living.setHealth(0.0F);
            living.die(atacante.damageSources().playerAttack(atacante));
            if (isIrrelevante || !living.isDeadOrDying()) {
                living.remove(Entity.RemovalReason.KILLED);
                living.discard();
            }
        } else {
            alvo.remove(Entity.RemovalReason.KILLED);
            alvo.discard();
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("infinity")
                .then(Commands.literal("salvar").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    POSICAO_SALVA.put(sp.getUUID(), sp.position());
                    sp.displayClientMessage(Component.literal("📍 Posição salva com sucesso!").withStyle(ChatFormatting.GREEN), true);
                    return 1;
                }))
                .then(Commands.literal("irsalvo").executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    Vec3 pos = (Vec3) POSICAO_SALVA.get(sp.getUUID());
                    if (pos != null) {
                        sp.teleportTo(sp.serverLevel(), pos.x, pos.y, pos.z, sp.getYRot(), sp.getXRot());
                        sp.fallDistance = 0.0F;
                    }
                    return 1;
                }))
                .then(Commands.literal("universo").executes(ctx -> {
                    InfinityBallItem.criarUniversoProprio(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("dim").then(Commands.argument("nome", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    String nome = StringArgumentType.getString(ctx, "nome");
                    ResourceKey key = nome.equals("nether") ? Level.NETHER : (nome.equals("end") ? Level.END : Level.OVERWORLD);
                    ServerLevel destino = sp.server.getLevel(key);
                    if (destino != null) {
                        sp.teleportTo(destino, sp.getX(), 100.0, sp.getZ(), sp.getYRot(), sp.getXRot());
                        sp.fallDistance = 0.0F;
                    }
                    return 1;
                })))
                .then(Commands.literal("player").then(Commands.argument("alvo", StringArgumentType.word()).executes(ctx -> {
                    ServerPlayer sp = ctx.getSource().getPlayerOrException();
                    ServerPlayer alvo = sp.server.getPlayerList().getPlayerByName(StringArgumentType.getString(ctx, "alvo"));
                    if (alvo != null) {
                        sp.teleportTo(alvo.serverLevel(), alvo.getX(), alvo.getY(), alvo.getZ(), alvo.getYRot(), alvo.getXRot());
                        sp.fallDistance = 0.0F;
                    }
                    return 1;
                })))
                .then(Commands.literal("coords")
                        .then(Commands.argument("x", IntegerArgumentType.integer())
                        .then(Commands.argument("y", IntegerArgumentType.integer())
                        .then(Commands.argument("z", IntegerArgumentType.integer()).executes(ctx -> {
                            ServerPlayer sp = ctx.getSource().getPlayerOrException();
                            sp.teleportTo(sp.serverLevel(), IntegerArgumentType.getInteger(ctx, "x") + 0.5, IntegerArgumentType.getInteger(ctx, "y"), IntegerArgumentType.getInteger(ctx, "z") + 0.5, sp.getYRot(), sp.getXRot());
                            sp.fallDistance = 0.0F;
                            return 1;
                        }))))));

        event.getDispatcher().register(
            Commands.literal("espadaexpurgo")
                .then(Commands.argument("dados", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        if (ctx.getSource().getEntity() instanceof ServerPlayer sp) {
                            EspadaSupremaItem.executarComandoMenu(sp, StringArgumentType.getString(ctx, "dados"));
                        }
                        return 1;
                    })
                )
        );
    }
    }
