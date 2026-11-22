package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class VisualEAdaptacao {

    private static final ChatFormatting[] ARCO_IRIS = {
            ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
            ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.LIGHT_PURPLE
    };

    public static MutableComponent textoBrilhante(String texto) {
        MutableComponent comp = Component.empty();
        long tempo = System.currentTimeMillis() / 120L;
        for (int i = 0; i < texto.length(); i++) {
            ChatFormatting cor = ARCO_IRIS[(int) ((tempo + i) % ARCO_IRIS.length)];
            comp.append(Component.literal(String.valueOf(texto.charAt(i))).withStyle(cor, ChatFormatting.BOLD));
        }
        return comp;
    }

    public static void adaptarItem(ItemStack stack, Player player, String motivo) {
        if (stack.isEmpty()) return;
        int pontos = stack.getOrCreateTag().getInt("PontosAdaptacao") + 1;
        int nivelAtual = stack.getOrCreateTag().getInt("NivelAdaptacao");
        stack.getOrCreateTag().putInt("PontosAdaptacao", pontos);
        int novoNivel = (pontos / 5) + 1;
        if (novoNivel > nivelAtual) {
            stack.getOrCreateTag().putInt("NivelAdaptacao", novoNivel);
            if (player != null && !player.level().isClientSide()) {
                player.displayClientMessage(textoBrilhante("★ ADAPTAÇÃO EVOLUÍDA! Nível " + novoNivel + " (" + motivo + ") ★"), true);
            }
        }
    }

    public static int getNivelAdaptacao(ItemStack stack) {
        return Math.max(1, stack.getOrCreateTag().getInt("NivelAdaptacao"));
    }

    public static boolean ehItemDoMod(Item item) {
        return item instanceof EspadaSupremaItem || item instanceof ArmaduraSupremaItem
                || item instanceof FerramentaSupremaItem || item instanceof EnxadaSupremaItem
                || item instanceof PaEMachadoSupremoItem.PaSuprema || item instanceof PaEMachadoSupremoItem.MachadoSupremo
                || item instanceof InfinityBallItem || item instanceof RelogioTempoItem
                || item instanceof ArmasEspeciaisItem.ElytraSuprema || item instanceof ArmasEspeciaisItem.LancaSuprema
                || item instanceof ArmasEspeciaisItem.MacaSuprema || item instanceof ArmasEspeciaisItem.TotemSupremo
                || item instanceof ArcoBestaSupremaItem.ArcoSupremo || item instanceof ArcoBestaSupremaItem.BestaSuprema
                || item instanceof ItensAvaritiaExtra.EscudoSupremo || item instanceof ItensAvaritiaExtra.ComidaSuprema
                || item instanceof ItensAvaritiaExtra.MaterialSupremo;
    }

    public static boolean ehIrrelevante(Item item) {
        return (item instanceof EspadaSupremaItem e && e.isIrrelevante)
                || (item instanceof ArmaduraSupremaItem a && a.isIrrelevante)
                || (item instanceof FerramentaSupremaItem f && f.isIrrelevante)
                            || (item instanceof EnxadaSupremaItem ex && ex.isIrrelevante)
            
                || (item instanceof PaEMachadoSupremoItem.PaSuprema pa && pa.isIrrelevante)
                || (item instanceof PaEMachadoSupremoItem.MachadoSupremo ma && ma.isIrrelevante)
                || (item instanceof ArmasEspeciaisItem.ElytraSuprema el && el.isIrrelevante)
                || (item instanceof ArmasEspeciaisItem.LancaSuprema l && l.isIrrelevante)
                || (item instanceof ArmasEspeciaisItem.MacaSuprema m && m.isIrrelevante)
                || (item instanceof ArmasEspeciaisItem.TotemSupremo t && t.isIrrelevante)
                || (item instanceof ArcoBestaSupremaItem.ArcoSupremo ar && ar.isIrrelevante)
                || (item instanceof ArcoBestaSupremaItem.BestaSuprema b && b.isIrrelevante)
                || (item instanceof ItensAvaritiaExtra.EscudoSupremo es && es.isIrrelevante);
    }
        @SubscribeEvent
    public void onTooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        if (!ehItemDoMod(item)) return;
        boolean irr = ehIrrelevante(item);
        int adapt = getNivelAdaptacao(stack);
        if (!event.getToolTip().isEmpty()) {
            String tituloAtual = event.getToolTip().get(0).getString();
            event.getToolTip().set(0, textoBrilhante(tituloAtual));
        }
        event.getToolTip().removeIf(c -> c.getString().contains("99999") || c.getString().contains("100000") || c.getString().contains("Attack Damage") || c.getString().contains("Dano de ataque") || c.getString().contains("Armor") || c.getString().contains("Armadura"));
        event.getToolTip().add(Component.literal("⚡ Adaptação Evolutiva: ").withStyle(ChatFormatting.GRAY)
                .append(textoBrilhante("Nível " + adapt + " (+" + (adapt * 15) + "% Poder)")));
        if (item instanceof EspadaSupremaItem || item instanceof ArmasEspeciaisItem.LancaSuprema || item instanceof ArmasEspeciaisItem.MacaSuprema || item instanceof ArcoBestaSupremaItem.ArcoSupremo || item instanceof ArcoBestaSupremaItem.BestaSuprema) {
            event.getToolTip().add(textoBrilhante(irr ? " +Irrelevante Dano de Ataque" : " +Imensurável Dano de Ataque"));
            event.getToolTip().add(textoBrilhante(" Velocidade de Ataque: Infinita"));
        } else if (item instanceof ArmaduraSupremaItem || item instanceof ItensAvaritiaExtra.EscudoSupremo || item instanceof ArmasEspeciaisItem.ElytraSuprema) {
            event.getToolTip().add(textoBrilhante(" +Infinity Armadura"));
            event.getToolTip().add(textoBrilhante(" Durabilidade: Infinita"));
        } else if (item instanceof FerramentaSupremaItem || item instanceof EnxadaSupremaItem || item instanceof PaEMachadoSupremoItem.PaSuprema || item instanceof PaEMachadoSupremoItem.MachadoSupremo) {
            event.getToolTip().add(textoBrilhante(irr ? " Escavação: Irrelevante (Ápice)" : " Escavação: Imensurável"));
            event.getToolTip().add(textoBrilhante(" Durabilidade: Infinita"));
        }
    }

    @SubscribeEvent
    public void onAttributes(ItemAttributeModifierEvent event) {
        if (ehItemDoMod(event.getItemStack().getItem())) event.clearModifiers();
    }

    @Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
    public static class ClientTooltipColor {
        @SubscribeEvent
        public static void onTooltipColor(RenderTooltipEvent.Color event) {
            if (!ehItemDoMod(event.getItemStack().getItem())) return;
            long t = System.currentTimeMillis() / 15L;
            int hue = (int) (t % 360);
            event.setBackgroundStart(0xF01A0836);
            event.setBackgroundEnd(0xF00B193D);
            event.setBorderStart(java.awt.Color.HSBtoRGB(hue / 360.0F, 0.95F, 1.0F));
            event.setBorderEnd(java.awt.Color.HSBtoRGB(((hue + 90) % 360) / 360.0F, 0.95F, 1.0F));
        }

        @SubscribeEvent
        public static void onRenderSlot(net.minecraftforge.client.event.ContainerScreenEvent.Render.Foreground event) {
            long t = System.currentTimeMillis() / 10L;
            for (net.minecraft.world.inventory.Slot slot : event.getContainerScreen().getMenu().slots) {
                if (slot.hasItem() && ehIrrelevante(slot.getItem().getItem())) {
                    int cor1 = java.awt.Color.HSBtoRGB(((t + slot.index * 15) % 360) / 360.0F, 1.0F, 1.0F);
                    int cor2 = java.awt.Color.HSBtoRGB(((t + slot.index * 15 + 120) % 360) / 360.0F, 1.0F, 1.0F);
                    event.getGuiGraphics().fillGradient(slot.x, slot.y, slot.x + 16, slot.y + 16, (cor1 & 0x00FFFFFF) | 0x55000000, (cor2 & 0x00FFFFFF) | 0x55000000);
                }
            }
        }

        @SubscribeEvent
        public static void onClientTick(net.minecraftforge.event.TickEvent.ClientTickEvent event) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && mc.player != null && mc.level != null && ehIrrelevante(mc.player.getMainHandItem().getItem())) {
                float hue = ((System.currentTimeMillis() / 8L) % 360) / 360.0F;
                int rgb = java.awt.Color.HSBtoRGB(hue, 1.0F, 1.0F);
                org.joml.Vector3f cor = new org.joml.Vector3f(((rgb >> 16) & 255) / 255.0F, ((rgb >> 8) & 255) / 255.0F, (rgb & 255) / 255.0F);
                double ang = mc.player.tickCount * 0.4D;
                mc.level.addParticle(new net.minecraft.core.particles.DustParticleOptions(cor, 1.3F), mc.player.getX() + Math.cos(ang) * 0.8D, mc.player.getY() + 1.1D, mc.player.getZ() + Math.sin(ang) * 0.8D, 0, 0.03, 0);
            }
        }
    
        }
    

    @SubscribeEvent
    public void onItemEntityTick(net.minecraftforge.event.TickEvent.LevelTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.level.isClientSide()) return;
        if (event.level instanceof net.minecraft.server.level.ServerLevel sl) {
            for (net.minecraft.world.entity.Entity e : sl.getAllEntities()) {
                if (e instanceof net.minecraft.world.entity.item.ItemEntity itemEnt && ehItemDoMod(itemEnt.getItem().getItem())) {
                    itemEnt.setInvulnerable(true);
                    itemEnt.setUnlimitedLifetime();
                    itemEnt.setNoGravity(itemEnt.isInLava() || itemEnt.getY() < sl.getMinBuildHeight() + 5);
                    if (itemEnt.getY() < sl.getMinBuildHeight() + 2) {
                        itemEnt.setDeltaMovement(0, 0.8D, 0);
                    }
                }
            }
        }
    }
    private static final java.util.Map<java.util.UUID, java.util.List<net.minecraft.world.item.ItemStack>> ITENS_SALVOS_MORTE = new java.util.HashMap<>();

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public void onPlayerDrops(net.minecraftforge.event.entity.living.LivingDropsEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            java.util.List<net.minecraft.world.item.ItemStack> salvos = new java.util.ArrayList<>();
            java.util.Iterator it = event.getDrops().iterator();
            while (it.hasNext()) {
                                net.minecraft.world.entity.item.ItemEntity drop = (net.minecraft.world.entity.item.ItemEntity) it.next();
                
                if (ehItemDoMod(drop.getItem().getItem())) {
                    salvos.add(drop.getItem().copy());
                    it.remove();
                }
            }
            if (!salvos.isEmpty()) {
                ITENS_SALVOS_MORTE.computeIfAbsent(player.getUUID(), k -> new java.util.ArrayList<>()).addAll(salvos);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();
        java.util.List<net.minecraft.world.item.ItemStack> salvos = ITENS_SALVOS_MORTE.remove(player.getUUID());
        if (salvos != null) {
            for (net.minecraft.world.item.ItemStack stack : salvos) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
    }
    private static final java.util.Map BACKUP_INVENTARIO = new java.util.HashMap();

    @SubscribeEvent
    public void onPlayerTickProtecaoClear(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.player.level().isClientSide()) return;
        net.minecraft.world.entity.player.Player p = event.player;
        java.util.List atuais = new java.util.ArrayList();
        for (int i = 0; !(i >= p.getInventory().getContainerSize()); i++) {
            net.minecraft.world.item.ItemStack st = p.getInventory().getItem(i);
            if (!st.isEmpty() && ehItemDoMod(st.getItem())) {
                atuais.add(st.copy());
            }
        }
        java.util.List antes = (java.util.List) BACKUP_INVENTARIO.get(p.getUUID());
        if (!atuais.isEmpty()) {
            BACKUP_INVENTARIO.put(p.getUUID(), atuais);
        } else if (antes != null && !antes.isEmpty() && p.isAlive()) {
            for (Object obj : antes) {
                net.minecraft.world.item.ItemStack restaurar = ((net.minecraft.world.item.ItemStack) obj).copy();
                p.getInventory().add(restaurar);
            }
            BACKUP_INVENTARIO.remove(p.getUUID());
            p.displayClientMessage(textoBrilhante("✦ Seus itens supremos resistiram ao /clear! ✦"), true);
        }
    }

    @SubscribeEvent
    public void onBreakBlockSupremo(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        net.minecraft.world.entity.player.Player p = event.getPlayer();
        if (p == null || p.level().isClientSide()) return;
        net.minecraft.world.item.ItemStack mao = p.getMainHandItem();
        if (!ehItemDoMod(mao.getItem())) return;

        if (!(mao.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY) >= 10)) {
            mao.enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY, 10);
        }
        int encModo = mao.getOrCreateTag().getInt("ModoEncanto");
        if (encModo == 1) {
            if (mao.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH) == 0) {
                mao.getEnchantmentTags().clear();
                mao.enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY, 10);
                mao.enchant(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH, 1);
            }
        } else {
            if (!(mao.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE) >= 10)) {
                mao.getEnchantmentTags().clear();
                mao.enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY, 10);
                mao.enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE, 10);
            }
        }
    }
}
