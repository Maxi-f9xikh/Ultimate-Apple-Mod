# Ultimate Apple Mod — Multi-Version Branches Design Spec
**Date:** 2026-05-26
**Scope:** Add Minecraft 1.20.4 and 1.21.1 support via Git branches

---

## 1. Strategie

Zwei unabhängige Git-Branches, beide aus `main` (1.20.1) erstellt:

| Branch | MC-Version | Plattformen | Java | Aufwand |
|--------|------------|-------------|------|---------|
| `1.20.4` | 1.20.4 | Fabric + Forge | 17 | ~2h |
| `1.21.1` | 1.21.1 | Fabric + NeoForge | 21 | ~1 Tag |

`main` bleibt unverändert als 1.20.1-Referenz. Bugfixes und Features werden per `git cherry-pick` von `main` in die Branches übertragen, falls nötig.

---

## 2. Branch `1.20.4`

### 2.1 Plattformen
- **Fabric** (wie 1.20.1, nur Versionen aktualisieren)
- **Forge** (wie 1.20.1, nur Versionen aktualisieren)

### 2.2 Änderungen in `gradle.properties`
```properties
mod_version = 1.20.4-1.0.0
minecraft_version = 1.20.4
architectury_api_version = <recherchieren: neueste für 1.20.4>
fabric_loader_version = <recherchieren: neueste für 1.20.4>
fabric_api_version = <recherchieren: neueste +1.20.4>
forge_version = <recherchieren: 49.x.x für 1.20.4>
```

### 2.3 Änderungen in Metadaten-Dateien
- `forge/src/main/resources/META-INF/mods.toml`: `loaderVersion` und `[[dependencies.ultimate_apple_mod]]`-Versionen anpassen
- `fabric/src/main/resources/fabric.mod.json`: `depends.fabricloader` und `depends.fabric` Versionen anpassen

### 2.4 Quellcode-Änderungen
Keine erwartet. 1.20.1 → 1.20.4 ist ein reiner Minor-Patch ohne Breaking Changes in den genutzten APIs. Falls der Build fehlschlägt, werden Fehler iterativ behoben.

### 2.5 Java-Version
Bleibt Java 17 — keine Änderung in `build.gradle`.

---

## 3. Branch `1.21.1`

### 3.1 Plattformen
- **Fabric** (Loader + API aktualisieren, Mixins prüfen)
- **NeoForge** (das bestehende `forge/`-Subprojekt wird auf NeoForge umgebaut — der Ordner bleibt `forge/` für minimale Umbenennung)

### 3.2 Java-Version
Java 17 → **Java 21** ist Pflicht für Minecraft 1.21.1.

In `build.gradle` (root):
```groovy
sourceCompatibility = JavaVersion.VERSION_21
targetCompatibility = JavaVersion.VERSION_21

tasks.withType(JavaCompile).configureEach {
    it.options.release = 21
}
```

### 3.3 Änderungen in `gradle.properties`
```properties
mod_version = 1.21.1-1.0.0
minecraft_version = 1.21.1
architectury_api_version = <recherchieren: neueste für 1.21.1>
fabric_loader_version = <recherchieren: neueste für 1.21.1>
fabric_api_version = <recherchieren: neueste +1.21.1>
neoforge_version = <recherchieren: 21.1.x für 1.21.1>
# forge_version entfernen (nicht mehr benötigt)
```

### 3.4 Umbau des `forge/`-Subprojekts auf NeoForge

**`forge/build.gradle`** — zentrale Änderungen:
```groovy
// ALT:
architectury { forge() }
// NEU:
architectury { neoForge() }

// ALT:
modImplementation "net.minecraftforge:forge:${rootProject.forge_version}"
// NEU:
modImplementation "net.neoforged:neoforge:${rootProject.neoforge_version}"
```

**`forge/src/main/resources/META-INF/mods.toml`** → umbenennen zu **`neoforge.mods.toml`**

NeoForge liest `neoforge.mods.toml` statt `mods.toml`. Inhalt bleibt weitgehend gleich, `modLoader="javafml"` → `modLoader="javafml"` (unverändert), aber `loaderVersion` und `dependencies` werden auf NeoForge angepasst.

### 3.5 Quellcode-Änderungen im `forge/`-Subprojekt

**Package-Imports:** NeoForge verschiebt Klassen in andere Packages:
- `net.minecraftforge.*` → `net.neoforged.*` (für NeoForge-spezifische Klassen)
- `net.minecraftforge.fml.common.Mod` → `net.neoforged.fml.common.Mod`
- `net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext` → `net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext`
- `net.minecraftforge.registries.*` → `net.neoforged.neoforge.registries.*`
- `net.minecraftforge.eventbus.*` → `net.neoforged.bus.*`
- `net.minecraftforge.client.event.*` → `net.neoforged.neoforge.client.event.*`
- `net.minecraftforge.event.*` → `net.neoforged.neoforge.event.*`

**`ultimate_apple_modForge.java`** — Konstruktor:
```java
// ALT:
IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
// NEU (NeoForge 21.1+):
IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
// (gleich, aber Import-Package ändert sich)
```

**Minecraft API-Änderungen 1.20.1 → 1.21.1** (bekannte Breaking Changes):
- `MobEffects.*` Konstanten sind jetzt `Holder<MobEffect>` statt direkte Instanzen → `MobEffectInstance(MobEffects.REGENERATION, ...)` bleibt syntaktisch gleich, aber intern als Holder
- `EntityEvent.Size` → prüfen ob API noch existiert in NeoForge 21.1
- `FoodProperties.Builder.effect()` — Lambda-Signatur prüfen
- `ThrownItemRenderer` → möglicherweise in anderem Package

### 3.6 Quellcode-Änderungen im `fabric/`-Subprojekt (1.21.1)

Erwartet minimal. Prüfen:
- `FabricPlayerSizeMixin` — Mixin-Target-Klasse und Methodensignatur in 1.21.1
- `FabricModClient` — Client-Init-Entrypoint-Signatur
- Fabric API: `net.fabricmc.fabric.api.*` Packages können sich leicht verschieben

---

## 4. Umsetzungsreihenfolge

Da die Branches unabhängig sind, können sie parallel bearbeitet werden. Empfohlene Reihenfolge:

1. **Branch `1.20.4` zuerst** — einfacher, bestätigt dass der Branch-Workflow funktioniert
2. **Branch `1.21.1` danach** — aufwändiger, profitiert nicht vom 1.20.4-Branch

---

## 5. Erfolgskriterien

### Branch `1.20.4`
- `./gradlew :fabric:build` → BUILD SUCCESSFUL
- `./gradlew :forge:build` → BUILD SUCCESSFUL
- Fabric dev run startet ohne Crash
- Forge dev run startet ohne Crash

### Branch `1.21.1`
- `./gradlew :fabric:build` → BUILD SUCCESSFUL
- `./gradlew :forge:build` → BUILD SUCCESSFUL (mit NeoForge-Backend)
- Fabric dev run startet ohne Crash
- Forge/NeoForge dev run startet ohne Crash
