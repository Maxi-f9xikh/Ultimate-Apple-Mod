# Adds JEED effect description translation keys to all lang files.
# JEED reads descriptions via lang key:  effect.<namespace>.<id>.description
# (not from any JSON data file — descriptions are purely lang-driven)

$langDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\lang"

# ── EN strings ──
$enKeys = [ordered]@{
    "effect.ultimate_apple_mod.lifesteal.description"       = "A portion of all melee damage dealt is returned to you as health. Higher amplifier levels increase the stolen fraction."
    "effect.ultimate_apple_mod.totem_protection.description"= "When a fatal blow is received, this effect absorbs it and grants a brief window of invincibility, keeping you alive with a small amount of health."
    "effect.ultimate_apple_mod.moon_gravity.description"    = "Reduces gravitational pull to about one-sixth of normal, letting you jump roughly three times higher and float slowly back down."
    "effect.ultimate_apple_mod.time_freeze.description"     = "Freezes all nearby mobs in place. Frozen mobs cannot move, attack, or deal damage until the effect expires."
    "effect.ultimate_apple_mod.curse_of_rotten.description" = "A curse from rotten apples. Shrinks your hitbox, raises movement speed unnaturally, and makes you uncontrollable for its duration."
}

# ── DE strings ──
$deKeys = [ordered]@{
    "effect.ultimate_apple_mod.lifesteal.description"       = "Ein Teil des ausgeteilten Nahkampfschadens wird dir als Gesundheit zurueckgegeben. Hoehere Verstaerker-Level erhoehen den gestohlenen Anteil."
    "effect.ultimate_apple_mod.totem_protection.description"= "Wenn ein toedlicher Schlag erhalten wird, absorbiert dieser Effekt ihn und gewaehrt ein kurzes Fenster der Unverwundbarkeit."
    "effect.ultimate_apple_mod.moon_gravity.description"    = "Reduziert die Schwerkraft auf etwa ein Sechstel des Normalen, sodass du etwa dreimal so hoch springen und langsam sinken kannst."
    "effect.ultimate_apple_mod.time_freeze.description"     = "Friert alle nahen Mobs ein. Eingefrorene Mobs koennen sich nicht bewegen, angreifen oder Schaden anrichten, bis der Effekt ablaeuft."
    "effect.ultimate_apple_mod.curse_of_rotten.description" = "Ein Fluch fauler Aepfel. Verkleinert deine Hitbox, erhoeht die Bewegungsgeschwindigkeit unnatuerlich und macht dich fuer seine Dauer unkontrollierbar."
}

function Build-Block([System.Collections.Specialized.OrderedDictionary]$keys) {
    ($keys.GetEnumerator() | ForEach-Object {
        "  `"$($_.Key)`": `"$($_.Value)`""
    }) -join ",`n"
}

$enBlock = Build-Block $enKeys
$deBlock = Build-Block $deKeys

$files = Get-ChildItem -Path $langDir -Filter "*.json"
$added = 0
$skipped = 0

foreach ($file in $files) {
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

    # Skip if already present (idempotent)
    if ($content -match 'effect\.ultimate_apple_mod\.lifesteal\.description') {
        Write-Host "  SKIP   $($file.Name)" -ForegroundColor Yellow
        $skipped++
        continue
    }

    $block = if ($file.BaseName -like 'de_*') { $deBlock } else { $enBlock }

    $lastBrace = $content.LastIndexOf('}')
    $before = $content.Substring(0, $lastBrace).TrimEnd()
    if (-not $before.EndsWith(',')) { $before += ',' }

    $newContent = $before + "`n$block`n}`n"
    [System.IO.File]::WriteAllText($file.FullName, $newContent, [System.Text.Encoding]::UTF8)
    Write-Host "  OK     $($file.Name)"
    $added++
}

Write-Host ""
Write-Host "Done. Added: $added  Skipped: $skipped"
