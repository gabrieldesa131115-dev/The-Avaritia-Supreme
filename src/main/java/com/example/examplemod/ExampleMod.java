package com.example.examplemod;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
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

    public static final DeferredRegister ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // ================= ESPADAS =================
    public static final RegistryObject ESPADA_IMENSURAVEL = ITEMS.register("espada_imensuravel", () -> new EspadaSupremaItem(false));
    public static final RegistryObject ESPADA_IRRELEVANTE = ITEMS.register("espada_irrelevante", () -> new EspadaSupremaItem(true));

    // ================= FERRAMENTAS =================
    public static final RegistryObject PICARETA_IMENSURAVEL = ITEMS.register("picareta_imensuravel", () -> new FerramentaSupremaItem.PicaretaSuprema(false));
    public static final RegistryObject PICARETA_IRRELEVANTE = ITEMS.register("picareta_irrelevante", () -> new FerramentaSupremaItem.PicaretaSuprema(true));
    public static final RegistryObject PA_IMENSURAVEL = ITEMS.register("pa_imensuravel", () -> new PaEMachadoSupremoItem.PaSuprema(false));
    public static final RegistryObject PA_IRRELEVANTE = ITEMS.register("pa_irrelevante", () -> new PaEMachadoSupremoItem.PaSuprema(true));
    public static final RegistryObject MACHADO_IMENSURAVEL = ITEMS.register("machado_imensuravel", () -> new PaEMachadoSupremoItem.MachadoSupremo(false));
    public static final RegistryObject MACHADO_IRRELEVANTE = ITEMS.register("machado_irrelevante", () -> new PaEMachadoSupremoItem.MachadoSupremo(true));
    public static final RegistryObject ENXADA_IMENSURAVEL = ITEMS.register("enxada_imensuravel", () -> new EnxadaSupremaItem(false));
    public static final RegistryObject ENXADA_IRRELEVANTE = ITEMS.register("enxada_irrelevante", () -> new EnxadaSupremaItem(true));

    // ================= ARMADURAS E ELYTRA =================
    public static final RegistryObject CAPACETE_IMENSURAVEL = ITEMS.register("capacete_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.HELMET, false));
    public static final RegistryObject PEITORAL_IMENSURAVEL = ITEMS.register("peitoral_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.CHESTPLATE, false));
    public static final RegistryObject CALCA_IMENSURAVEL = ITEMS.register("calca_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.LEGGINGS, false));
    public static final RegistryObject BOTAS_IMENSURAVEL = ITEMS.register("botas_imensuravel", () -> new ArmaduraSupremaItem(ArmorItem.Type.BOOTS, false));

    public static final RegistryObject CAPACETE_IRRELEVANTE = ITEMS.register("capacete_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.HELMET, true));
    public static final RegistryObject PEITORAL_IRRELEVANTE = ITEMS.register("peitoral_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.CHESTPLATE, true));
    public static final RegistryObject CALCA_IRRELEVANTE = ITEMS.register("calca_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.LEGGINGS, true));
    public static final RegistryObject BOTAS_IRRELEVANTE = ITEMS.register("botas_irrelevante", () -> new ArmaduraSupremaItem(ArmorItem.Type.BOOTS, true));

    public static final RegistryObject ELYTRA_IMENSURAVEL = ITEMS.register("elytra_imensuravel", () -> new ElytraSupremaItem(false));
    public static final RegistryObject ELYTRA_IRRELEVANTE = ITEMS.register("elytra_irrelevante", () -> new ElytraSupremaItem(true));
        // ================= ARMAS ESPECIAIS, RELÓGIO E EXTRAS =================
    public static final RegistryObject ARCO_COSMICO = ITEMS.register("arco_cosmico", () -> new ArmasEspeciaisItem.ArcoCosmico(false));
    public static final RegistryObject ARCO_IRRELEVANTE = ITEMS.register("arco_irrelevante", () -> new ArmasEspeciaisItem.ArcoCosmico(true));
    public static final RegistryObject TRIDENTE_COSMICO = ITEMS.register("tridente_cosmico", () -> new ArmasEspeciaisItem.TridenteCosmico(false));
    public static final RegistryObject TRIDENTE_IRRELEVANTE = ITEMS.register("tridente_irrelevante", () -> new ArmasEspeciaisItem.TridenteCosmico(true));
    public static final RegistryObject CAJADO_SINGULARIDADE = ITEMS.register("cajado_singularidade", () -> new ArmasEspeciaisItem.CajadoSingularidade(false));
    public static final RegistryObject CAJADO_IRRELEVANTE = ITEMS.register("cajado_irrelevante", () -> new ArmasEspeciaisItem.CajadoSingularidade(true));
    public static final RegistryObject ESCUDO_ETERNO = ITEMS.register("escudo_eterno", () -> new ArmasEspeciaisItem.EscudoEterno(false));
    public static final RegistryObject ESCUDO_IRRELEVANTE = ITEMS.register("escudo_irrelevante", () -> new ArmasEspeciaisItem.EscudoEterno(true));

    public static final RegistryObject RELOGIO_TEMPO = ITEMS.register("relogio_tempo", () -> new RelogioTempoItem(false));
    public static final RegistryObject RELOGIO_IRRELEVANTE = ITEMS.register("relogio_irrelevante", () -> new RelogioTempoItem(true));

    public static final RegistryObject PEROLA_COSMICA = ITEMS.register("perola_cosmica", () -> new ItensAvaritiaExtra.PerolaCosmica());
    public static final RegistryObject ANEL_SINGULARIDADE = ITEMS.register("anel_singularidade", () -> new ItensAvaritiaExtra.AnelSingularidade());
    public static final RegistryObject MACA_COSMICA = ITEMS.register("maca_cosmica", () -> new ItensAvaritiaExtra.MacaCosmica());
    public static final RegistryObject TOTEM_YGGDRASIL = ITEMS.register("totem_yggdrasil", () -> new ItensAvaritiaExtra.TotemYggdrasil());
    public static final RegistryObject ENSOPADO_COSMICO = ITEMS.register("ensopado_cosmico", () -> new ItensAvaritiaExtra.EnsopadoCosmico());
    public static final RegistryObject BOLINHO_COSMICO = ITEMS.register("bolinho_cosmico", () -> new ItensAvaritiaExtra.BolinhoCosmico());
    public static final RegistryObject NUCLEO_COSMICO = ITEMS.register("nucleo_cosmico", () -> new ItensAvaritiaExtra.NucleoCosmico());
    public static final RegistryObject CATALISADOR_INFINITO = ITEMS.register("catalisador_infinito", () -> new ItensAvaritiaExtra.CatalisadorInfinito());

    // ================= ABA DO CRIATIVO =================
    public static final RegistryObject ABA_SUPREMA = CREATIVE_MODE_TABS.register("aba_suprema", () ->
        CreativeModeTab.builder()
            .title(Component.literal("✦ The Supreme Avaritia ✦"))
            .icon(() -> new ItemStack((Item) ESPADA_IRRELEVANTE.get()))
            .displayItems((params, output) -> {
                ITEMS.getEntries().forEach(item -> output.accept((Item) item.get()));
            })
            .build()
    );

    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new VisualEAdaptacao());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
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

    public static void executarAlvo(Entity alvo, Player atacante, boolean irrelevante) {
        if (alvo == null || alvo == atacante) return;
        if (alvo instanceof LivingEntity vivo) {
            float dano = irrelevante ? Float.MAX_VALUE : 999999999.0F;
            vivo.hurt(atacante.damageSources().playerAttack(atacante), dano);
            vivo.setHealth(0.0F);
            vivo.die(atacante.damageSources().playerAttack(atacante));
        }
        if (irrelevante && !(alvo instanceof Player) && alvo.level() instanceof ServerLevel) {
            alvo.discard();
        }
    }
    @SubscribeEvent
    public void onPlayerTickSupremo(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.player.level().isClientSide()) return;
        Player p = event.player;
        boolean temSupremo = false;
        for (ItemStack armor : p.getArmorSlots()) {
            if (armor.getItem() instanceof ArmaduraSupremaItem) {
                temSupremo = true;
                break;
            }
        }
        if (!temSupremo && (p.getMainHandItem().getItem() instanceof EspadaSupremaItem || p.getOffhandItem().getItem() instanceof EspadaSupremaItem)) {
            temSupremo = true;
        }
        if (temSupremo) {
            if (p.getHealth() < p.getMaxHealth()) p.setHealth(p.getMaxHealth());
            if (p.isOnFire()) p.clearFire();
            p.setAirSupply(p.getMaxAirSupply());
            p.getFoodData().setFoodLevel(20);
            p.getFoodData().setSaturation(20.0F);
        }
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public void onLivingAttackSupremo(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (event.getEntity() instanceof Player p) {
            for (ItemStack armor : p.getArmorSlots()) {
                if (armor.getItem() instanceof ArmaduraSupremaItem) {
                    event.setCanceled(true);
                    return;
                }
            }
            if (p.getMainHandItem().getItem() instanceof EspadaSupremaItem || p.getOffhandItem().getItem() instanceof EspadaSupremaItem) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public void onLivingDeathSupremo(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        if (event.getEntity() instanceof Player p) {
            for (ItemStack armor : p.getArmorSlots()) {
                if (armor.getItem() instanceof ArmaduraSupremaItem) {
                    event.setCanceled(true);
                    p.setHealth(p.getMaxHealth());
                    return;
                }
            }
        }
    }
}
