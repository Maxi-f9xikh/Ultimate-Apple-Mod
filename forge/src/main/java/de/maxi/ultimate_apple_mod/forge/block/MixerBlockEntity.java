package de.maxi.ultimate_apple_mod.forge.block;

import de.maxi.ultimate_apple_mod.forge.ultimate_apple_modForge;
import de.maxi.ultimate_apple_mod.item.CupItem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MixerBlockEntity extends BlockEntity implements Container, MenuProvider {

    // ── Slot indices ───────────────────────────────────────────────────────
    public static final int SLOT_CUP    = 0;
    public static final int SLOT_ING1   = 1;
    public static final int SLOT_ING2   = 2;
    public static final int SLOT_OUTPUT = 3;

    /** 5 seconds at 20 ticks/s */
    public static final int MAX_PROGRESS = 100;

    // ── State ──────────────────────────────────────────────────────────────

    private final NonNullList<ItemStack> items =
        NonNullList.withSize(4, ItemStack.EMPTY);

    /**
     * Non-null while a mix is in progress.
     * The NBT tag that will be attached to the output shake when done.
     * Ingredients are consumed WHEN this is set (at mix-start), not at mix-end.
     */
    @Nullable
    private CompoundTag pendingShakeTag = null;

    int progress = 0;
    private boolean stateDirty = false;

    final ContainerData data = new ContainerData() {
        @Override public int get(int i)         { return i == 0 ? progress : MAX_PROGRESS; }
        @Override public void set(int i, int v) { if (i == 0) progress = v; }
        @Override public int getCount()         { return 2; }
    };

    public MixerBlockEntity(BlockPos pos, BlockState state) {
        super(ultimate_apple_modForge.MIXER_BLOCK_ENTITY.get(), pos, state);
    }

    // ── MenuProvider ───────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ultimate_apple_mod.mixer");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MixerMenu(id, inv, this, this.data);
    }

    // ── Server tick ─────────────────────────────────────────────────────────

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixerBlockEntity be) {
        if (be.pendingShakeTag != null) {
            // A mix is in progress — tick the bar
            be.progress++;
            if (be.progress >= MAX_PROGRESS) {
                // Produce the shake
                ItemStack shake = new ItemStack(ultimate_apple_modForge.SHAKE_ITEM.get());
                shake.setTag(be.pendingShakeTag.copy());
                be.items.set(SLOT_OUTPUT, shake);
                be.pendingShakeTag = null;
                be.progress = 0;
                be.stateDirty = true;
            }
            be.setChanged();
        } else {
            // Idle — try to start a new mix
            if (be.canStartMix()) {
                // Derive contributions before consuming items
                MixerRecipes.ShakeContribution c1 =
                    MixerRecipes.getContribution(be.items.get(SLOT_ING1)).get();
                MixerRecipes.ShakeContribution c2 =
                    MixerRecipes.getContribution(be.items.get(SLOT_ING2)).get();

                // Quantum Apple: replace its fixed contribution with a random one
                // from the registry so every shake with Quantum is a surprise.
                ResourceLocation quantumId =
                    new ResourceLocation("ultimate_apple_mod", "quantum_apple");
                var rng = level.getRandom();
                List<MixerRecipes.ShakeContribution> pool =
                    MixerRecipes.getRandomizableContributions();
                if (!pool.isEmpty()) {
                    if (quantumId.equals(
                            ForgeRegistries.ITEMS.getKey(be.items.get(SLOT_ING1).getItem()))) {
                        c1 = pool.get(rng.nextInt(pool.size()));
                    }
                    if (quantumId.equals(
                            ForgeRegistries.ITEMS.getKey(be.items.get(SLOT_ING2).getItem()))) {
                        c2 = pool.get(rng.nextInt(pool.size()));
                    }
                }

                // Consume 1 of each item immediately (furnace-style)
                be.consumeSlot(SLOT_CUP);
                be.consumeSlot(SLOT_ING1);
                be.consumeSlot(SLOT_ING2);

                // Build the pending shake tag
                be.pendingShakeTag = buildShakeNbt(c1, c2);
                be.progress = 0;
                be.stateDirty = true;
                be.setChanged();
            }
        }

        if (be.stateDirty) {
            be.syncBlockState(level, pos, state);
            be.stateDirty = false;
            be.setChanged();
        }
    }

    // ── Mix preconditions ───────────────────────────────────────────────────

    /**
     * Returns true when all conditions are met to begin a new mix cycle:
     *  - no mix already in progress
     *  - output slot is empty (shake must be taken before more can be made)
     *  - cup slot has a CupItem
     *  - both ingredient slots have valid, DIFFERENT items
     */
    private boolean canStartMix() {
        if (pendingShakeTag != null) return false;           // already mixing
        if (!items.get(SLOT_OUTPUT).isEmpty()) return false; // must take output first

        ItemStack cup  = items.get(SLOT_CUP);
        ItemStack ing1 = items.get(SLOT_ING1);
        ItemStack ing2 = items.get(SLOT_ING2);

        if (!(cup.getItem() instanceof CupItem)) return false;
        if (ing1.isEmpty() || ing2.isEmpty()) return false;
        if (ing1.getItem() == ing2.getItem()) return false;  // same type not allowed

        return MixerRecipes.getContribution(ing1).isPresent()
            && MixerRecipes.getContribution(ing2).isPresent();
    }

    // ── Shake production ─────────────────────────────────────────────────────

    /**
     * Merges two ShakeContributions into a single NBT CompoundTag.
     * Duplicate effects are deduplicated: highest amplifier and longest duration win.
     */
    private static CompoundTag buildShakeNbt(MixerRecipes.ShakeContribution c1,
                                              MixerRecipes.ShakeContribution c2) {
        boolean clearsEffects = c1.clearsEffects() || c2.clearsEffects();

        // When ANY ingredient cleanses, the whole shake is cleanse-only —
        // no effects from either ingredient are carried over.
        Map<ResourceLocation, MixerRecipes.EffectData> merged = new LinkedHashMap<>();
        if (!clearsEffects) {
            for (MixerRecipes.EffectData e : c1.effects()) mergeEffect(merged, e);
            for (MixerRecipes.EffectData e : c2.effects()) mergeEffect(merged, e);
        }

        // Sum / OR the special values — also zeroed out when cleansing.
        int dragonCharges  = clearsEffects ? 0 : (c1.dragonCharges() + c2.dragonCharges());
        boolean lifesteal    = !clearsEffects && (c1.lifesteal()    || c2.lifesteal());
        boolean witherCurse  = !clearsEffects && (c1.witherCurse()  || c2.witherCurse());
        boolean voidLaunch   = !clearsEffects && (c1.voidLaunch()   || c2.voidLaunch());
        boolean rewindEffect = !clearsEffects && (c1.rewindEffect() || c2.rewindEffect());
        boolean orchardSpawn = !clearsEffects && (c1.orchardSpawn() || c2.orchardSpawn());
        boolean enderTeleport = !clearsEffects && (c1.enderTeleport() || c2.enderTeleport());
        // isBomb: always throwable when either ingredient is the Apple Bomb
        boolean isBomb = c1.isBomb() || c2.isBomb();

        // Duration multiplier: take the higher of the two ingredients' multipliers
        // (Longevity Apple contributes 2.0; everything else is 1.0).
        // On top of this, all shakes get a flat +20 % bonus duration.
        double multiplier = Math.max(c1.durationMultiplier(), c2.durationMultiplier());

        CompoundTag tag = new CompoundTag();
        ListTag effectsList = new ListTag();
        for (MixerRecipes.EffectData e : merged.values()) {
            CompoundTag et = new CompoundTag();
            et.putString("id",       e.id().toString());
            // +20 % base bonus, then longevity multiplier on top
            et.putInt("duration",  (int)(e.duration() * 1.20 * multiplier));
            et.putInt("amplifier", e.amplifier());
            effectsList.add(et);
        }
        tag.put("effects", effectsList);
        tag.putInt("dragonCharges",      dragonCharges);
        tag.putBoolean("lifesteal",      lifesteal);
        tag.putBoolean("witherCurse",    witherCurse);
        tag.putBoolean("clearsEffects",  clearsEffects);
        tag.putBoolean("voidLaunch",     voidLaunch);
        tag.putBoolean("rewindEffect",   rewindEffect);
        tag.putBoolean("orchardSpawn",   orchardSpawn);
        tag.putBoolean("enderTeleport",  enderTeleport);
        tag.putBoolean("isBomb",         isBomb);
        return tag;
    }

    private static void mergeEffect(Map<ResourceLocation, MixerRecipes.EffectData> map,
                                     MixerRecipes.EffectData e) {
        if (map.containsKey(e.id())) {
            MixerRecipes.EffectData existing = map.get(e.id());
            int amp = Math.max(existing.amplifier(), e.amplifier());
            int dur = Math.max(existing.duration(),  e.duration());
            map.put(e.id(), new MixerRecipes.EffectData(e.id(), dur, amp));
        } else {
            map.put(e.id(), e);
        }
    }

    // ── Slot helpers ────────────────────────────────────────────────────────

    /** Decrements one item from the given slot; clears it if it reaches zero. */
    private void consumeSlot(int slot) {
        ItemStack stack = items.get(slot);
        stack.shrink(1);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
    }

    private void syncBlockState(Level level, BlockPos pos, BlockState state) {
        // HAS_JAR = mixing in progress OR cup / ingredient items are present
        boolean hasJar = (pendingShakeTag != null)
            || !items.get(SLOT_CUP).isEmpty()
            || !items.get(SLOT_ING1).isEmpty()
            || !items.get(SLOT_ING2).isEmpty();
        boolean hasShake = !items.get(SLOT_OUTPUT).isEmpty();

        BlockState newState = state
            .setValue(MixerBlock.HAS_JAR,   hasJar)
            .setValue(MixerBlock.HAS_SHAKE, hasShake);
        if (!newState.equals(state)) {
            level.setBlock(pos, newState, 3);
        }
    }

    // ── Container interface ─────────────────────────────────────────────────

    @Override
    public int getContainerSize() { return items.size(); }

    @Override
    public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }

    @Override
    public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack result = ContainerHelper.removeItem(items, slot, count);
        if (!result.isEmpty()) { stateDirty = true; setChanged(); }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        stateDirty = true;
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(
            worldPosition.getX() + 0.5,
            worldPosition.getY() + 0.5,
            worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
        stateDirty = true;
        setChanged();
    }

    /** Slot-validation called by the framework when a player tries to place an item. */
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CUP  -> stack.getItem() instanceof CupItem;
            case SLOT_ING1 -> {
                if (MixerRecipes.getContribution(stack).isEmpty()) yield false;
                // Prevent placing the same item type as whatever is in ING2
                ItemStack ing2 = items.get(SLOT_ING2);
                yield ing2.isEmpty() || ing2.getItem() != stack.getItem();
            }
            case SLOT_ING2 -> {
                if (MixerRecipes.getContribution(stack).isEmpty()) yield false;
                ItemStack ing1 = items.get(SLOT_ING1);
                yield ing1.isEmpty() || ing1.getItem() != stack.getItem();
            }
            case SLOT_OUTPUT -> false;  // output-only
            default -> false;
        };
    }

    // ── NBT ────────────────────────────────────────────────────────────────

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        if (pendingShakeTag != null) {
            tag.put("PendingShake", pendingShakeTag.copy());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        pendingShakeTag = tag.contains("PendingShake", Tag.TAG_COMPOUND)
            ? tag.getCompound("PendingShake").copy()
            : null;
    }
}
