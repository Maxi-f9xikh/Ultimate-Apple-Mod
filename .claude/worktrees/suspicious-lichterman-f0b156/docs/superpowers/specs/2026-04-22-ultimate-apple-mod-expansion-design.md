# Ultimate Apple Mod — Expansion Design Spec
**Date:** 2026-04-22  
**Mod:** Ultimate Apple Mod | Forge 1.20.1  
**Scope:** 5 neue Äpfel, Baby-Zombie-Rotten-Apple, Effekte für 6 bestehende Äpfel, No-Texture-Tinting für Copper Apple, Lang-Datei-Updates

---

## 1. Architektur-Entscheidung

**Ansatz A — Custom Class pro Apfel** wurde gewählt.

- Mechanisch komplexe Äpfel → eigene `Item`-Subklasse (in `forge/src/main/java/de/maxi/ultimate_apple_mod/item/`)
- Zeitbegrenzte Zustands-Effekte → eigene `MobEffect`-Subklasse (in `forge/src/main/java/de/maxi/ultimate_apple_mod/effect/`)
- Einfache Effekt-Ergänzungen an bestehenden Äpfeln → Änderung der `FoodProperties` in `ultimate_apple_modForge.java`
- Alle neuen Items werden in `ultimate_apple_modForge.java` registriert (DeferredRegister-Pattern wie bisher)

---

## 2. No-Texture-Strategie

Keine neuen PNG-Dateien. Alle neuen Äpfel sowie der Copper Apple nutzen:

**Modell-JSON** (in `assets/ultimate_apple_mod/models/item/<name>.json`):
```json
{
  "parent": "item/generated",
  "textures": { "layer0": "minecraft:item/apple" }
}
```

**IItemColor-Registration** in `ModClient.java` via `RegisterColorHandlersEvent.Item`:
```java
event.getItemColors().register((stack, tintIndex) -> 0xRRGGBB, ModItems.ITEM.get());
```

**Farbtabelle:**
| Item | Farbe | Hex |
|---|---|---|
| Gravity Apple | Violett | `0x9B00FF` |
| Orchard Caller | Hellgrün | `0x44CC44` |
| Echo Apple | Cyan | `0x00CCDD` |
| Glitch Apple | Matrix-Grün | `0x00FF41` |
| Apple Bomb | Orange-Rot | `0xFF6600` |
| Copper Apple | Kupfer | `0xB87333` |

---

## 3. Neue Apple Items

### 3.1 Gravity Apple

**Registrierungs-ID:** `gravity_apple`  
**Klassen:** `GravityAppleItem.java`, `GravityEffect.java`  
**Nutrition:** 6 | **Saturation:** 0.6f | **alwaysEat:** true

**Mechanik:**
- Verzehr wendet den `GRAVITY_INVERSION`-Effekt für 200 Ticks (10 Sekunden) an
- `GravityEffect extends MobEffect`:
  - `addAttributeModifiers()` → `entity.setNoGravity(true)`
  - `removeAttributeModifiers()` → `entity.setNoGravity(false)`
  - `isDurationEffectTick()` → `return true` (jeden Tick)
  - `applyEffectTick()` → `entity.setDeltaMovement(dx, Math.min(dy + 0.08, 0.4), dz)` (Aufwärts-Beschleunigung wie invertiertes Fallen, max. Terminal-Velocity 0.4)
  - Nur Server-Side: `if (!entity.level().isClientSide())`

**Acquisition:** Crafting (Shaped) — 8x Phantom Membrane rund um 1 Apple

**Partikel & Sound:**
- `ParticleTypes.REVERSE_PORTAL` am Spieler-Standort beim Effekt-Start
- `SoundEvents.PHANTOM_FLAP` beim Verzehr

**Modell:** `gravity_apple.json` (vanilla apple + Tint `0x9B00FF`)  
**Lang:** `"item.ultimate_apple_mod.gravity_apple"` → "Gravity Apple" / "Schwerkraft-Apfel"

---

### 3.2 Orchard Caller

**Registrierungs-ID:** `orchard_caller`  
**Klasse:** `OrchardCallerItem.java`  
**Nutrition:** 4 | **Saturation:** 0.4f | **alwaysEat:** true

**Mechanik (`finishUsingItem` auf ServerLevel):**
1. Prüfe ob auf Bodenhöhe (y-1 des Spielers) innerhalb eines horizontalen 5-Block-Radius mind. 1 solider Block existiert. Wenn nicht (Spieler schwebt, im Wasser etc.) → return early, kein Verbrauch, Action-Bar: *"Hier kann nichts wachsen."*
2. Wähle 4 zufällige Positionen im Radius 2–5 Blöcke um den Spieler (horizontal, auf Bodenhöhe)
3. Für jede Position:
   - Prüfe: Untergrundblock solid, mindestens 3 Luft-Blöcke darüber
   - Wenn Untergrundblock kein `#minecraft:dirt` oder `GRASS_BLOCK`: setze `Blocks.GRASS_BLOCK` an dieser Position
   - Setze `Blocks.OAK_SAPLING.defaultBlockState()` einen Block darüber
   - Rufe `((SaplingBlock) Blocks.OAK_SAPLING).performBonemeal(serverLevel, random, saplingPos, saplingState)` auf
4. Spawn `ParticleTypes.HAPPY_VILLAGER` an jeder Baum-Position

**Acquisition:** Crafting (Shaped) — 4x Oak Sapling + 4x Bone Meal + 1 Apple (center)

**Modell:** `orchard_caller.json` (vanilla apple + Tint `0x44CC44`)  
**Lang:** `"item.ultimate_apple_mod.orchard_caller"` → "Orchard Caller" / "Obstgarten-Rufer"

---

### 3.3 Echo Apple

**Registrierungs-ID:** `echo_apple`  
**Klasse:** `EchoAppleItem.java`  
**Nutrition:** 5 | **Saturation:** 0.5f | **alwaysEat:** true

**Mechanik (`finishUsingItem`):**
- **Erster Verzehr** (kein `"echo_apple_pos"` in `player.getPersistentData()`):
  - Speichere CompoundTag `{x: double, y: double, z: double, dim: String}` in `player.getPersistentData()`
  - Action-Bar: *"Rückkehrpunkt gesetzt!"*
  - Partikel: `ParticleTypes.SCULK_SOUL` am Spieler-Standort
- **Zweiter Verzehr** (Tag vorhanden):
  - Lese Koordinaten aus NBT
  - Dimension-Check: Nur teleportieren wenn Spieler in derselben Dimension (sonst Action-Bar: *"Dimensionswechsel nicht möglich."* — Item wird **nicht verbraucht**, Marker bleibt erhalten)
  - `player.teleportTo(x, y, z)` + Sound `SoundEvents.ENDERMAN_TELEPORT`
  - Partikel: `ParticleTypes.PORTAL` am Abgangs- und Zielpunkt
  - Entferne `"echo_apple_pos"` aus persistentData

**Acquisition:** Crafting (Shaped) — 4x Echo Shard + 4x Amethyst Shard + 1 Apple (center)

**Modell:** `echo_apple.json` (vanilla apple + Tint `0x00CCDD`)  
**Lang:** `"item.ultimate_apple_mod.echo_apple"` → "Echo Apple" / "Echo-Apfel"

---

### 3.4 Glitch Apple

**Registrierungs-ID:** `glitch_apple`  
**Klassen:** `GlitchAppleItem.java`, `GlitchEffect.java`  
**Nutrition:** 4 | **Saturation:** 0.3f | **alwaysEat:** true

**Mechanik:**
- Verzehr wendet `GLITCH`-Effekt für 100 Ticks (5 Sekunden) an
- `GlitchEffect extends MobEffect`:
  - `addAttributeModifiers()` → speichere Eintrittsposition als NBT `"glitch_entry_pos"` in `entity.getPersistentData()`, setze `entity.noPhysics = true`
  - `isDurationEffectTick()` → `return true`
  - `applyEffectTick()`:
    1. `entity.noPhysics = true` (sicherheitshalber jeden Tick)
    2. Berechne Distanz zwischen aktuellem Standort und `glitch_entry_pos`
    3. Wenn `entity.isInWall()` und Distanz > 3.0 → Abbruch: `entity.noPhysics = false`, teleportiere Spieler zurück zu Eintrittsposition, entferne Effekt via `entity.removeEffect(this)`
  - `removeAttributeModifiers()`:
    1. `entity.noPhysics = false`
    2. Sicherheits-Extraktion: Wenn `entity.isInWall()` → scanne 3 Blöcke vertikal nach freiem Luft-Slot, teleportiere dorthin
    3. Entferne `"glitch_entry_pos"` aus persistentData

**Dicken-Check:** Distanz zwischen `glitch_entry_pos` und aktuellem Standort > 3.0 Blöcke während Spieler in Wand ist → automatischer Abbruch + Rückteleport zu Eintrittsposition. Wand ≤ 2 Blöcke Tiefe → normales Durchlaufen. `entity.removeEffect()` wird nicht mid-tick aufgerufen; stattdessen wird ein `pendingGlitchCancel`-Flag im persistentData gesetzt, das am Ende des Ticks ausgewertet wird.

**Acquisition:** Loot-Only — Ancient City Truhen (25% Chance). Kein Crafting-Rezept. Loot-Modifier-JSON: `EPA_Ancient_City.json`

**Partikel:** `ParticleTypes.ELECTRIC_SPARK` am Spieler jeden Tick während Effekt aktiv (Client-Side Event)

**Modell:** `glitch_apple.json` (vanilla apple + Tint `0x00FF41`)  
**Lang:** `"item.ultimate_apple_mod.glitch_apple"` → "Glitch Apple" / "Glitch-Apfel"

---

### 3.5 Apple Bomb

**Registrierungs-ID:** `apple_bomb`  
**Klassen:** `AppleBombItem.java`, `AppleBombEntity.java`  
**Kein Food-Item** — `use()` Override (kein `finishUsingItem`)

**Mechanik:**
- `AppleBombItem.use()`: Erstellt `new AppleBombEntity(player, level)`, `level.addFreshEntity(...)`, spielt `SoundEvents.SNOWBALL_THROW` ab, reduziert Stack um 1
- `AppleBombEntity extends ThrowableProjectile`:
  - Standardwurf-Physik (wie Snowball)
  - `onHitEntity(EntityHitResult)` + `onHitBlock(BlockHitResult)` → beide rufen `explode()` auf
  - `explode()`: findet alle `LivingEntity` im Radius 4 Blöcke, wendet an:
    - Regeneration I (200 Ticks / 10s)
    - Speed I (100 Ticks / 5s)
    - Strength I (60 Ticks / 3s)
  - Partikel beim Aufprall: `ParticleTypes.CRIT` + `ParticleTypes.HAPPY_VILLAGER`
  - Sound: `SoundEvents.GENERIC_EXPLODE` (leise, pitch 2.0 für hohen Ton)
- `AppleBombEntity` muss als `EntityType` registriert werden in `ultimate_apple_modForge.java`

**Acquisition:** Crafting (Shaped) — Ergibt 2x Apple Bomb:
```
G G G
G B G   G = Gunpowder (7x), B = Blaze Powder (1x), A = Diamond Apple (1x)
G A G
```

**Modell:** `apple_bomb.json` (vanilla apple + Tint `0xFF6600`)  
**Lang:** `"item.ultimate_apple_mod.apple_bomb"` → "Apple Bomb" / "Apfel-Bombe"

---

## 4. Rotten Apple — Baby-Zombie-Modus

**Geänderte Dateien:** `CurseOfRotten.java`, `ModClient.java`, neuer Event-Handler

Der Rotten Apple selbst wird nicht als Custom-Class refaktoriert. Der `CURSE_OF_ROTTEN`-Effekt bekommt zwei neue Hooks:

### 4.1 Visuelles Schrumpfen (Client)

In `ModClient.java`, neuer `@SubscribeEvent`:
```java
@SubscribeEvent
public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
    if (event.getEntity().hasEffect(ModEffects.CURSE_OF_ROTTEN.get())) {
        event.getPoseStack().scale(0.5f, 0.5f, 0.5f);
    }
}
```
Spieler rendert halb so groß (Füße bleiben auf Bodenhöhe da Origin = Füße).

### 4.2 Hitbox-Verkleinerung (Server)

Neuer `@SubscribeEvent` für `EntityEvent.Size` (in einem neuen Event-Handler oder in der bestehenden Event-Klasse):
```java
@SubscribeEvent
public static void onEntitySize(EntityEvent.Size event) {
    if (event.getEntity() instanceof Player player 
        && player.hasEffect(ModEffects.CURSE_OF_ROTTEN.get())) {
        event.setNewSize(EntityDimensions.scalable(0.3f, 0.9f));
    }
}
```
Spieler-Hitbox: 0.3 Breite, 0.9 Höhe (statt 0.6/1.8) → passt durch 1-Block-Lücken.

### 4.3 Partikel beim Effekt-Start

`CurseOfRotten.java` — Override `addAttributeModifiers()`:  
Nach dem `super`-Call: Spawn `ParticleTypes.WITCH` in einem Kreis um den Spieler (5–6 Partikel auf Server-Side via `ServerLevel.sendParticles()`).

**Bestehende Attribute bleiben unverändert:**
- MOVEMENT_SPEED +60%, MAX_HEALTH +20, ATTACK_SPEED +2

---

## 5. Bestehende Äpfel — Effekt-Ergänzungen

Änderungen in `ultimate_apple_modForge.java`, FoodProperties.Builder der jeweiligen Items:

| Item | Hinzugefügte Effekte | Dauer | Amplifier |
|---|---|---|---|
| `lapislazuli_apple` | Luck | 600t (30s) | 1 |
| `emerald_apple` | Luck + Night Vision | 1200t / 600t | 1 / 0 |
| `redstone_apple` | Speed + Haste | 400t / 400t | 1 / 0 |
| `netherite_apple` | Fire Resistance + Resistance + Strength | 1200t / 600t / 300t | 0 / 1 / 0 |
| `pear_apple` | Regeneration + Saturation | 300t / 100t | 0 / 0 |
| `copper_apple` | Haste + Water Breathing | 400t / 600t | 1 / 0 |

---

## 6. Copper Apple — No-Texture Update

Der `copper_apple` war bereits registriert mit eigener PNG. Er bekommt:
- Modell-JSON Update: `layer0` → `minecraft:item/apple` (vanilla Basis)
- IItemColor-Eintrag in `ModClient.java`: Tint `0xB87333`
- Die alte `copper_apple.png` kann entfernt werden (optional, da No-Texture-Strategie)

---

## 7. Lang-Datei Updates

### Neue Keys (alle Sprachen):
```
item.ultimate_apple_mod.gravity_apple
item.ultimate_apple_mod.orchard_caller
item.ultimate_apple_mod.echo_apple
item.ultimate_apple_mod.glitch_apple
item.ultimate_apple_mod.apple_bomb
effect.ultimate_apple_mod.gravity_inversion
effect.ultimate_apple_mod.glitch
tooltip.ultimate_apple_mod.echo_apple.line1
tooltip.ultimate_apple_mod.glitch_apple.line1
tooltip.ultimate_apple_mod.apple_bomb.line1
```

### Vollständige en_us.json Ergänzungen:
```json
"item.ultimate_apple_mod.gravity_apple": "Gravity Apple",
"item.ultimate_apple_mod.orchard_caller": "Orchard Caller",
"item.ultimate_apple_mod.echo_apple": "Echo Apple",
"item.ultimate_apple_mod.glitch_apple": "Glitch Apple",
"item.ultimate_apple_mod.apple_bomb": "Apple Bomb",
"effect.ultimate_apple_mod.gravity_inversion": "Gravity Inversion",
"effect.ultimate_apple_mod.glitch": "Glitch",
"tooltip.ultimate_apple_mod.echo_apple.line1": "§6First use sets a return point.",
"tooltip.ultimate_apple_mod.echo_apple.line2": "§7Second use teleports you back.",
"tooltip.ultimate_apple_mod.glitch_apple.line1": "§6Phase through walls for 5 seconds.",
"tooltip.ultimate_apple_mod.glitch_apple.line2": "§7Max wall thickness: 2 blocks.",
"tooltip.ultimate_apple_mod.apple_bomb.line1": "§6Throw to splash nearby entities with power."
```

### de_de.json Ergänzungen:
```json
"item.ultimate_apple_mod.gravity_apple": "Schwerkraft-Apfel",
"item.ultimate_apple_mod.orchard_caller": "Obstgarten-Rufer",
"item.ultimate_apple_mod.echo_apple": "Echo-Apfel",
"item.ultimate_apple_mod.glitch_apple": "Glitch-Apfel",
"item.ultimate_apple_mod.apple_bomb": "Apfel-Bombe",
"effect.ultimate_apple_mod.gravity_inversion": "Schwerkraft-Umkehr",
"effect.ultimate_apple_mod.glitch": "Glitch",
"tooltip.ultimate_apple_mod.echo_apple.line1": "§6Erster Verzehr setzt einen Rückkehrpunkt.",
"tooltip.ultimate_apple_mod.echo_apple.line2": "§7Zweiter Verzehr teleportiert zurück.",
"tooltip.ultimate_apple_mod.glitch_apple.line1": "§6Gehe 5 Sekunden durch Wände.",
"tooltip.ultimate_apple_mod.glitch_apple.line2": "§7Maximale Wanddicke: 2 Blöcke.",
"tooltip.ultimate_apple_mod.apple_bomb.line1": "§6Werfen verleiht Effekte im Umkreis."
```

Alle anderen Sprachdateien erhalten die englischen Werte als Fallback.

---

## 8. Neue Dateien (Übersicht)

### Java
- `item/GravityAppleItem.java`
- `item/OrchardCallerItem.java`
- `item/EchoAppleItem.java`
- `item/GlitchAppleItem.java`
- `item/AppleBombItem.java`
- `item/AppleBombEntity.java`
- `effect/GravityEffect.java`
- `effect/GlitchEffect.java`

### Ressourcen
- `models/item/gravity_apple.json`
- `models/item/orchard_caller.json`
- `models/item/echo_apple.json`
- `models/item/glitch_apple.json`
- `models/item/apple_bomb.json`
- `data/ultimate_apple_mod/recipes/gravity_apple.json`
- `data/ultimate_apple_mod/recipes/orchard_caller.json`
- `data/ultimate_apple_mod/recipes/echo_apple.json`
- `data/ultimate_apple_mod/recipes/apple_bomb.json`
- `data/ultimate_apple_mod/loot_modifiers/glitch_apple_ancient_city.json`

### Modifizierte Dateien
- `forge/ultimate_apple_modForge.java` (Item/Effect/EntityType Registry)
- `forge/ModClient.java` (IItemColor + RenderPlayerEvent + EntityEvent.Size)
- `effect/CurseOfRotten.java` (addAttributeModifiers Partikel)
- `assets/.../lang/en_us.json` + alle anderen Lang-Dateien

---

## 9. Offene Fragen / Bekannte Risiken

- `entity.noPhysics` in Forge 1.20.1: Feld ist möglicherweise nicht direkt public zugänglich → Access Transformer (`forge_at.cfg`) könnte nötig sein
- `EntityEvent.Size`: Muss getestet werden ob Hitbox-Änderung korrekt auf Client synchronisiert wird
- `AppleBombEntity` benötigt Entity-Type-Registration + Entity-Renderer auf Client (kann als unsichtbar/invisible gerendert werden, da das Item-Modell als Projektil-Renderer verwendet wird via `ItemEntityRenderer` oder `ThrownItemRenderer`)
