package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class TelaSeletorEspada extends Screen {
    private final int categoria; // 1 = Hostis, 2 = Pacificos, 3 = Players
    private final List<String> opcoes = new ArrayList<>();
    private final List<String> nomesVisiveis = new ArrayList<>();
    private final Set<String> selecionados = new HashSet<>();
    private EditBox caixaRaio;
    private boolean todosDoServer = false;
    private int pagina = 0;
    private static final int POR_PAGINA = 6;

    public TelaSeletorEspada(int categoria) {
        super(Component.literal(categoria == 1 ? "Seletor: Monstros Hostis" : (categoria == 2 ? "Seletor: Criaturas Pacificas" : "Seletor: Jogadores")));
        this.categoria = categoria;
    }

    public static void abrir(int categoria) {
        Minecraft.getInstance().setScreen(new TelaSeletorEspada(categoria));
    }

    @Override
    protected void init() {
        opcoes.clear();
        nomesVisiveis.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            if (categoria == 3) {
                for (Player p : mc.level.players()) {
                    if (p != mc.player) {
                        opcoes.add(p.getGameProfile().getName());
                        nomesVisiveis.add(p.getGameProfile().getName());
                    }
                }
            } else {
                Set<EntityType> vistos = new LinkedHashSet<>();
                for (Entity e : mc.level.entitiesForRendering()) {
                    if (e != mc.player && !(e instanceof Player)) {
                        boolean hostil = e.getType().getCategory() == MobCategory.MONSTER;
                        if ((categoria == 1 && hostil) || (categoria == 2 && !hostil && e instanceof net.minecraft.world.entity.LivingEntity)) {
                            vistos.add(e.getType());
                        }
                    }
                }
                for (EntityType tipo : vistos) {
                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(tipo);
                    if (id != null) {
                        opcoes.add(id.toString());
                        nomesVisiveis.add(tipo.getDescription().getString());
                    }
                }
            }
        }
        reconstruirBotoes();
    }
      private void reconstruirBotoes() {
        this.clearWidgets();
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.caixaRaio = new EditBox(this.font, cx - 100, cy - 82, 95, 18, Component.literal("Raio"));
        this.caixaRaio.setHint(Component.literal("Raio (40)"));
        this.addRenderableWidget(this.caixaRaio);

        if (categoria == 3) {
            String txtServer = todosDoServer ? "[X] Todos do Server" : "[ ] Todos do Server";
            this.addRenderableWidget(Button.builder(Component.literal(txtServer), b -> {
                todosDoServer = !todosDoServer;
                reconstruirBotoes();
            }).bounds(cx + 2, cy - 83, 108, 20).build());
        }

        int inicio = pagina * POR_PAGINA;
        for (int i = 0; i < POR_PAGINA; i++) {
            int idx = inicio + i;
            if (idx >= opcoes.size()) break;
            String chave = opcoes.get(idx);
            String nome = nomesVisiveis.get(idx);
            boolean marcado = selecionados.contains(chave);
            String rotulo = (marcado ? "§a[✔] " : "§7[ ] ") + nome;
            this.addRenderableWidget(Button.builder(Component.literal(rotulo), b -> {
                if (selecionados.contains(chave)) selecionados.remove(chave);
                else selecionados.add(chave);
                reconstruirBotoes();
            }).bounds(cx - 100, cy - 56 + (i * 22), 200, 20).build());
        }

        if (pagina > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("< Ant"), b -> {
                pagina--;
                reconstruirBotoes();
            }).bounds(cx - 100, cy + 80, 45, 20).build());
        }
        if ((pagina + 1) * POR_PAGINA < opcoes.size()) {
            this.addRenderableWidget(Button.builder(Component.literal("Prox >"), b -> {
                pagina++;
                reconstruirBotoes();
            }).bounds(cx + 55, cy + 80, 45, 20).build());
        }

        this.addRenderableWidget(Button.builder(Component.literal("§d§lEXECUTAR EXPURGO"), b -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                double raio = 40.0D;
                try {
                    String txt = caixaRaio.getValue().trim();
                    if (!txt.isEmpty()) raio = Double.parseDouble(txt);
                } catch (Exception ignored) {}
                String alvosStr = String.join(",", selecionados);
                String payload = categoria + "|" + raio + "|" + (todosDoServer ? "1" : "0") + "|" + alvosStr;
                mc.player.connection.sendUnsignedCommand("espadaexpurgo " + payload);
            }
            this.onClose();
        }).bounds(cx - 50, cy + 80, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        gui.drawCenteredString(this.font, this.title.copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), this.width / 2, this.height / 2 - 102, 0xFFFFFF);
        if (opcoes.isEmpty() && !todosDoServer) {
            gui.drawCenteredString(this.font, Component.literal("Nenhum alvo visivel carregado no momento").withStyle(ChatFormatting.YELLOW), this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        }
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

