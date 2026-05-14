# Add 4 new copper-apple tooltip lang keys to all 28 lang files.
# Inserts after "tooltip.ultimate_apple_mod.copper_apple.fully_oxidized".

$langDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\lang"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

# Localised translations per locale code.
# Defaults to English when not listed.
$translations = @{
    "de_de" = @{
        time_to_oxidize  = "Oxidiert in ca. 15-20 Min. (trocken)"
        oxidizes_slowly  = "Oxidiert langsam mit der Zeit."
        rain_doubles     = "Regen verdoppelt die Oxidationsgeschwindigkeit."
        wax_to_stop      = "Mit Bienenwabe wachsen, um die Oxidation zu stoppen."
    }
    "de_at" = @{
        time_to_oxidize  = "Oxidiert in ca. 15-20 Min. (trocken)"
        oxidizes_slowly  = "Oxidiert langsam mit der Zeit."
        rain_doubles     = "Regen verdoppelt die Oxidationsgeschwindigkeit."
        wax_to_stop      = "Mit Bienenwabe wachsen, um die Oxidation zu stoppen."
    }
    "de_ch" = @{
        time_to_oxidize  = "Oxidiert in ca. 15-20 Min. (trocken)"
        oxidizes_slowly  = "Oxidiert langsam mit der Zeit."
        rain_doubles     = "Regen verdoppelt die Oxidationsgeschwindigkeit."
        wax_to_stop      = "Mit Bienenwabe wachsen, um die Oxidation zu stoppen."
    }
    "es_es" = @{
        time_to_oxidize  = "Se oxida en ~15-20 min (seco)"
        oxidizes_slowly  = "Se oxida lentamente con el tiempo."
        rain_doubles     = "La lluvia duplica la velocidad de oxidacion."
        wax_to_stop      = "Aplica cera de panal para detener la oxidacion."
    }
    "es_ar" = @{
        time_to_oxidize  = "Se oxida en ~15-20 min (seco)"
        oxidizes_slowly  = "Se oxida lentamente con el tiempo."
        rain_doubles     = "La lluvia duplica la velocidad de oxidacion."
        wax_to_stop      = "Aplica cera de panal para detener la oxidacion."
    }
    "es_cl" = @{
        time_to_oxidize  = "Se oxida en ~15-20 min (seco)"
        oxidizes_slowly  = "Se oxida lentamente con el tiempo."
        rain_doubles     = "La lluvia duplica la velocidad de oxidacion."
        wax_to_stop      = "Aplica cera de panal para detener la oxidacion."
    }
    "es_ec" = @{
        time_to_oxidize  = "Se oxida en ~15-20 min (seco)"
        oxidizes_slowly  = "Se oxida lentamente con el tiempo."
        rain_doubles     = "La lluvia duplica la velocidad de oxidacion."
        wax_to_stop      = "Aplica cera de panal para detener la oxidacion."
    }
    "es_mx" = @{
        time_to_oxidize  = "Se oxida en ~15-20 min (seco)"
        oxidizes_slowly  = "Se oxida lentamente con el tiempo."
        rain_doubles     = "La lluvia duplica la velocidad de oxidacion."
        wax_to_stop      = "Aplica cera de panal para detener la oxidacion."
    }
    "es_uy" = @{
        time_to_oxidize  = "Se oxida en ~15-20 min (seco)"
        oxidizes_slowly  = "Se oxida lentamente con el tiempo."
        rain_doubles     = "La lluvia duplica la velocidad de oxidacion."
        wax_to_stop      = "Aplica cera de panal para detener la oxidacion."
    }
    "es_ve" = @{
        time_to_oxidize  = "Se oxida en ~15-20 min (seco)"
        oxidizes_slowly  = "Se oxida lentamente con el tiempo."
        rain_doubles     = "La lluvia duplica la velocidad de oxidacion."
        wax_to_stop      = "Aplica cera de panal para detener la oxidacion."
    }
    "fr_fr" = @{
        time_to_oxidize  = "S'oxyde en ~15-20 min (sec)"
        oxidizes_slowly  = "S'oxyde lentement avec le temps."
        rain_doubles     = "La pluie double la vitesse d'oxydation."
        wax_to_stop      = "Appliquer de la cire d'abeille pour stopper l'oxydation."
    }
    "fr_ca" = @{
        time_to_oxidize  = "S'oxyde en ~15-20 min (sec)"
        oxidizes_slowly  = "S'oxyde lentement avec le temps."
        rain_doubles     = "La pluie double la vitesse d'oxydation."
        wax_to_stop      = "Appliquer de la cire d'abeille pour stopper l'oxydation."
    }
    "pt_br" = @{
        time_to_oxidize  = "Oxida em ~15-20 min (seco)"
        oxidizes_slowly  = "Oxida lentamente com o tempo."
        rain_doubles     = "A chuva dobra a velocidade de oxidacao."
        wax_to_stop      = "Use cera de colmeia para parar a oxidacao."
    }
    "pt_pt" = @{
        time_to_oxidize  = "Oxida em ~15-20 min (seco)"
        oxidizes_slowly  = "Oxida lentamente com o tempo."
        rain_doubles     = "A chuva dobra a velocidade de oxidacao."
        wax_to_stop      = "Use cera de colmeia para parar a oxidacao."
    }
    "ru_ru" = @{
        time_to_oxidize  = "Okislyaetsya za ~15-20 min (suho)"
        oxidizes_slowly  = "Okislyaetsya medlenno so vremenem."
        rain_doubles     = "Dozhdj udvaivaet skorost okisleniya."
        wax_to_stop      = "Voskovoj sotoj ostanovit okislenie."
    }
    "ja_jp" = @{
        time_to_oxidize  = "~15-20 min de sanka shimasu (kanso)"
        oxidizes_slowly  = "Jikan to tomo ni sanka shimasu."
        rain_doubles     = "Ame wa sanka sokudo wo 2 bai ni shimasu."
        wax_to_stop      = "Honeycomb de waxtingu shite sanka wo tomeru."
    }
    "ko_kr" = @{
        time_to_oxidize  = "~15-20 bun anae sanhwa (geonjo si)"
        oxidizes_slowly  = "Sigan i jinamyeo cheoncheonhi sanhwa."
        rain_doubles     = "Biga sanhwa sogdoreul 2bae ro neullinda."
        wax_to_stop      = "Honeycomb euro wakseuhaeseo sanhwa reul meomchwo."
    }
    "zh_cn" = @{
        time_to_oxidize  = "~15-20 fen zhong hou yang hua (gan zao)"
        oxidizes_slowly  = "Sui shi jian man man yang hua."
        rain_doubles     = "Xia yu shi yang hua su du jia bei."
        wax_to_stop      = "Yong mi fang la tu mo lai zu zhi yang hua."
    }
    "zh_tw" = @{
        time_to_oxidize  = "~15-20 fen zhong hou yang hua (gan zao)"
        oxidizes_slowly  = "Sui shi jian man man yang hua."
        rain_doubles     = "Xia yu shi yang hua su du jia bei."
        wax_to_stop      = "Yong mi fang la tu mo lai zu zhi yang hua."
    }
}

$defaultTrans = @{
    time_to_oxidize  = "Oxidizes in ~15-20 min (dry)"
    oxidizes_slowly  = "Oxidizes slowly over time."
    rain_doubles     = "Rain doubles the oxidation speed."
    wax_to_stop      = "Wax with a Honeycomb to stop oxidation."
}

$anchor = '"tooltip.ultimate_apple_mod.copper_apple.fully_oxidized"'

$files = Get-ChildItem -Path $langDir -Filter "*.json"
$changed = 0

foreach ($file in $files) {
    $locale = [System.IO.Path]::GetFileNameWithoutExtension($file.Name)
    $content = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)

    # Skip if the new key already exists
    if ($content.Contains('"tooltip.ultimate_apple_mod.copper_apple.time_to_oxidize"')) {
        Write-Host "SKIP $($file.Name) (already has keys)"
        continue
    }

    # Pick translations
    $t = if ($translations.ContainsKey($locale)) { $translations[$locale] } else { $defaultTrans }

    $newLines = @"
,
  "tooltip.ultimate_apple_mod.copper_apple.time_to_oxidize": "$($t.time_to_oxidize)",
  "tooltip.ultimate_apple_mod.copper_apple.oxidizes_slowly": "$($t.oxidizes_slowly)",
  "tooltip.ultimate_apple_mod.copper_apple.rain_doubles": "$($t.rain_doubles)",
  "tooltip.ultimate_apple_mod.copper_apple.wax_to_stop": "$($t.wax_to_stop)"
"@

    # Find the fully_oxidized line and append new keys after it.
    # The line ends in a comma already in most files; we need to handle both cases.
    # Pattern: find the line with the anchor, and the rest of the value ending with ","
    # We'll insert BEFORE the next key line.

    # Strategy: find anchor line, replace trailing comma or lack thereof
    # to insert new block.
    # Simpler: replace the anchor key's full line (with its trailing comma if any)
    # by that line plus our new lines.

    # Find end of the fully_oxidized value line
    $idx = $content.IndexOf($anchor)
    if ($idx -lt 0) {
        Write-Host "WARN: anchor not found in $($file.Name)"
        continue
    }
    # Find end of this line
    $lineEnd = $content.IndexOf("`n", $idx)
    if ($lineEnd -lt 0) { $lineEnd = $content.Length }

    $lineContent = $content.Substring($idx, $lineEnd - $idx).TrimEnd()

    # Remove trailing comma from the current fully_oxidized line (we'll re-add via $newLines)
    $lineContentNoComma = $lineContent.TrimEnd(',')

    # The new block starts with a comma, so:
    #   <fully_oxidized line without comma><newLines>\n<rest>
    $before   = $content.Substring(0, $idx)
    $after    = $content.Substring($lineEnd)  # includes \n and rest

    $newContent = $before + $lineContentNoComma + $newLines + $after

    [System.IO.File]::WriteAllText($file.FullName, $newContent, $utf8NoBom)
    Write-Host "OK  $($file.Name)"
    $changed++
}

Write-Host "`nDone. Modified $changed files."
