# Add JEI ingredient-info lang keys to all 28 lang files.
# Avoids em-dashes and non-ASCII chars in PS5.1 string literals.
# Uses [char]0xA7 for the Minecraft section sign.

$langDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\lang"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$s = [char]0xA7   # Minecraft section sign: §

# ── Keys for all locales (English defaults) ─────────────────────────────────
$en = [ordered]@{
    "jei.ultimate_apple_mod.totem_apple.info1"       = ("Grants " + $s + "6Totem Protection" + $s + "r - survives one fatal blow with brief invincibility.")
    "jei.ultimate_apple_mod.totem_apple.info2"       = ("Source: " + $s + "6Totem Apple" + $s + "r and " + $s + "6Quantum Apple" + $s + "r.")
    "jei.ultimate_apple_mod.wither_apple.info1"      = ("Grants " + $s + "cLifesteal" + $s + "r - a portion of melee damage dealt is returned as health.")
    "jei.ultimate_apple_mod.wither_apple.info2"      = ("Also provides Absorption III, Resistance II and Regeneration II.")
    "jei.ultimate_apple_mod.moon_apple.info1"        = ("Grants " + $s + "bMoon Gravity" + $s + "r - reduces gravity to ~1/6th of normal for 30 s.")
    "jei.ultimate_apple_mod.moon_apple.info2"        = ("Jump ~3x higher and descend slowly. Source: " + $s + "bMoon Apple" + $s + "r.")
    "jei.ultimate_apple_mod.time_freeze_apple.info1" = ("Grants " + $s + "3Time Freeze" + $s + "r - all nearby mobs are frozen in place for 30 s.")
    "jei.ultimate_apple_mod.time_freeze_apple.info2" = ("Frozen mobs cannot move or attack until the effect expires.")
    "jei.ultimate_apple_mod.rotten_apple.info1"      = ("Grants " + $s + "4Curse of Rotten" + $s + "r - shrinks hitbox and causes uncontrollable movement.")
    "jei.ultimate_apple_mod.rotten_apple.info2"      = ("Only obtainable as a rare drop from " + $s + "4Baby Zombies" + $s + "r. Cannot be crafted.")
    "jei.ultimate_apple_mod.quantum_apple.info1"     = ("Grants " + $s + "5ALL effects" + $s + "r from every apple in this mod simultaneously.")
    "jei.ultimate_apple_mod.quantum_apple.info2"     = ("Includes Totem Protection, Lifesteal, Moon Gravity, Time Freeze, Curse of Rotten and more.")
    "jei.ultimate_apple_mod.quantum_apple.info3"     = ("Crafted in the " + $s + "dMixer" + $s + "r - the ultimate apple.")
    "jei.ultimate_apple_mod.blaze_apple.info"        = ($s + "6Rare drop from Blazes" + $s + "r in the Nether (approx. 3% chance).")
    "jei.ultimate_apple_mod.ender_pearl_apple.info"  = ($s + "5Rare drop from Endermen" + $s + "r (approx. 3% chance).")
    "jei.ultimate_apple_mod.copper_apple.info1"      = ("Oxidises slowly over ~15-20 minutes in the player's inventory.")
    "jei.ultimate_apple_mod.copper_apple.info2"      = ("Each apple in a stack oxidises independently - one at a time.")
    "jei.ultimate_apple_mod.copper_apple.info3"      = ($s + "eWax with a Honeycomb" + $s + "r to freeze the current stage permanently.")
    "jei.ultimate_apple_mod.void_apple.info"         = ($s + "8Teleports" + $s + "r the player to a random location in the world.")
    "jei.ultimate_apple_mod.prism_apple.info"        = ($s + "3Prismarine-infused" + $s + "r apple granting a unique aquatic buff.")
    "jei.ultimate_apple_mod.nether_star_apple.info"  = ($s + "fCrafted from a Nether Star" + $s + "r - immensely powerful. Stack size: 1.")
    "jei.ultimate_apple_mod.echo_apple.info"         = ($s + "7Echoes" + $s + "r a nearby sound and grants special sculk-related properties.")
    "jei.ultimate_apple_mod.rewind_apple.info"       = ($s + "aReverts" + $s + "r the player's position and health to a few seconds in the past.")
}

# ── German translations ──────────────────────────────────────────────────────
$de = [ordered]@{
    "jei.ultimate_apple_mod.totem_apple.info1"       = ("Verleiht " + $s + "6Totemschutz" + $s + "r - uberlebt einen todlichen Treffer mit kurzer Unverwundbarkeit.")
    "jei.ultimate_apple_mod.totem_apple.info2"       = ("Quelle: " + $s + "6Totem-Apfel" + $s + "r und " + $s + "6Quanten-Apfel" + $s + "r.")
    "jei.ultimate_apple_mod.wither_apple.info1"      = ("Verleiht " + $s + "cLebensraub" + $s + "r - ein Teil des Nahkampfschadens wird als Gesundheit zuruckgegeben.")
    "jei.ultimate_apple_mod.wither_apple.info2"      = ("Gibt auch Absorption III, Resistenz II und Regeneration II.")
    "jei.ultimate_apple_mod.moon_apple.info1"        = ("Verleiht " + $s + "bMondgravitation" + $s + "r - reduziert Schwerkraft fur 30 s auf ca. 1/6.")
    "jei.ultimate_apple_mod.moon_apple.info2"        = ("Springe ca. 3x hoher und sinke langsam. Quelle: " + $s + "bMond-Apfel" + $s + "r.")
    "jei.ultimate_apple_mod.time_freeze_apple.info1" = ("Verleiht " + $s + "3Zeiteinfrierung" + $s + "r - alle nahen Monster werden fur 30 s eingefroren.")
    "jei.ultimate_apple_mod.time_freeze_apple.info2" = ("Eingefrorene Monster konnen sich weder bewegen noch angreifen.")
    "jei.ultimate_apple_mod.rotten_apple.info1"      = ("Verleiht " + $s + "4Fluch des Verfaulten" + $s + "r - verkleinert Hitbox und verursacht unkontrollierbare Bewegung.")
    "jei.ultimate_apple_mod.rotten_apple.info2"      = ("Nur als seltener Drop von " + $s + "4Baby-Zombies" + $s + "r erhaltlich. Nicht herstellbar.")
    "jei.ultimate_apple_mod.quantum_apple.info1"     = ("Verleiht " + $s + "5ALLE Effekte" + $s + "r aus jedem Apfel dieses Mods gleichzeitig.")
    "jei.ultimate_apple_mod.quantum_apple.info2"     = ("Enthalt Totemschutz, Lebensraub, Mondgravitation, Zeiteinfrierung, Fluch des Verfaulten u.v.m.")
    "jei.ultimate_apple_mod.quantum_apple.info3"     = ("Im " + $s + "dMixer" + $s + "r hergestellt - der ultimative Apfel.")
    "jei.ultimate_apple_mod.blaze_apple.info"        = ($s + "6Seltener Drop von Glutis" + $s + "r im Nether (ca. 3% Chance).")
    "jei.ultimate_apple_mod.ender_pearl_apple.info"  = ($s + "5Seltener Drop von Endermen" + $s + "r (ca. 3% Chance).")
    "jei.ultimate_apple_mod.copper_apple.info1"      = ("Oxidiert langsam uber ca. 15-20 Minuten im Inventar.")
    "jei.ultimate_apple_mod.copper_apple.info2"      = ("Jeder Apfel in einem Stapel oxidiert unabhangig - einer nach dem anderen.")
    "jei.ultimate_apple_mod.copper_apple.info3"      = ($s + "eMit Bienenwabe wachsen" + $s + "r, um die aktuelle Stufe dauerhaft einzufrieren.")
    "jei.ultimate_apple_mod.void_apple.info"         = ($s + "8Teleportiert" + $s + "r den Spieler an einen zufalligen Ort.")
    "jei.ultimate_apple_mod.prism_apple.info"        = ($s + "3Prismaringetrankt" + $s + "r - verleiht einen einzigartigen aquatischen Buff.")
    "jei.ultimate_apple_mod.nether_star_apple.info"  = ($s + "fAus einem Nether-Stern hergestellt" + $s + "r - extrem machtig. Stapel: 1.")
    "jei.ultimate_apple_mod.echo_apple.info"         = ($s + "7Hallt" + $s + "r einen nahen Klang wider und verleiht skulk-bezogene Eigenschaften.")
    "jei.ultimate_apple_mod.rewind_apple.info"       = ($s + "aSetzt" + $s + "r Position und Gesundheit einige Sekunden zuruck.")
}

$deLocales = @("de_de", "de_at", "de_ch", "bar")

function Build-Block([hashtable]$keys) {
    $lines = @()
    foreach ($k in $keys.Keys) {
        $v = $keys[$k] -replace '"', '\"' -replace "'", "''"
        $lines += "  `"$k`": `"$v`""
    }
    return ($lines -join ",`n")
}

$enBlock = Build-Block $en
$deBlock = Build-Block $de

$files = Get-ChildItem -Path $langDir -Filter "*.json"
$changed = 0

foreach ($file in $files) {
    $locale = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

    if ($content.Contains('"jei.ultimate_apple_mod.')) {
        Write-Host "SKIP $($file.Name)"
        continue
    }

    $block = if ($deLocales -contains $locale) { $deBlock } else { $enBlock }

    $lastBrace = $content.LastIndexOf("}")
    if ($lastBrace -lt 0) { Write-Host "WARN no brace in $($file.Name)"; continue }

    $before = $content.Substring(0, $lastBrace).TrimEnd()
    if (-not $before.EndsWith(",")) { $before += "," }
    $newContent = $before + "`n" + $block + "`n}"

    [System.IO.File]::WriteAllText($file.FullName, $newContent, $utf8NoBom)
    Write-Host "OK  $($file.Name)"
    $changed++
}

Write-Host "`nDone. Modified $changed files."
