package de.maxi.ultimate_apple_mod.forge.block;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.item.CupItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MixerMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    // ── Server-side constructor (called by block entity) ───────────────────

    public MixerMenu(int id, Inventory playerInv, Container container, ContainerData data) {
        super(ultimate_apple_modForge.MIXER_MENU_TYPE.get(), id);
        this.container = container;
        this.data       = data;

        checkContainerSize(container, 4);
        addDataSlots(data);

        // ── Mixer slots ─────────────────────────────────────────────────────

        // Slot 0 – Cup (accepts only CupItem)
        addSlot(new Slot(container, MixerBlockEntity.SLOT_CUP, 26, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof CupItem;
            }
        });

        // Slot 1 – Ingredient 1 (accepts any mixable item)
        addSlot(new Slot(container, MixerBlockEntity.SLOT_ING1, 62, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MixerRecipes.getContribution(stack).isPresent();
            }
        });

        // Slot 2 – Ingredient 2
        addSlot(new Slot(container, MixerBlockEntity.SLOT_ING2, 62, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MixerRecipes.getContribution(stack).isPresent();
            }
        });

        // Slot 3 – Output (extract only) — position matches MixerScreen.OUT_X/OUT_Y
        addSlot(new Slot(container, MixerBlockEntity.SLOT_OUTPUT, MixerScreen.OUT_X, MixerScreen.OUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // ── Player inventory (3 rows × 9) ───────────────────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv,
                    col + row * 9 + 9,
                    8 + col * 18,
                    84 + row * 18));
            }
        }

        // ── Player hotbar (9 slots) ──────────────────────────────────────────
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    // ── Client-side constructor (called via IForgeMenuType) ───────────────

    public MixerMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, new SimpleContainer(4), new SimpleContainerData(2));
    }

    // ── Progress sync ──────────────────────────────────────────────────────

    public int getProgress()    { return data.get(0); }
    public int getMaxProgress() { return data.get(1); }

    // ── Validity ───────────────────────────────────────────────────────────

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    // ── Shift-click logic ──────────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slot == null || !slot.hasItem()) return result;

        ItemStack slotStack = slot.getItem();
        result = slotStack.copy();

        if (index < 4) {
            // From mixer → player inventory
            if (!moveItemStackTo(slotStack, 4, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From player → try cup slot first, then ingredient slots
            if (slotStack.getItem() instanceof CupItem) {
                if (!moveItemStackTo(slotStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(slotStack, 1, 3, false)) {
                // Try ingredient slots (1 and 2), skip output (3)
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return result;
    }
}
