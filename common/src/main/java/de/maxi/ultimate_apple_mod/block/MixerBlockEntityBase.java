package de.maxi.ultimate_apple_mod.block;

import de.maxi.ultimate_apple_mod.ModRegistries;
import de.maxi.ultimate_apple_mod.item.CupItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import org.jetbrains.annotations.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class MixerBlockEntityBase extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_CUP    = 0;
    public static final int SLOT_ING1   = 1;
    public static final int SLOT_ING2   = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int MAX_PROGRESS = 100;

    protected final NonNullList<ItemStack> items =
            NonNullList.withSize(4, ItemStack.EMPTY);

    @Nullable
    protected CompoundTag pendingShakeTag = null;

    int progress = 0;
    protected boolean stateDirty = false;

    final ContainerData data = new ContainerData() {
        @Override public int get(int i)         { return i == 0 ? progress : MAX_PROGRESS; }
        @Override public void set(int i, int v) { if (i == 0) progress = v; }
        @Override public int getCount()         { return 2; }
    };

    protected MixerBlockEntityBase(BlockPos pos, BlockState state) {
        super(ModRegistries.MIXER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ultimate_apple_mod.mixer");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MixerMenu(id, inv, this, this.data);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixerBlockEntityBase be) {
        if (be.pendingShakeTag != null) {
            boolean cupPresent  = be.items.get(SLOT_CUP).getItem() instanceof CupItem;
            boolean ing1Present = !be.items.get(SLOT_ING1).isEmpty();
            boolean ing2Present = !be.items.get(SLOT_ING2).isEmpty();
            if (!cupPresent || !ing1Present || !ing2Present) {
                be.pendingShakeTag = null;
                be.progress = 0;
                be.stateDirty = true;
                be.setChanged();
                return;
            }

            be.progress++;
            if (be.progress >= MAX_PROGRESS) {
                be.consumeSlot(SLOT_CUP);
                be.consumeSlot(SLOT_ING1);
                be.consumeSlot(SLOT_ING2);

                ItemStack shake = new ItemStack(ModRegistries.SHAKE_ITEM.get());
                shake.setTag(be.pendingShakeTag.copy());
                be.items.set(SLOT_OUTPUT, shake);
                be.pendingShakeTag = null;
                be.progress = 0;
                be.stateDirty = true;
            }
            be.setChanged();
        } else {
            if (be.canStartMix()) {
                MixerRecipes.ShakeContribution c1 =
                        MixerRecipes.getContribution(be.items.get(SLOT_ING1)).get();
                MixerRecipes.ShakeContribution c2 =
                        MixerRecipes.getContribution(be.items.get(SLOT_ING2)).get();

                ResourceLocation quantumId = new ResourceLocation("ultimate_apple_mod", "quantum_apple");
                var rng = level.getRandom();
                List<MixerRecipes.ShakeContribution> pool = MixerRecipes.getRandomizableContributions();
                if (!pool.isEmpty()) {
                    if (quantumId.equals(BuiltInRegistries.ITEM.getKey(be.items.get(SLOT_ING1).getItem())))
                        c1 = pool.get(rng.nextInt(pool.size()));
                    if (quantumId.equals(BuiltInRegistries.ITEM.getKey(be.items.get(SLOT_ING2).getItem())))
                        c2 = pool.get(rng.nextInt(pool.size()));
                }

                ResourceLocation COAL_ID = new ResourceLocation("ultimate_apple_mod", "coal_apple");
                ResourceLocation TNT_ID  = new ResourceLocation("ultimate_apple_mod", "tnt_apple");
                ResourceLocation BOMB_ID = new ResourceLocation("ultimate_apple_mod", "apple_bomb");
                ResourceLocation id1 = BuiltInRegistries.ITEM.getKey(be.items.get(SLOT_ING1).getItem());
                ResourceLocation id2 = BuiltInRegistries.ITEM.getKey(be.items.get(SLOT_ING2).getItem());

                boolean coalTnt  = (COAL_ID.equals(id1) && TNT_ID.equals(id2)) || (TNT_ID.equals(id1) && COAL_ID.equals(id2));
                boolean coalBomb = (COAL_ID.equals(id1) && BOMB_ID.equals(id2)) || (BOMB_ID.equals(id1) && COAL_ID.equals(id2));
                boolean coalMix  = COAL_ID.equals(id1) || COAL_ID.equals(id2);

                if (coalTnt) {
                    if (COAL_ID.equals(id1)) c1 = emptyContribution();
                    else                     c2 = emptyContribution();
                } else if (coalBomb) {
                    if (COAL_ID.equals(id1)) c1 = withDoubledDuration(c1);
                    else                     c2 = withDoubledDuration(c2);
                }

                CompoundTag shakeTag = buildShakeNbt(c1, c2);
                if (coalMix && !coalTnt) shakeTag.putBoolean("isCoalFuel", true);
                be.pendingShakeTag = shakeTag;
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

    private boolean canStartMix() {
        if (pendingShakeTag != null) return false;
        if (!items.get(SLOT_OUTPUT).isEmpty()) return false;
        ItemStack cup  = items.get(SLOT_CUP);
        ItemStack ing1 = items.get(SLOT_ING1);
        ItemStack ing2 = items.get(SLOT_ING2);
        if (!(cup.getItem() instanceof CupItem)) return false;
        if (ing1.isEmpty() || ing2.isEmpty()) return false;
        if (ing1.getItem() == ing2.getItem()) return false;
        if (MixerRecipes.areIncompatible(ing1, ing2)) return false;
        return MixerRecipes.getContribution(ing1).isPresent()
                && MixerRecipes.getContribution(ing2).isPresent();
    }

    private static CompoundTag buildShakeNbt(MixerRecipes.ShakeContribution c1,
                                             MixerRecipes.ShakeContribution c2) {
        boolean clearsEffects = c1.clearsEffects() || c2.clearsEffects();
        Map<ResourceLocation, MixerRecipes.EffectData> merged = new LinkedHashMap<>();
        if (clearsEffects) {
            if (c1.clearsEffects()) for (MixerRecipes.EffectData e : c1.effects()) mergeEffect(merged, e);
            if (c2.clearsEffects()) for (MixerRecipes.EffectData e : c2.effects()) mergeEffect(merged, e);
        } else {
            for (MixerRecipes.EffectData e : c1.effects()) mergeEffect(merged, e);
            for (MixerRecipes.EffectData e : c2.effects()) mergeEffect(merged, e);
        }
        int dragonCharges  = clearsEffects ? 0 : (c1.dragonCharges() + c2.dragonCharges());
        boolean lifesteal    = !clearsEffects && (c1.lifesteal()    || c2.lifesteal());
        boolean witherCurse  = !clearsEffects && (c1.witherCurse()  || c2.witherCurse());
        boolean voidLaunch   = !clearsEffects && (c1.voidLaunch()   || c2.voidLaunch());
        boolean rewindEffect = !clearsEffects && (c1.rewindEffect() || c2.rewindEffect());
        boolean orchardSpawn = !clearsEffects && (c1.orchardSpawn() || c2.orchardSpawn());
        boolean enderTeleport = !clearsEffects && (c1.enderTeleport() || c2.enderTeleport());
        boolean isBomb = c1.isBomb() || c2.isBomb();
        boolean isTntExplosion = c1.isTntExplosion() || c2.isTntExplosion();
        double rawMultiplier = Math.max(c1.durationMultiplier(), c2.durationMultiplier());
        final double durationFactor;
        if (clearsEffects && rawMultiplier >= 2.0) { merged.clear(); durationFactor = 1.0; }
        else if (clearsEffects) { durationFactor = 0.80; }
        else { durationFactor = 1.20 * rawMultiplier; }

        CompoundTag tag = new CompoundTag();
        ListTag effectsList = new ListTag();
        for (MixerRecipes.EffectData e : merged.values()) {
            CompoundTag et = new CompoundTag();
            et.putString("id", e.id().toString());
            et.putInt("duration", (int)(e.duration() * durationFactor));
            et.putInt("amplifier", e.amplifier());
            effectsList.add(et);
        }
        tag.put("effects", effectsList);
        tag.putInt("dragonCharges", dragonCharges);
        tag.putBoolean("lifesteal", lifesteal);
        tag.putBoolean("witherCurse", witherCurse);
        tag.putBoolean("clearsEffects", clearsEffects);
        tag.putBoolean("voidLaunch", voidLaunch);
        tag.putBoolean("rewindEffect", rewindEffect);
        tag.putBoolean("orchardSpawn", orchardSpawn);
        tag.putBoolean("enderTeleport", enderTeleport);
        tag.putBoolean("isBomb", isBomb);
        tag.putBoolean("isTntExplosion", isTntExplosion);
        return tag;
    }

    private static MixerRecipes.ShakeContribution emptyContribution() {
        return new MixerRecipes.ShakeContribution(
                List.of(), 0, false, false, false, 1.0,
                false, false, false, false, false, false);
    }

    private static MixerRecipes.ShakeContribution withDoubledDuration(MixerRecipes.ShakeContribution c) {
        List<MixerRecipes.EffectData> doubled = c.effects().stream()
                .map(e -> new MixerRecipes.EffectData(e.id(), e.duration() * 2, e.amplifier()))
                .collect(java.util.stream.Collectors.toList());
        return new MixerRecipes.ShakeContribution(doubled,
                c.dragonCharges(), c.lifesteal(), c.witherCurse(),
                c.clearsEffects(), c.durationMultiplier(),
                c.voidLaunch(), c.rewindEffect(), c.orchardSpawn(),
                c.enderTeleport(), c.isBomb(), c.isTntExplosion());
    }

    private static void mergeEffect(Map<ResourceLocation, MixerRecipes.EffectData> map,
                                    MixerRecipes.EffectData e) {
        if (map.containsKey(e.id())) {
            MixerRecipes.EffectData ex = map.get(e.id());
            map.put(e.id(), new MixerRecipes.EffectData(e.id(),
                    Math.max(ex.duration(), e.duration()), Math.max(ex.amplifier(), e.amplifier())));
        } else {
            map.put(e.id(), e);
        }
    }

    protected void consumeSlot(int slot) {
        ItemStack stack = items.get(slot);
        stack.shrink(1);
        if (stack.isEmpty()) items.set(slot, ItemStack.EMPTY);
    }

    private void syncBlockState(Level level, BlockPos pos, BlockState state) {
        boolean hasJar   = (pendingShakeTag != null) || !items.get(SLOT_CUP).isEmpty()
                         || !items.get(SLOT_ING1).isEmpty() || !items.get(SLOT_ING2).isEmpty();
        boolean hasShake = !items.get(SLOT_OUTPUT).isEmpty();
        BlockState newState = state
                .setValue(MixerBlock.HAS_JAR,   hasJar)
                .setValue(MixerBlock.HAS_SHAKE, hasShake);
        if (!newState.equals(state)) level.setBlock(pos, newState, 3);
    }

    @Override public int  getContainerSize()           { return items.size(); }
    @Override public boolean isEmpty()                 { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot)       { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int count) {
        ItemStack r = ContainerHelper.removeItem(items, slot, count);
        if (!r.isEmpty()) { stateDirty = true; setChanged(); }
        return r;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        stateDirty = true; setChanged();
    }
    @Override public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5) <= 64.0;
    }
    @Override public void clearContent() { items.clear(); stateDirty = true; setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CUP    -> stack.getItem() instanceof CupItem && items.get(SLOT_CUP).isEmpty()
                             && items.get(SLOT_OUTPUT).isEmpty() && pendingShakeTag == null;
            case SLOT_ING1   -> {
                if (MixerRecipes.getContribution(stack).isEmpty()) yield false;
                ItemStack ing2 = items.get(SLOT_ING2);
                yield ing2.isEmpty() || (ing2.getItem() != stack.getItem() && !MixerRecipes.areIncompatible(stack, ing2));
            }
            case SLOT_ING2   -> {
                if (MixerRecipes.getContribution(stack).isEmpty()) yield false;
                ItemStack ing1 = items.get(SLOT_ING1);
                yield ing1.isEmpty() || (ing1.getItem() != stack.getItem() && !MixerRecipes.areIncompatible(stack, ing1));
            }
            default -> false;
        };
    }

    @Override public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt("Progress", progress);
        if (pendingShakeTag != null) tag.put("PendingShake", pendingShakeTag.copy());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);
        ContainerHelper.loadAllItems(tag, items);
        progress = tag.getInt("Progress");
        pendingShakeTag = tag.contains("PendingShake", Tag.TAG_COMPOUND)
                ? tag.getCompound("PendingShake").copy() : null;
    }
}
