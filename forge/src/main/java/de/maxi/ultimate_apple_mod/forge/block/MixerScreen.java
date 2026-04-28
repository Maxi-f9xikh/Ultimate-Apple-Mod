package de.maxi.ultimate_apple_mod.forge.block;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Pure-Java Mixer GUI — no external texture PNG required.
 *
 * Layout (176 × 166):
 *
 *   ┌──────────────────────────────────────────────────┐
 *   │               Mixer                              │  y=6  (title)
 *   │                                                  │
 *   │  [CUP]    [ING1]  ████►  [OUTPUT]               │
 *   │           [ING2]                                 │
 *   │  ─────────────────────────────────────────────  │  y=82 (separator)
 *   │  Inventory                                       │  y=72
 *   │  [  ][  ][  ][  ][  ][  ][  ][  ][  ]           │  y=84  (row 0)
 *   │  [  ][  ][  ][  ][  ][  ][  ][  ][  ]           │  y=102 (row 1)
 *   │  [  ][  ][  ][  ][  ][  ][  ][  ][  ]           │  y=120 (row 2)
 *   │  ─────────────────────────────────────────────  │  y=139 (hotbar divider)
 *   │  [  ][  ][  ][  ][  ][  ][  ][  ][  ]           │  y=142 (hotbar)
 *   └──────────────────────────────────────────────────┘
 */
@OnlyIn(Dist.CLIENT)
public class MixerScreen extends AbstractContainerScreen<MixerMenu> {

    // ── Colours (ARGB) ─────────────────────────────────────────────────────
    private static final int BG           = 0xFFC6C6C6;
    private static final int BORDER_DARK  = 0xFF555555;
    private static final int BORDER_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_DARK    = 0xFF373737;
    private static final int SLOT_INNER   = 0xFF8B8B8B;
    private static final int SLOT_LIGHT   = 0xFFFFFFFF;
    private static final int SEP_LINE     = 0xFF888888;
    private static final int ARROW_BG     = 0xFF8B8B8B;
    private static final int ARROW_FILL   = 0xFFFF8800;
    private static final int ARROW_LINE   = 0xFF373737;
    private static final int TEXT_COLOR   = 0x404040;

    // ── Mixer slot positions (relative to GUI top-left) ────────────────────
    private static final int CUP_X  = 26,  CUP_Y  = 35;
    private static final int ING1_X = 62,  ING1_Y = 17;
    private static final int ING2_X = 62,  ING2_Y = 53;
    /**
     * Output slot: x=130 gives 10 px clearance after the arrowhead.
     * Must match MixerMenu.SLOT_OUTPUT_X.
     */
    static final int OUT_X  = 130, OUT_Y  = 35;

    // ── Progress arrow ─────────────────────────────────────────────────────
    private static final int ARROW_X = 88, ARROW_Y = 34;
    private static final int ARROW_W = 22, ARROW_H = 16;

    // ── Player inventory positions ─────────────────────────────────────────
    private static final int INV_X       = 8;
    private static final int INV_ROW_Y   = 84;   // first row
    private static final int HOTBAR_Y    = 142;
    private static final int SLOT_STRIDE = 18;

    public MixerScreen(MixerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth      = 176;
        this.imageHeight     = 166;
        this.inventoryLabelY = 72;  // "Inventory" label just above player slots
    }

    // ── Render ─────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        // ── Panel background ─────────────────────────────────────────────
        g.fill(x, y, x + imageWidth, y + imageHeight, BG);

        // Outer bevel
        g.fill(x,                   y,                   x + imageWidth, y + 1,           BORDER_DARK);
        g.fill(x,                   y,                   x + 1,          y + imageHeight, BORDER_DARK);
        g.fill(x + imageWidth - 1,  y,                   x + imageWidth, y + imageHeight, BORDER_LIGHT);
        g.fill(x,                   y + imageHeight - 1, x + imageWidth, y + imageHeight, BORDER_LIGHT);

        // ── Mixer area slots ──────────────────────────────────────────────
        drawSlot(g, x + CUP_X,  y + CUP_Y);
        drawSlot(g, x + ING1_X, y + ING1_Y);
        drawSlot(g, x + ING2_X, y + ING2_Y);

        // Output slot — larger frame to visually distinguish it (like crafting output)
        drawOutputSlot(g, x + OUT_X, y + OUT_Y);

        // ── Progress arrow ────────────────────────────────────────────────
        int progress    = menu.getProgress();
        int maxProgress = menu.getMaxProgress();
        int filled = (maxProgress > 0 && progress > 0) ? (progress * ARROW_W) / maxProgress : 0;

        // Track outline + background
        g.fill(x + ARROW_X - 1,          y + ARROW_Y - 1,
               x + ARROW_X + ARROW_W + 1, y + ARROW_Y + ARROW_H + 1, ARROW_LINE);
        g.fill(x + ARROW_X, y + ARROW_Y,
               x + ARROW_X + ARROW_W, y + ARROW_Y + ARROW_H, ARROW_BG);
        // Orange fill
        if (filled > 0) {
            g.fill(x + ARROW_X, y + ARROW_Y,
                   x + ARROW_X + filled, y + ARROW_Y + ARROW_H, ARROW_FILL);
        }
        // Arrowhead ">" — pixel-by-pixel diagonals, capped at 7 steps
        int ax = x + ARROW_X + ARROW_W + 3;
        int ay = y + ARROW_Y;
        int half = ARROW_H / 2;           // = 8
        int steps = Math.min(half, 6);    // limit width so head doesn't overlap output
        for (int i = 0; i < steps; i++) {
            g.fill(ax + i, ay + i,          ax + i + 1, ay + i + 1,          ARROW_LINE); // upper
            g.fill(ax + i, ay + ARROW_H - 1 - i, ax + i + 1, ay + ARROW_H - i, ARROW_LINE); // lower
        }

        // ── Separator: mixer area ↔ player inventory ──────────────────────
        g.fill(x + 7, y + 81, x + 169, y + 82, BORDER_DARK);
        g.fill(x + 7, y + 82, x + 169, y + 83, BORDER_LIGHT);

        // ── Player inventory slot backgrounds (3 rows × 9) ───────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlot(g,
                    x + INV_X + col * SLOT_STRIDE,
                    y + INV_ROW_Y + row * SLOT_STRIDE);
            }
        }

        // ── Hotbar slot backgrounds (9 slots) ────────────────────────────
        for (int col = 0; col < 9; col++) {
            drawSlot(g, x + INV_X + col * SLOT_STRIDE, y + HOTBAR_Y);
        }

        // ── Divider between inventory rows and hotbar ────────────────────
        g.fill(x + 7, y + 139, x + 169, y + 140, BORDER_DARK);
        g.fill(x + 7, y + 140, x + 169, y + 141, BORDER_LIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Title — centred
        int titleX = (imageWidth - font.width(title)) / 2;
        g.drawString(font, title, titleX, 6, TEXT_COLOR, false);

        // "Inventory" label
        g.drawString(font, playerInventoryTitle, 8, inventoryLabelY, TEXT_COLOR, false);

        // Cup label (left of cup slot, well below title)
        g.drawString(font,
            Component.translatable("gui.ultimate_apple_mod.mixer.cup"),
            5, CUP_Y + 18, 0x606060, false);
    }

    // ── Slot drawing helpers ───────────────────────────────────────────────

    /** Standard vanilla-style recessed slot (16 × 16 inner area). */
    private void drawSlot(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 1, x + 17, y,      SLOT_DARK);   // top edge
        g.fill(x - 1, y - 1, x,      y + 17, SLOT_DARK);   // left edge
        g.fill(x - 1, y + 16, x + 17, y + 17, SLOT_LIGHT); // bottom edge
        g.fill(x + 16, y - 1, x + 17, y + 17, SLOT_LIGHT); // right edge
        g.fill(x, y, x + 16, y + 16, SLOT_INNER);           // floor
    }

    /**
     * Larger output slot with an extra frame ring — visually matches the
     * crafting table's oversized output slot.
     */
    private void drawOutputSlot(GuiGraphics g, int x, int y) {
        // Outer decorative ring (3 px around the slot)
        g.fill(x - 4, y - 4, x + 20, y + 20, SEP_LINE);
        g.fill(x - 3, y - 3, x + 19, y + 19, BG);
        // Standard slot inset on top of the ring
        drawSlot(g, x, y);
    }
}
