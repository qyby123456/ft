package net.farmingtales.ftboss.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class StatueHubViewport {
    private final float scale;
    private final double offsetX;
    private final double offsetY;
    private final int width;
    private final int height;

    public StatueHubViewport(int width, int height, int panelWidth, int panelHeight) {
        this.width = width;
        this.height = height;
        this.scale = Math.max(0.01F, Math.min(1.0F, Math.min((width - 12.0F) / panelWidth, (height - 12.0F) / panelHeight)));
        this.offsetX = scaled() ? (width - panelWidth * this.scale) / 2.0 - (width - panelWidth) / 2 * this.scale : 0;
        this.offsetY = scaled() ? (height - panelHeight * this.scale) / 2.0 - (height - panelHeight) / 2 * this.scale : 0;
    }

    public boolean scaled() { return this.scale < 1.0F; }

    public double localX(double horizontal) { return (horizontal - this.offsetX) / this.scale; }

    public double localY(double vertical) { return (vertical - this.offsetY) / this.scale; }

    public double screenX(double horizontal) { return this.offsetX + horizontal * this.scale; }

    public double screenY(double vertical) { return this.offsetY + vertical * this.scale; }

    public double localDelta(double delta) { return delta / this.scale; }

    public GuiGraphics graphics(GuiGraphics source) {
        return scaled() ? new ScaledGraphics(source, this) : source;
    }

    private static final class ScaledGraphics extends GuiGraphics {
        private final GuiGraphics source;
        private final StatueHubViewport viewport;

        private ScaledGraphics(GuiGraphics source, StatueHubViewport viewport) {
            super(Minecraft.getInstance(), source.bufferSource());
            this.source = source;
            this.viewport = viewport;
            pose().last().pose().set(source.pose().last().pose());
            pose().last().normal().set(source.pose().last().normal());
            pose().translate(viewport.offsetX, viewport.offsetY, 0);
            pose().scale(viewport.scale, viewport.scale, 1.0F);
        }

        @Override
        public void enableScissor(int left, int top, int right, int bottom) {
            this.source.enableScissor((int) Math.floor(this.viewport.screenX(left)), (int) Math.floor(this.viewport.screenY(top)),
                    (int) Math.ceil(this.viewport.screenX(right)), (int) Math.ceil(this.viewport.screenY(bottom)));
        }

        @Override
        public void disableScissor() {
            this.source.disableScissor();
        }

        @Override
        public int guiWidth() { return (int) Math.ceil(this.viewport.localX(this.viewport.width)); }

        @Override
        public int guiHeight() { return (int) Math.ceil(this.viewport.localY(this.viewport.height)); }
    }
}
