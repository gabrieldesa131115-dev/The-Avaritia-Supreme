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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class TelaRelogioTempo extends Screen {
    public static double velocidadeAtual = 5.0D;
    public static boolean imuneDono = true;
    public static int travaHora = 0; // 0 = livre, 1 = sempre dia, 2 = sempre noite
    public static int travaClima = 0; // 0 = livre, 1 = limpo, 2 = chuva, 3 = tempestade

    private int aba = 0; // 0 = Geral, 1 = Hostis, 2 = Pacificos, 3 = Players
    private EditBox caixaVel;
    private EditBox caixaRaio;
    private boolean todosServer = false;
    private int pagina = 0;
    private static final int POR_PAGINA = 5;

    private final List opcoes = new ArrayList();
    private final List nomes = new ArrayList();
    private final Set selecionados = new HashSet();

    public TelaRelogioTempo() {
        super(Component.literal("Cronometro Cosmico Supremo"));
    }

    public static void abrir() {
        Minecraft.getInstance().setScreen(new TelaRelogioTempo());
    }

    @Override
    protected void init() {
        carregarLista();
        reconstruir();
    }

    private void carregarLista() {
        opcoes.clear();
        nomes.clear();
        if (aba == 0) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (aba == 3) {
            for (Player p : mc.level.players()) {
                if (p != mc.player) {
                    opcoes.add(p.getGameProfile().getName());
                    nomes.add(p.getGameProfile().getName());
                }
            }
        } else {
            Set vistos = new LinkedHashSet();
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e != mc.player && !(e instanceof Player) && e instanceof LivingEntity) {
                    boolean hostil = e.getType().getCategory() == MobCategory.MONSTER;
                    if ((aba == 1 && hostil) || (aba == 2 && !hostil)) {
                        vistos.add(e.getType());
                    }
                }
            }
            for (Object obj : vistos) {
                EntityType tipo = (EntityType) obj;
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(tipo);
                if (id != null) {
                    opcoes.add(id.toString());
                    nomes.add(tipo.getDescription().getString());
                }
            }
        }
    }
      private void reconstruir() {
        this.clearWidgets();
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Abas superiores
        this.addRenderableWidget(Button.builder(Component.literal(aba == 0 ? "§b[Tempo/Clima]" : "Tempo/Clima"), b -> { aba = 0; pagina = 0; reconstruir(); }).bounds(cx - 155, cy - 95, 76, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal(aba == 1 ? "§c[Hostis]" : "Hostis"), b -> { aba = 1; pagina = 0; carregarLista(); reconstruir(); }).bounds(cx - 76, cy - 95, 74, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal(aba == 2 ? "§a[Pacificos]" : "Pacificos"), b -> { aba = 2; pagina = 0; carregarLista(); reconstruir(); }).bounds(cx + 1, cy - 95, 76, 18).build());
        this.addRenderableWidget(Button.builder(Component.literal(aba == 3 ? "§d[Players]" : "Players"), b -> { aba = 3; pagina = 0; carregarLista(); reconstruir(); }).bounds(cx + 80, cy - 95, 75, 18).build());

        if (aba == 0) {
            this.caixaVel = new EditBox(this.font, cx - 145, cy - 66, 75, 18, Component.literal("Vel"));
            this.caixaVel.setValue(String.valueOf(velocidadeAtual));
            this.addRenderableWidget(this.caixaVel);

            this.addRenderableWidget(Button.builder(Component.literal("Aplicar Vel"), b -> {
                try {
                    String txt = caixaVel.getValue().trim().replace(",", ".");
                    double v = Double.parseDouble(txt);
                    if (v > 0.0) velocidadeAtual = v;
                } catch (Exception ignored) {}
                enviar("VEL|" + velocidadeAtual + "|" + (imuneDono ? "1" : "0"));
            }).bounds(cx - 65, cy - 67, 72, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("Reset (5.0)"), b -> {
                velocidadeAtual = 5.0D;
                caixaVel.setValue("5.0");
                enviar("VEL|5.0|" + (imuneDono ? "1" : "0"));
            }).bounds(cx + 10, cy - 67, 68, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal(imuneDono ? "§aImune: SIM" : "§cImune: NAO"), b -> {
                imuneDono = !imuneDono;
                enviar("VEL|" + velocidadeAtual + "|" + (imuneDono ? "1" : "0"));
                reconstruir();
            }).bounds(cx + 81, cy - 67, 74, 20).build());

            // Hora
            this.addRenderableWidget(Button.builder(Component.literal("Dia"), b -> enviar("HORA|1000")).bounds(cx - 145, cy - 35, 45, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Noite"), b -> enviar("HORA|13000")).bounds(cx - 97, cy - 35, 48, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal(travaHora == 1 ? "§e[X] Sempre Dia" : "[ ] Sempre Dia"), b -> {
                travaHora = (travaHora == 1) ? 0 : 1;
                enviar("TRAVAHORA|" + travaHora);
                reconstruir();
            }).bounds(cx - 45, cy - 35, 98, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal(travaHora == 2 ? "§9[X] Sempre Noite" : "[ ] Sempre Noite"), b -> {
                travaHora = (travaHora == 2) ? 0 : 2;
                enviar("TRAVAHORA|" + travaHora);
                reconstruir();
            }).bounds(cx + 56, cy - 35, 99, 20).build());

            // Clima
            this.addRenderableWidget(Button.builder(Component.literal("Limpo"), b -> { travaClima = 1; enviar("CLIMA|1"); reconstruir(); }).bounds(cx - 145, cy - 5, 58, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Chuva"), b -> { travaClima = 2; enviar("CLIMA|2"); reconstruir(); }).bounds(cx - 84, cy - 5, 58, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Tempestade"), b -> { travaClima = 3; enviar("CLIMA|3"); reconstruir(); }).bounds(cx - 23, cy - 5, 76, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal(travaClima == 0 ? "§7Clima Livre" : "§bDestravar"), b -> {
                travaClima = 0;
                enviar("CLIMA|0");
                reconstruir();
            }).bounds(cx + 56, cy - 5, 99, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("§a§lDESCONGELAR TODOS OS ALVOS"), b -> enviar("UNFREEZE|ALL")).bounds(cx - 110, cy + 32, 220, 22).build());
        } else {
            this.caixaRaio = new EditBox(this.font, cx - 145, cy - 68, 85, 18, Component.literal("Raio"));
            this.caixaRaio.setHint(Component.literal("Raio (40)"));
            this.addRenderableWidget(this.caixaRaio);

            if (aba == 3) {
                this.addRenderableWidget(Button.builder(Component.literal(todosServer ? "§d[X] Todos Server" : "[ ] Todos Server"), b -> {
                    todosServer = !todosServer;
                    reconstruir();
                }).bounds(cx - 55, cy - 69, 105, 20).build());
            }

            int inicio = pagina * POR_PAGINA;
            for (int i = 0; !(i >= POR_PAGINA); i++) {
                int idx = inicio + i;
                if (idx >= opcoes.size()) break;
                String chave = (String) opcoes.get(idx);
                String nome = (String) nomes.get(idx);
                boolean ok = selecionados.contains(chave);
                this.addRenderableWidget(Button.builder(Component.literal((ok ? "§b[❄] " : "§7[ ] ") + nome), b -> {
                    if (selecionados.contains(chave)) selecionados.remove(chave);
                    else selecionados.add(chave);
                    reconstruir();
                }).bounds(cx - 145, cy - 44 + (i * 21), 290, 19).build());
            }

            if (pagina > 0) {
                this.addRenderableWidget(Button.builder(Component.literal("< Ant"), b -> { pagina--; reconstruir(); }).bounds(cx - 145, cy + 68, 45, 20).build());
            }
            if ((pagina + 1) * POR_PAGINA < opcoes.size()) {
                this.addRenderableWidget(Button.builder(Component.literal("Prox >"), b -> { pagina++; reconstruir(); }).bounds(cx + 100, cy + 68, 45, 20).build());
            }

            this.addRenderableWidget(Button.builder(Component.literal("§b§lPARAR TEMPO DOS SELECIONADOS"), b -> {
                double raio = 40.0D;
                try {
                    String txt = caixaRaio.getValue().trim().replace(",", ".");
                    if (!txt.isEmpty()) raio = Double.parseDouble(txt);
                } catch (Exception ignored) {}
                String lista = String.join(",", selecionados);
                enviar("FREEZE|" + aba + "|" + raio + "|" + (todosServer ? "1" : "0") + "|" + lista);
                this.onClose();
            }).bounds(cx - 95, cy + 68, 190, 20).build());
        }
    }

    private void enviar(String cmd) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendUnsignedCommand("relogiotempo " + cmd);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        gui.drawCenteredString(this.font, Component.literal("⏳ Cronometro Cosmico (Normal = 5.0) ⏳").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), this.width / 2, this.height / 2 - 112, 0xFFFFFF);
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
                  }

