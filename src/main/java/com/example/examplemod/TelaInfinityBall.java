package com.example.examplemod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TelaInfinityBall extends Screen {
    private int aba = 0; // 0 = Universos, 1 = Players, 2 = Coords/Dims
    private EditBox caixaX;
    private EditBox caixaY;
    private EditBox caixaZ;
    private final List jogadores = new ArrayList();

    public TelaInfinityBall() {
        super(Component.literal("Esfera do Infinito Multiversal"));
    }

    public static void abrir() {
        Minecraft.getInstance().setScreen(new TelaInfinityBall());
    }

    @Override
    protected void init() {
        jogadores.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            for (Player p : mc.level.players()) {
                if (p != mc.player) {
                    jogadores.add(p.getGameProfile().getName());
                }
            }
        }
        reconstruir();
    }
  
    private void reconstruir() {
        this.clearWidgets();
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addRenderableWidget(Button.builder(Component.literal(aba == 0 ? "§d[Criar Universos]" : "Criar Universos"), b -> { aba = 0; reconstruir(); }).bounds(cx - 145, cy - 90, 95, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(aba == 1 ? "§b[Players]" : "Players"), b -> { aba = 1; reconstruir(); }).bounds(cx - 47, cy - 90, 94, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal(aba == 2 ? "§a[Dimensoes/XYZ]" : "Dimensoes/XYZ"), b -> { aba = 2; reconstruir(); }).bounds(cx + 50, cy - 90, 95, 20).build());

        if (aba == 0) {
            this.addRenderableWidget(Button.builder(Component.literal("§f§l⬜ Universo Plano (Arena Gigante)"), b -> { enviar("UNI|PLANO"); this.onClose(); }).bounds(cx - 130, cy - 58, 260, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("§5§l🌌 Universo Vazio Cosmico"), b -> { enviar("UNI|VAZIO"); this.onClose(); }).bounds(cx - 130, cy - 31, 260, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("§b§l🏝 Universo Ilhas Flutuantes"), b -> { enviar("UNI|ILHAS"); this.onClose(); }).bounds(cx - 130, cy - 4, 260, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("§a§l🌳 Universo Normal (Santuario)"), b -> { enviar("UNI|NORMAL"); this.onClose(); }).bounds(cx - 130, cy + 23, 260, 22).build());
            this.addRenderableWidget(Button.builder(Component.literal("§eVoltar ao Overworld (Spawn)"), b -> { enviar("DIM|overworld"); this.onClose(); }).bounds(cx - 130, cy + 52, 260, 20).build());
        } else if (aba == 1) {
            for (int i = 0; !(i >= jogadores.size() || i >= 6); i++) {
                String nome = (String) jogadores.get(i);
                this.addRenderableWidget(Button.builder(Component.literal("§b⚡ Teleportar para: §f" + nome), b -> {
                    enviar("TP|" + nome);
                    this.onClose();
                }).bounds(cx - 130, cy - 58 + (i * 23), 260, 20).build());
            }
        } else {
            this.addRenderableWidget(Button.builder(Component.literal("Overworld"), b -> { enviar("DIM|overworld"); this.onClose(); }).bounds(cx - 135, cy - 58, 86, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("Nether"), b -> { enviar("DIM|nether"); this.onClose(); }).bounds(cx - 43, cy - 58, 86, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("The End"), b -> { enviar("DIM|end"); this.onClose(); }).bounds(cx + 49, cy - 58, 86, 20).build());

            this.caixaX = new EditBox(this.font, cx - 135, cy - 26, 85, 18, Component.literal("X"));
            this.caixaX.setHint(Component.literal("X"));
            this.addRenderableWidget(this.caixaX);

            this.caixaY = new EditBox(this.font, cx - 43, cy - 26, 85, 18, Component.literal("Y"));
            this.caixaY.setHint(Component.literal("Y"));
            this.addRenderableWidget(this.caixaY);

            this.caixaZ = new EditBox(this.font, cx + 49, cy - 26, 85, 18, Component.literal("Z"));
            this.caixaZ.setHint(Component.literal("Z"));
            this.addRenderableWidget(this.caixaZ);

            this.addRenderableWidget(Button.builder(Component.literal("§dTeleportar para Coordenadas"), b -> {
                enviar("XYZ|" + caixaX.getValue().trim() + "|" + caixaY.getValue().trim() + "|" + caixaZ.getValue().trim());
                this.onClose();
            }).bounds(cx - 135, cy + 2, 270, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("§eSalvar Posicao Atual"), b -> { enviar("SALVAR|1"); this.onClose(); }).bounds(cx - 135, cy + 30, 130, 20).build());
            this.addRenderableWidget(Button.builder(Component.literal("§aIr para Posicao Salva"), b -> { enviar("IRSALVO|1"); this.onClose(); }).bounds(cx + 5, cy + 30, 130, 20).build());
        }
    }

    private void enviar(String dados) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.connection.sendUnsignedCommand("infinitymenu " + dados);
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gui);
        gui.drawCenteredString(this.font, Component.literal("❖ Infinity Ball — Controle Dimensional ❖").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), this.width / 2, this.height / 2 - 110, 0xFFFFFF);
        if (aba == 1 && jogadores.isEmpty()) {
            gui.drawCenteredString(this.font, Component.literal("Nenhum outro jogador online no momento").withStyle(ChatFormatting.YELLOW), this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        }
        super.render(gui, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
