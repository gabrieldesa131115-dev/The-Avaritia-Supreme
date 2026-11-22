package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public class EspadaSupremaItem extends SwordItem {
    public final boolean isIrrelevante;

    public EspadaSupremaItem(boolean isIrrelevante) {
        super(FerramentaSupremaItem.TIER_APICE, isIrrelevante ? 2147483646 : 999999999, 100.0F,
                new Item.Properties().stacksTo(1).fireResistant().rarity(Rarity.EPIC));
        this.isIrrelevante = isIrrelevante;
    }

    @Override public boolean isFoil(ItemStack stack) { return true; }

    @Override
    public Component getName(ItemStack stack) {
        return VisualEAdaptacao.textoBrilhante(isIrrelevante ? "❖ Espada do Cosmos Irrelevante ❖" : "✦ Espada do Cosmos Imensurável ✦");
    }

    private static String getNomeModo(int m) {
        return switch (m) {
            case 0 -> "Modo 1: Matar Monstros Hostis (Raio 40)";
            case 1 -> "Modo 2: Matar Criaturas Pacificas (Raio 40)";
            case 2 -> "Modo 3: Matar Players (Raio 40)";
            case 3 -> "Modo 4: Matar Todos os 3 Tipos (Raio 40)";
            case 4 -> "Modo 5: Matar Todos os Players do Server";
            case 5 -> "Modo 6: Cortes Cosmicos Roxos e Azuis";
            case 6 -> "Modo 7: Explosao Circular Cosmica (Raio 11)";
            case 7 -> "Modo 8: Menu Seletor (Monstros Hostis)";
            case 8 -> "Modo 9: Menu Seletor (Criaturas Pacificas)";
            case 9 -> "Modo 10: Menu Seletor (Players / Server)";
            case 10 -> "Modo 11: Fogo Cosmico (Ja Assa os Itens)";
            case 11 -> "Modo 12: Fortuna Suprema (10x Mais Loot)";
            default -> "Modo 13: Dano em Area ao Bater (12 blocos)";
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List tooltip, TooltipFlag flag) {
        int modo = stack.getOrCreateTag().getInt("ModoEspada");
        tooltip.add(Component.literal("Modo Atual: " + getNomeModo(modo)).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("• [Shift + Clique Direito] Trocar Modo (1 a 13)").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("• [Clique Direito] Ativar Poder / Abrir Menu").withStyle(ChatFormatting.GOLD));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!player.level().isClientSide() && player.level() instanceof ServerLevel sl) {
            int modo = stack.getOrCreateTag().getInt("ModoEspada");
            if (modo == 10) entity.setSecondsOnFire(20);
            ExampleMod.executarAlvo(entity, player, this.isIrrelevante);

            if (modo == 12) {
                for (Entity e : sl.getEntities(player, player.getBoundingBox().inflate(12.0D))) {
                    if (e != player && e != entity && !(e instanceof ItemEntity)) {
                        ExampleMod.executarAlvo(e, player, this.isIrrelevante);
                    }
                }
            }
            VisualEAdaptacao.adaptarItem(stack, player, "Aniquilacao");
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide() && attacker instanceof Player p) {
            int modo = stack.getOrCreateTag().getInt("ModoEspada");
            if (modo == 10) target.setSecondsOnFire(20);
            ExampleMod.executarAlvo(target, p, this.isIrrelevante);
            VisualEAdaptacao.adaptarItem(stack, p, "Aniquilacao");
        }
        return true;
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getSource().getEntity() instanceof Player p) {
            ItemStack mao = p.getMainHandItem();
            if (mao.getItem() instanceof EspadaSupremaItem) {
                int modo = mao.getOrCreateTag().getInt("ModoEspada");
                if (modo == 10 && p.level() instanceof ServerLevel sl) {
                    // Modo 11: Assa automaticamente os itens dropados
                    for (ItemEntity drop : event.getDrops()) {
                        ItemStack bruto = drop.getItem();
                        Optional receita = sl.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(bruto), sl);
                        if (receita.isPresent()) {
                            ItemStack assado = ((SmeltingRecipe) receita.get()).getResultItem(sl.registryAccess()).copy();
                            if (!assado.isEmpty()) {
                                assado.setCount(bruto.getCount());
                                drop.setItem(assado);
                            }
                        }
                    }
                } else if (modo == 11) {
                    // Modo 12: Multiplica o loot em 10x
                    for (ItemEntity drop : event.getDrops()) {
                        ItemStack st = drop.getItem();
                        st.setCount(Math.min(64, st.getCount() * 10));
                        drop.setItem(st);
                    }
                }
            }
        }
    }
        @Override
    public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        int modo = stack.getOrCreateTag().getInt("ModoEspada");

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                int prox = (modo + 1) % 13;
                stack.getOrCreateTag().putInt("ModoEspada", prox);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("⚔ " + getNomeModo(prox) + " ⚔"), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.5F);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (level.isClientSide()) {
            if (modo == 7 || modo == 8 || modo == 9) {
                int cat = modo - 6;
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TelaSeletorEspada.abrir(cat));
            } else if (modo == 5) {
                EfeitosCosmicosClient.spawnCorteDiagonal(player, false);
                EfeitosCosmicosClient.spawnCorteDiagonal(player, true);
            } else if (modo == 6 || modo == 12) {
                for (int k = 0; !(k > 7); k++) {
                    double ang = k * (Math.PI / 4.0);
                    EfeitosCosmicosClient.spawnRaioDiagonal(player.position().add(Math.cos(ang) * 9.0, 0, Math.sin(ang) * 9.0));
                }
            } else if (modo == 0 || modo == 1 || modo == 2 || modo == 3 || modo == 10 || modo == 11) {
                EfeitosCosmicosClient.spawnRaioDiagonal(player.position().add(player.getLookAngle().scale(12.0)));
            }
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (level instanceof ServerLevel sl) {
            int apagados = 0;
            if (modo == 0 || modo == 1 || modo == 2 || modo == 3 || modo == 10 || modo == 11) {
                AABB area = player.getBoundingBox().inflate(40.0D);
                for (Entity alvo : sl.getEntities(player, area)) {
                    if (alvo == player || alvo instanceof ItemEntity) continue;
                    boolean ehPlayer = alvo instanceof Player;
                    boolean ehHostil = !ehPlayer && alvo.getType().getCategory() == MobCategory.MONSTER;
                    boolean ehPacifico = !ehPlayer && !ehHostil && alvo instanceof LivingEntity;
                    if ((modo == 0 && ehHostil) || (modo == 1 && ehPacifico) || (modo == 2 && ehPlayer) || ((modo == 3 || modo == 10 || modo == 11) && (ehHostil || ehPacifico || ehPlayer))) {
                        if (modo == 10) alvo.setSecondsOnFire(20);
                        ExampleMod.executarAlvo(alvo, player, this.isIrrelevante);
                        apagados++;
                    }
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.2F, 1.4F);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("⚡ " + apagados + " alvos apagados!"), true);
            } else if (modo == 4) {
                if (sl.getServer() != null) {
                    for (ServerPlayer sp : sl.getServer().getPlayerList().getPlayers()) {
                        if (sp != player) {
                            ExampleMod.executarAlvo(sp, player, this.isIrrelevante);
                            apagados++;
                        }
                    }
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.2F, 1.2F);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("☠ " + apagados + " players do servidor apagados!"), true);
            } else if (modo == 5) {
                Vec3 origem = player.getEyePosition();
                Vec3 dir = player.getLookAngle().normalize();
                DustParticleOptions roxo = new DustParticleOptions(new Vector3f(0.75F, 0.1F, 1.0F), 2.2F);
                DustParticleOptions azul = new DustParticleOptions(new Vector3f(0.1F, 0.65F, 1.0F), 2.2F);
                Set atingidos = new HashSet();
                for (int i = 1; !(i > 45); i++) {
                    Vec3 p = origem.add(dir.scale(i));
                    sl.sendParticles(i % 2 == 0 ? roxo : azul, p.x, p.y, p.z, 10, 1.2, 0.5, 1.2, 0.05);
                    for (Entity e : sl.getEntities(player, new AABB(p.x - 2.2, p.y - 2.2, p.z - 2.2, p.x + 2.2, p.y + 2.2, p.z + 2.2))) {
                        if (e != player && !(e instanceof ItemEntity)) atingidos.add(e);
                    }
                }
                for (Object obj : atingidos) {
                    ExampleMod.executarAlvo((Entity) obj, player, this.isIrrelevante);
                    apagados++;
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5F, 0.6F);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("🌀 Cortes Cósmicos apagaram " + apagados + " alvos!"), true);
            } else if (modo == 6 || modo == 12) {
                double raio = 11.0D;
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 1.0, player.getZ(), 3, 3.5, 1.0, 3.5, 0.1);
                for (Entity alvo : sl.getEntities(player, player.getBoundingBox().inflate(raio))) {
                    if (alvo != player && !(alvo instanceof ItemEntity) && !(alvo.distanceTo(player) > raio)) {
                        ExampleMod.executarAlvo(alvo, player, this.isIrrelevante);
                        apagados++;
                    }
                }
                sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 0.8F);
                player.displayClientMessage(VisualEAdaptacao.textoBrilhante("💥 Expurgo Circular apagou " + apagados + " alvos!"), true);
            }
            if (apagados > 0) VisualEAdaptacao.adaptarItem(stack, player, "Expurgo Supremo");
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public static void executarComandoMenu(ServerPlayer player, String payload) {
        try {
            String[] partes = payload.split("\\|", 4);
            int cat = Integer.parseInt(partes);
            double raio = Double.parseDouble(partes);
            boolean todosServer = "1".equals(partes);
            Set escolhas = new HashSet();
            if (partes.length > 3 && !partes.isEmpty()) {
                escolhas.addAll(Arrays.asList(partes.split(",")));
            }
            boolean irr = player.getMainHandItem().getItem() instanceof EspadaSupremaItem esp && esp.isIrrelevante;
            int apagados = 0;

            if (cat == 3 && todosServer && player.getServer() != null) {
                for (ServerPlayer sp : player.getServer().getPlayerList().getPlayers()) {
                    if (sp != player) {
                        ExampleMod.executarAlvo(sp, player, irr);
                        apagados++;
                    }
                }
            } else if (player.level() instanceof ServerLevel sl) {
                for (Entity alvo : sl.getEntities(player, player.getBoundingBox().inflate(raio))) {
                    if (alvo == player || alvo instanceof ItemEntity) continue;
                    if (cat == 3 && alvo instanceof Player p) {
                        if (escolhas.contains(p.getGameProfile().getName())) {
                            ExampleMod.executarAlvo(p, player, irr);
                            apagados++;
                        }
                    } else if (cat != 3 && !(alvo instanceof Player)) {
                        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(alvo.getType());
                        if (id != null && escolhas.contains(id.toString())) {
                            ExampleMod.executarAlvo(alvo, player, irr);
                            apagados++;
                        }
                    }
                }
            }
            player.displayClientMessage(VisualEAdaptacao.textoBrilhante("✦ Menu Espada: " + apagados + " alvos eliminados!"), true);
        } catch (Exception ignored) {}
    }
                            }

