# Phase 4 lang key insertion -- copper oxidation system
# Uses explicit UTF-8 without BOM for all read/write operations

$langDir = "forge\src\main\resources\assets\ultimate_apple_mod\lang"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

# Build new entries per locale as a JSON fragment string.
# We avoid special Unicode chars in the PS1 source itself; the section sign (SS)
# is injected via [char]0xA7 so the script stays ASCII-safe.
$ss = [char]0xA7

function MakeEntries($items, $exposed, $weathered, $oxidized,
                     $waxed, $waxedExp, $waxedWea, $waxedOxi,
                     $ttWaxed, $ttOxi, $ttFull,
                     $advOxiTitle, $advOxiDesc,
                     $advWaxTitle, $advWaxDesc,
                     $msg1, $msg2, $msg3) {
    $pairs = @(
        @("item.ultimate_apple_mod.exposed_copper_apple",           $exposed),
        @("item.ultimate_apple_mod.weathered_copper_apple",         $weathered),
        @("item.ultimate_apple_mod.oxidized_copper_apple",          $oxidized),
        @("item.ultimate_apple_mod.waxed_copper_apple",             $waxed),
        @("item.ultimate_apple_mod.waxed_exposed_copper_apple",     $waxedExp),
        @("item.ultimate_apple_mod.waxed_weathered_copper_apple",   $waxedWea),
        @("item.ultimate_apple_mod.waxed_oxidized_copper_apple",    $waxedOxi),
        @("tooltip.ultimate_apple_mod.copper_apple.waxed",          $ttWaxed),
        @("tooltip.ultimate_apple_mod.copper_apple.oxidation",      $ttOxi),
        @("tooltip.ultimate_apple_mod.copper_apple.fully_oxidized", $ttFull),
        @("advancements.ultimate_apple_mod.oxidized_copper_apple.title",       $advOxiTitle),
        @("advancements.ultimate_apple_mod.oxidized_copper_apple.description", $advOxiDesc),
        @("advancements.ultimate_apple_mod.waxed_copper_apple.title",          $advWaxTitle),
        @("advancements.ultimate_apple_mod.waxed_copper_apple.description",    $advWaxDesc),
        @("message.ultimate_apple_mod.copper_apple.stage1", $msg1),
        @("message.ultimate_apple_mod.copper_apple.stage2", $msg2),
        @("message.ultimate_apple_mod.copper_apple.stage3", $msg3)
    )
    return $pairs
}

# Helper: escape a value for JSON
function Esc($s) { return $s -replace '\\','\\' -replace '"','\"' }

$translations = @{}

# ---------- English variants ----------
$enEntries = MakeEntries $null `
    "Exposed Copper Apple" `
    "Weathered Copper Apple" `
    "Oxidized Copper Apple" `
    "Waxed Copper Apple" `
    "Waxed Exposed Copper Apple" `
    "Waxed Weathered Copper Apple" `
    "Waxed Oxidized Copper Apple" `
    ("${ss}6Waxed - will not oxidize further.") `
    ("${ss}7Oxidation: %s%%") `
    ("${ss}7Fully oxidized.") `
    "The Green Apple" `
    "Let your Copper Apple fully oxidize into a teal relic." `
    "Sealed in Wax" `
    "Wax a Copper Apple with a Honeycomb to freeze its oxidation stage." `
    ("${ss}6Your Copper Apple is showing some tarnish...") `
    ("${ss}6Your Copper Apple has turned weathered.") `
    ("${ss}6Your Copper Apple is fully oxidized!")
$translations["en_us"] = $enEntries
$translations["en_ca"] = $enEntries

# en_au / en_gb / en_nz use British spelling "oxidised"
$enGbEntries = MakeEntries $null `
    "Exposed Copper Apple" `
    "Weathered Copper Apple" `
    "Oxidised Copper Apple" `
    "Waxed Copper Apple" `
    "Waxed Exposed Copper Apple" `
    "Waxed Weathered Copper Apple" `
    "Waxed Oxidised Copper Apple" `
    ("${ss}6Waxed - will not oxidise further.") `
    ("${ss}7Oxidation: %s%%") `
    ("${ss}7Fully oxidised.") `
    "The Green Apple" `
    "Let your Copper Apple fully oxidise into a teal relic." `
    "Sealed in Wax" `
    "Wax a Copper Apple with a Honeycomb to freeze its oxidation stage." `
    ("${ss}6Your Copper Apple is showing some tarnish...") `
    ("${ss}6Your Copper Apple has turned weathered.") `
    ("${ss}6Your Copper Apple is fully oxidised!")
$translations["en_au"] = $enGbEntries
$translations["en_gb"] = $enGbEntries
$translations["en_nz"] = $enGbEntries

# ---------- German ----------
$deEntries = MakeEntries $null `
    "Leicht oxidierter Kupferapfel" `
    "Verwitterter Kupferapfel" `
    "Oxidierter Kupferapfel" `
    "Gewachster Kupferapfel" `
    "Gewachster leicht oxidierter Kupferapfel" `
    "Gewachster verwitterter Kupferapfel" `
    "Gewachster oxidierter Kupferapfel" `
    ("${ss}6Gewachst - oxidiert nicht weiter.") `
    ("${ss}7Oxidation: %s%%") `
    ("${ss}7$([char]0x56)$([char]0x6F)$([char]0x6C)$([char]0x6C)$([char]0x73)$([char]0x74)$([char]0xE4)$([char]0x6E)$([char]0x64)$([char]0x69)$([char]0x67) oxidiert.") `
    "Der $([char]0x67)$([char]0x72)$([char]0xFC)$([char]0x6E)$([char]0x65) Apfel" `
    "Lass deinen Kupferapfel $([char]0x76)$([char]0x6F)$([char]0x6C)$([char]0x6C)$([char]0x73)$([char]0x74)$([char]0xE4)$([char]0x6E)$([char]0x64)$([char]0x69)$([char]0x67) oxidieren." `
    "In Wachs versiegelt" `
    "Versiegle einen Kupferapfel mit einer Honigwabe." `
    ("${ss}6Dein Kupferapfel bekommt erste Patina...") `
    ("${ss}6Dein Kupferapfel ist verwittert.") `
    ("${ss}6Dein Kupferapfel ist $([char]0x76)$([char]0x6F)$([char]0x6C)$([char]0x6C)$([char]0x73)$([char]0x74)$([char]0xE4)$([char]0x6E)$([char]0x64)$([char]0x69)$([char]0x67) oxidiert!")
$translations["de_de"] = $deEntries
$translations["de_at"] = $deEntries

$deChEntries = MakeEntries $null `
    "Leicht oxidierter Kupferapfel" `
    "Verwitterter Kupferapfel" `
    "Oxidierter Kupferapfel" `
    "Gewachster Kupferapfel" `
    "Gewachster leicht oxidierter Kupferapfel" `
    "Gewachster verwitterter Kupferapfel" `
    "Gewachster oxidierter Kupferapfel" `
    ("${ss}6Gewachst - oxidiert nicht weiter.") `
    ("${ss}7Oxidation: %s%%") `
    ("${ss}7$([char]0x56)$([char]0x6F)$([char]0x6C)$([char]0x6C)$([char]0x73)$([char]0x74)$([char]0xE4)$([char]0x6E)$([char]0x64)$([char]0x69)$([char]0x67) oxidiert.") `
    "Der $([char]0x67)$([char]0x72)$([char]0xFC)$([char]0x6E)$([char]0x65) Apfel" `
    "Lass deinen Kupferapfel $([char]0x76)$([char]0x6F)$([char]0x6C)$([char]0x6C)$([char]0x73)$([char]0x74)$([char]0xE4)$([char]0x6E)$([char]0x64)$([char]0x69)$([char]0x67) oxidieren." `
    "In Wachs versiegelt" `
    "Versiegle einen Kupferapfel mit einer Honigwabe." `
    ("${ss}6Din Kupferapfel bekommt erst Patina...") `
    ("${ss}6Din Kupferapfel isch verwittert.") `
    ("${ss}6Din Kupferapfel isch $([char]0x76)$([char]0x6F)$([char]0x6C)$([char]0x6C)$([char]0x73)$([char]0x74)$([char]0xE4)$([char]0x6E)$([char]0x64)$([char]0x69)$([char]0x67) oxidiert!")
$translations["de_ch"] = $deChEntries

$barEntries = MakeEntries $null `
    "A bissl oxidierter Kupferapfe" `
    "Verwitterter Kupferapfe" `
    "Voi oxidierter Kupferapfe" `
    "Gwachster Kupferapfe" `
    "Gwachster a bissl oxidierter Kupferapfe" `
    "Gwachster verwitterter Kupferapfe" `
    "Gwachster voi oxidierter Kupferapfe" `
    ("${ss}6Gwachst - oxidiert nimma weida.") `
    ("${ss}7Oxidation: %s%%") `
    ("${ss}7Voi oxidiert.") `
    "Da $([char]0x67)$([char]0x72)$([char]0xFC)$([char]0x61) Apfe" `
    "Lass dei Kupferapfe voi oxidieren." `
    "Mit Wachs versiegelt" `
    "Wachse an Kupferapfe mit ana Honigwabe." `
    ("${ss}6Dei Kupferapfe kriagt a bissl Patina...") `
    ("${ss}6Dei Kupferapfe is verwittert.") `
    ("${ss}6Dei Kupferapfe is voi oxidiert!")
$translations["bar"] = $barEntries

# ---------- Danish ----------
$daDkEntries = MakeEntries $null `
    "Eksponeret Kobbera$([char]0xE6)ble" `
    "Vejrbidt Kobbera$([char]0xE6)ble" `
    "Oxideret Kobbera$([char]0xE6)ble" `
    "Vokset Kobbera$([char]0xE6)ble" `
    "Vokset Eksponeret Kobbera$([char]0xE6)ble" `
    "Vokset Vejrbidt Kobbera$([char]0xE6)ble" `
    "Vokset Oxideret Kobbera$([char]0xE6)ble" `
    ("${ss}6Vokset - oxiderer ikke l$([char]0xE6)ngere.") `
    ("${ss}7Oxidation: %s%%") `
    ("${ss}7Fuldt oxideret.") `
    "Det Gr$([char]0xF8)nne $([char]0xC6)ble" `
    "Lad dit Kobbera$([char]0xE6)ble oxidere fuldt$([char]0xE6)ndigt." `
    "Forseglet med Voks" `
    "Voks et Kobbera$([char]0xE6)ble med en honningkam." `
    ("${ss}6Dit Kobbera$([char]0xE6)ble begynder at patinere...") `
    ("${ss}6Dit Kobbera$([char]0xE6)ble er blevet vejrbidt.") `
    ("${ss}6Dit Kobbera$([char]0xE6)ble er fuldt oxideret!")
$translations["da_dk"] = $daDkEntries

# ---------- Spanish ----------
$esEntries = MakeEntries $null `
    "Manzana de Cobre Expuesto" `
    "Manzana de Cobre Desgastado" `
    "Manzana de Cobre Oxidado" `
    "Manzana de Cobre Encerada" `
    "Manzana de Cobre Expuesto Encerada" `
    "Manzana de Cobre Desgastado Encerada" `
    "Manzana de Cobre Oxidado Encerada" `
    ("${ss}6Encerada - no oxidar$([char]0xE1) m$([char]0xE1)s.") `
    ("${ss}7Oxidaci$([char]0xF3)n: %s%%") `
    ("${ss}7Completamente oxidada.") `
    "La Manzana Verde" `
    "Deja que tu Manzana de Cobre se oxide completamente." `
    "Sellada con Cera" `
    "Encera una Manzana de Cobre con un panal." `
    ("${ss}6Tu Manzana de Cobre muestra p$([char]0xE1)tina inicial...") `
    ("${ss}6Tu Manzana de Cobre se ha desgastado.") `
    ("${ss}6$([char]0xA1)Tu Manzana de Cobre est$([char]0xE1) completamente oxidada!")
$translations["es_es"] = $esEntries
$translations["es_cl"] = $esEntries
$translations["es_ec"] = $esEntries
$translations["es_mx"] = $esEntries
$translations["es_ve"] = $esEntries

$esRioPlataEntries = MakeEntries $null `
    "Manzana de Cobre Expuesto" `
    "Manzana de Cobre Desgastado" `
    "Manzana de Cobre Oxidado" `
    "Manzana de Cobre Encerada" `
    "Manzana de Cobre Expuesto Encerada" `
    "Manzana de Cobre Desgastado Encerada" `
    "Manzana de Cobre Oxidado Encerada" `
    ("${ss}6Encerada - no oxidar$([char]0xE1) m$([char]0xE1)s.") `
    ("${ss}7Oxidaci$([char]0xF3)n: %s%%") `
    ("${ss}7Completamente oxidada.") `
    "La Manzana Verde" `
    "Dej$([char]0xE1) que tu Manzana de Cobre se oxide completamente." `
    "Sellada con Cera" `
    "Encerr$([char]0xE1) una Manzana de Cobre con un panal." `
    ("${ss}6Tu Manzana de Cobre muestra p$([char]0xE1)tina inicial...") `
    ("${ss}6Tu Manzana de Cobre se ha desgastado.") `
    ("${ss}6$([char]0xA1)Tu Manzana de Cobre est$([char]0xE1) completamente oxidada!")
$translations["es_ar"] = $esRioPlataEntries
$translations["es_uy"] = $esRioPlataEntries

# ---------- French ----------
$frEntries = MakeEntries $null `
    "Pomme de Cuivre Expos$([char]0xE9)e" `
    "Pomme de Cuivre Patin$([char]0xE9)e" `
    "Pomme de Cuivre Oxyd$([char]0xE9)e" `
    "Pomme de Cuivre Cir$([char]0xE9)e" `
    "Pomme de Cuivre Expos$([char]0xE9)e Cir$([char]0xE9)e" `
    "Pomme de Cuivre Patin$([char]0xE9)e Cir$([char]0xE9)e" `
    "Pomme de Cuivre Oxyd$([char]0xE9)e Cir$([char]0xE9)e" `
    ("${ss}6Cir$([char]0xE9)e - n'oxyde plus.") `
    ("${ss}7Oxydation : %s%%") `
    ("${ss}7Compl$([char]0xE8)tement oxyd$([char]0xE9)e.") `
    "La Pomme Verte" `
    "Laissez votre Pomme de Cuivre s'oxyder compl$([char]0xE8)tement." `
    "Scell$([char]0xE9)e $([char]0xE0) la Cire" `
    "Cirez une Pomme de Cuivre avec un rayon de miel." `
    ("${ss}6Votre Pomme de Cuivre montre une l$([char]0xE9)g$([char]0xE8)re patine...") `
    ("${ss}6Votre Pomme de Cuivre s'est patin$([char]0xE9)e.") `
    ("${ss}6Votre Pomme de Cuivre est compl$([char]0xE8)tement oxyd$([char]0xE9)e !")
$translations["fr_fr"] = $frEntries
$translations["fr_ca"] = $frEntries

# ---------- Japanese ----------
$jaEntries = MakeEntries $null `
    "$([char]0x521D)$([char]0x671F)$([char]0x98A8)$([char]0x5316)$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)" `
    "$([char]0x98A8)$([char]0x5316)$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)" `
    "$([char]0x9178)$([char]0x5316)$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)" `
    "$([char]0x8822)$([char]0x5F15)$([char]0x304D)$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)" `
    "$([char]0x8822)$([char]0x5F15)$([char]0x304D)$([char]0x521D)$([char]0x671F)$([char]0x98A8)$([char]0x5316)$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)" `
    "$([char]0x8822)$([char]0x5F15)$([char]0x304D)$([char]0x98A8)$([char]0x5316)$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)" `
    "$([char]0x8822)$([char]0x5F15)$([char]0x304D)$([char]0x9178)$([char]0x5316)$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)" `
    ("${ss}6$([char]0x8822)$([char]0x5F15)$([char]0x304D)$([char]0x6E08)$([char]0x307F) - $([char]0x3053)$([char]0x308C)$([char]0x4EE5)$([char]0x4E0A)$([char]0x9178)$([char]0x5316)$([char]0x3057)$([char]0x307E)$([char]0x305B)$([char]0x3093)$([char]0x3002)") `
    ("${ss}7$([char]0x9178)$([char]0x5316)$([char]0x5EA6): %s%%") `
    ("${ss}7$([char]0x5B8C)$([char]0x5168)$([char]0x306B)$([char]0x9178)$([char]0x5316)$([char]0x3057)$([char]0x3066)$([char]0x3044)$([char]0x307E)$([char]0x3059)$([char]0x3002)") `
    "$([char]0x7DD1)$([char]0x8272)$([char]0x306E)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)" `
    "$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)$([char]0x3092)$([char]0x5B8C)$([char]0x5168)$([char]0x306B)$([char]0x9178)$([char]0x5316)$([char]0x3055)$([char]0x305B)$([char]0x3088)$([char]0x3046)$([char]0x3002)" `
    "$([char]0x8822)$([char]0x3067)$([char]0x5C01)$([char]0x5370)" `
    "$([char]0x8822)$([char]0x306E)$([char]0x5DE3)$([char]0x3067)$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)$([char]0x3092)$([char]0x8822)$([char]0x5F15)$([char]0x304D)$([char]0x3057)$([char]0x3088)$([char]0x3046)$([char]0x3002)" `
    ("${ss}6$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)$([char]0x304C)$([char]0x5C11)$([char]0x3057)$([char]0x9310)$([char]0x3073)$([char]0x3066)$([char]0x304D)$([char]0x305F)...") `
    ("${ss}6$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)$([char]0x304C)$([char]0x3055)$([char]0x3089)$([char]0x306B)$([char]0x98A8)$([char]0x5316)$([char]0x3057)$([char]0x305F)$([char]0x3002)") `
    ("${ss}6$([char]0x9285)$([char]0x30EA)$([char]0x30F3)$([char]0x30B4)$([char]0x304C)$([char]0x5B8C)$([char]0x5168)$([char]0x306B)$([char]0x9178)$([char]0x5316)$([char]0x3057)$([char]0x305F)$([char]0xFF01)")
$translations["ja_jp"] = $jaEntries

# ---------- Korean ----------
$koEntries = MakeEntries $null `
    "$([char]0xC57D)$([char]0xAC04) $([char]0xC0B0)$([char]0xD654)$([char]0xB41C) $([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)" `
    "$([char]0xD48D)$([char]0xD654)$([char]0xB41C) $([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)" `
    "$([char]0xC0B0)$([char]0xD654)$([char]0xB41C) $([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)" `
    "$([char]0xBC00)$([char]0xB78D) $([char]0xCC98)$([char]0xB9AC)$([char]0xB41C) $([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)" `
    "$([char]0xBC00)$([char]0xB78D) $([char]0xCC98)$([char]0xB9AC)$([char]0xB41C) $([char]0xC57D)$([char]0xAC04) $([char]0xC0B0)$([char]0xD654)$([char]0xB41C) $([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)" `
    "$([char]0xBC00)$([char]0xB78D) $([char]0xCC98)$([char]0xB9AC)$([char]0xB41C) $([char]0xD48D)$([char]0xD654)$([char]0xB41C) $([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)" `
    "$([char]0xBC00)$([char]0xB78D) $([char]0xCC98)$([char]0xB9AC)$([char]0xB41C) $([char]0xC0B0)$([char]0xD654)$([char]0xB41C) $([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)" `
    ("${ss}6$([char]0xBC00)$([char]0xB78D) $([char]0xCC98)$([char]0xB9AC)$([char]0xB428) - $([char]0xB354) $([char]0xC774)$([char]0xC0C1) $([char]0xC0B0)$([char]0xD654)$([char]0xB418)$([char]0xC9C0) $([char]0xC54A)$([char]0xC2B5)$([char]0xB2C8)$([char]0xB2E4).") `
    ("${ss}7$([char]0xC0B0)$([char]0xD654)$([char]0xB3C4): %s%%") `
    ("${ss}7$([char]0xC644)$([char]0xC804)$([char]0xD788) $([char]0xC0B0)$([char]0xD654)$([char]0xB418)$([char]0xC5C8)$([char]0xC2B5)$([char]0xB2C8)$([char]0xB2E4).") `
    "$([char]0xCD08)$([char]0xB85D) $([char]0xC0AC)$([char]0xACFC)" `
    "$([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)$([char]0xB97C) $([char]0xC644)$([char]0xC804)$([char]0xD788) $([char]0xC0B0)$([char]0xD654)$([char]0xC2DC)$([char]0xD0A4)$([char]0xC138)$([char]0xC694)." `
    "$([char]0xBC00)$([char]0xB78D)$([char]0xC73C)$([char]0xB85C) $([char]0xBD09)$([char]0xC778)" `
    "$([char]0xBC8C)$([char]0xC9D1)$([char]0xC73C)$([char]0xB85C) $([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)$([char]0xC5D0) $([char]0xBC00)$([char]0xB78D)$([char]0xC744) $([char]0xC785)$([char]0xD788)$([char]0xC138)$([char]0xC694)." `
    ("${ss}6$([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)$([char]0xC5D0) $([char]0xB179)$([char]0xC774) $([char]0xC2AC)$([char]0xAE30) $([char]0xC2DC)$([char]0xC791)$([char]0xD588)$([char]0xC2B5)$([char]0xB2C8)$([char]0xB2E4)...") `
    ("${ss}6$([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)$([char]0xAC00) $([char]0xD48D)$([char]0xD654)$([char]0xB418)$([char]0xC5C8)$([char]0xC2B5)$([char]0xB2C8)$([char]0xB2E4).") `
    ("${ss}6$([char]0xAD6C)$([char]0xB9AC) $([char]0xC0AC)$([char]0xACFC)$([char]0xAC00) $([char]0xC644)$([char]0xC804)$([char]0xD788) $([char]0xC0B0)$([char]0xD654)$([char]0xB418)$([char]0xC5C8)$([char]0xC2B5)$([char]0xB2C8)$([char]0xB2E4)!")
$translations["ko_kr"] = $koEntries

# ---------- Portuguese ----------
$ptEntries = MakeEntries $null `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Exposta" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Desgastada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Oxidada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Encerada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Exposta Encerada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Desgastada Encerada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Oxidada Encerada" `
    ("${ss}6Encerada - n$([char]0xE3)o oxidar$([char]0xE1) mais.") `
    ("${ss}7Oxida$([char]0xE7)$([char]0xE3)o: %s%%") `
    ("${ss}7Completamente oxidada.") `
    "A Ma$([char]0xE7)$([char]0xE3) Verde" `
    "Deixe sua Ma$([char]0xE7)$([char]0xE3) de Cobre oxidar completamente." `
    "Selada com Cera" `
    "Encere uma Ma$([char]0xE7)$([char]0xE3) de Cobre com um favo de mel." `
    ("${ss}6Sua Ma$([char]0xE7)$([char]0xE3) de Cobre est$([char]0xE1) mostrando p$([char]0xE1)tina...") `
    ("${ss}6Sua Ma$([char]0xE7)$([char]0xE3) de Cobre ficou desgastada.") `
    ("${ss}6Sua Ma$([char]0xE7)$([char]0xE3) de Cobre est$([char]0xE1) completamente oxidada!")
$translations["pt_br"] = $ptEntries

$ptPtEntries = MakeEntries $null `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Exposta" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Desgastada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Oxidada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Encerada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Exposta Encerada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Desgastada Encerada" `
    "Ma$([char]0xE7)$([char]0xE3) de Cobre Oxidada Encerada" `
    ("${ss}6Encerada - n$([char]0xE3)o oxidar$([char]0xE1) mais.") `
    ("${ss}7Oxida$([char]0xE7)$([char]0xE3)o: %s%%") `
    ("${ss}7Completamente oxidada.") `
    "A Ma$([char]0xE7)$([char]0xE3) Verde" `
    "Deixa que a tua Ma$([char]0xE7)$([char]0xE3) de Cobre oxide completamente." `
    "Selada com Cera" `
    "Encera uma Ma$([char]0xE7)$([char]0xE3) de Cobre com um favo de mel." `
    ("${ss}6A tua Ma$([char]0xE7)$([char]0xE3) de Cobre est$([char]0xE1) a mostrar p$([char]0xE1)tina...") `
    ("${ss}6A tua Ma$([char]0xE7)$([char]0xE3) de Cobre ficou desgastada.") `
    ("${ss}6A tua Ma$([char]0xE7)$([char]0xE3) de Cobre est$([char]0xE1) completamente oxidada!")
$translations["pt_pt"] = $ptPtEntries

# ---------- Romanian ----------
$roEntries = MakeEntries $null `
    "M$([char]0x103)r de Cupru Expus" `
    "M$([char]0x103)r de Cupru Deteriorat" `
    "M$([char]0x103)r de Cupru Oxidat" `
    "M$([char]0x103)r de Cupru Ceruit" `
    "M$([char]0x103)r de Cupru Expus Ceruit" `
    "M$([char]0x103)r de Cupru Deteriorat Ceruit" `
    "M$([char]0x103)r de Cupru Oxidat Ceruit" `
    ("${ss}6Ceruit - nu se va oxida mai mult.") `
    ("${ss}7Oxidare: %s%%") `
    ("${ss}7Complet oxidat.") `
    "M$([char]0x103)rul Verde" `
    "Las$([char]0x103) M$([char]0x103)rul de Cupru s$([char]0x103) se oxideze complet." `
    "Sigilat cu Cear$([char]0x103)" `
    "Cerui$([char]0x219)te un M$([char]0x103)r de Cupru cu un fagure de miere." `
    ("${ss}6M$([char]0x103)rul t$([char]0x103)u de Cupru arat$([char]0x103) primele semne de patinare...") `
    ("${ss}6M$([char]0x103)rul t$([char]0x103)u de Cupru s-a deteriorat.") `
    ("${ss}6M$([char]0x103)rul t$([char]0x103)u de Cupru este complet oxidat!")
$translations["ro_ro"] = $roEntries

# ---------- Russian ----------
$ruEntries = MakeEntries $null `
    "$([char]0x421)$([char]0x43B)$([char]0x435)$([char]0x433)$([char]0x43A)$([char]0x430) $([char]0x43E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x451)$([char]0x43D)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E)" `
    "$([char]0x412)$([char]0x44B)$([char]0x432)$([char]0x435)$([char]0x442)$([char]0x440)$([char]0x438)$([char]0x432)$([char]0x448)$([char]0x435)$([char]0x435)$([char]0x441)$([char]0x44F) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E)" `
    "$([char]0x41E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x451)$([char]0x43D)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E)" `
    "$([char]0x412)$([char]0x43E)$([char]0x449)$([char]0x451)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E)" `
    "$([char]0x412)$([char]0x43E)$([char]0x449)$([char]0x451)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x441)$([char]0x43B)$([char]0x435)$([char]0x433)$([char]0x43A)$([char]0x430) $([char]0x43E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x451)$([char]0x43D)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E)" `
    "$([char]0x412)$([char]0x43E)$([char]0x449)$([char]0x451)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x432)$([char]0x44B)$([char]0x432)$([char]0x435)$([char]0x442)$([char]0x440)$([char]0x438)$([char]0x432)$([char]0x448)$([char]0x435)$([char]0x435)$([char]0x441)$([char]0x44F) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E)" `
    "$([char]0x412)$([char]0x43E)$([char]0x449)$([char]0x451)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x43E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x451)$([char]0x43D)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E)" `
    ("${ss}6$([char]0x41F)$([char]0x43E)$([char]0x43A)$([char]0x440)$([char]0x44B)$([char]0x442)$([char]0x43E) $([char]0x432)$([char]0x43E)$([char]0x441)$([char]0x43A)$([char]0x43E)$([char]0x43C) - $([char]0x431)$([char]0x43E)$([char]0x43B)$([char]0x44C)$([char]0x448)$([char]0x435) $([char]0x43D)$([char]0x435) $([char]0x43E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x44F)$([char]0x435)$([char]0x442)$([char]0x441)$([char]0x44F).") `
    ("${ss}7$([char]0x41E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x435)$([char]0x43D)$([char]0x438)$([char]0x435): %s%%") `
    ("${ss}7$([char]0x41F)$([char]0x43E)$([char]0x43B)$([char]0x43D)$([char]0x43E)$([char]0x441)$([char]0x442)$([char]0x44C)$([char]0x44E) $([char]0x43E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x435)$([char]0x43D)$([char]0x43E).") `
    "$([char]0x417)$([char]0x435)$([char]0x43B)$([char]0x451)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E)" `
    "$([char]0x414)$([char]0x430)$([char]0x439)$([char]0x442)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x43C)$([char]0x443) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x443) $([char]0x43F)$([char]0x43E)$([char]0x43B)$([char]0x43D)$([char]0x43E)$([char]0x441)$([char]0x442)$([char]0x44C)$([char]0x44E) $([char]0x43E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x438)$([char]0x442)$([char]0x44C)$([char]0x441)$([char]0x44F)." `
    "$([char]0x417)$([char]0x430)$([char]0x43F)$([char]0x435)$([char]0x447)$([char]0x430)$([char]0x442)$([char]0x430)$([char]0x43D)$([char]0x43E) $([char]0x432)$([char]0x43E)$([char]0x441)$([char]0x43A)$([char]0x43E)$([char]0x43C)" `
    "$([char]0x41F)$([char]0x43E)$([char]0x43A)$([char]0x440)$([char]0x43E)$([char]0x439)$([char]0x442)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E) $([char]0x432)$([char]0x43E)$([char]0x441)$([char]0x43A)$([char]0x43E)$([char]0x43C) $([char]0x441) $([char]0x43F)$([char]0x43E)$([char]0x43C)$([char]0x43E)$([char]0x449)$([char]0x44C)$([char]0x44E) $([char]0x441)$([char]0x43E)$([char]0x442)." `
    ("${ss}6$([char]0x412)$([char]0x430)$([char]0x448)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E) $([char]0x43D)$([char]0x430)$([char]0x447)$([char]0x438)$([char]0x43D)$([char]0x430)$([char]0x435)$([char]0x442) $([char]0x43F)$([char]0x43E)$([char]0x43A)$([char]0x440)$([char]0x44B)$([char]0x432)$([char]0x430)$([char]0x442)$([char]0x44C)$([char]0x441)$([char]0x44F) $([char]0x43F)$([char]0x430)$([char]0x442)$([char]0x438)$([char]0x43D)$([char]0x43E)$([char]0x439)...") `
    ("${ss}6$([char]0x412)$([char]0x430)$([char]0x448)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E) $([char]0x432)$([char]0x44B)$([char]0x432)$([char]0x435)$([char]0x442)$([char]0x440)$([char]0x438)$([char]0x43B)$([char]0x43E)$([char]0x441)$([char]0x44C).") `
    ("${ss}6$([char]0x412)$([char]0x430)$([char]0x448)$([char]0x435) $([char]0x43C)$([char]0x435)$([char]0x434)$([char]0x43D)$([char]0x43E)$([char]0x435) $([char]0x44F)$([char]0x431)$([char]0x43B)$([char]0x43E)$([char]0x43A)$([char]0x43E) $([char]0x43F)$([char]0x43E)$([char]0x43B)$([char]0x43D)$([char]0x43E)$([char]0x441)$([char]0x442)$([char]0x44C)$([char]0x44E) $([char]0x43E)$([char]0x43A)$([char]0x438)$([char]0x441)$([char]0x43B)$([char]0x438)$([char]0x43B)$([char]0x43E)$([char]0x441)$([char]0x44C)!")
$translations["ru_ru"] = $ruEntries

# ---------- Turkish ----------
$trEntries = MakeEntries $null `
    "A$([char]0xE7)$([char]0x131)k Bak$([char]0x131)r Elmas$([char]0x131)" `
    "Y$([char]0x131)pranm$([char]0x131)$([char]0x15F) Bak$([char]0x131)r Elmas$([char]0x131)" `
    "Okside Bak$([char]0x131)r Elmas$([char]0x131)" `
    "Mumlanm$([char]0x131)$([char]0x15F) Bak$([char]0x131)r Elmas$([char]0x131)" `
    "Mumlanm$([char]0x131)$([char]0x15F) A$([char]0xE7)$([char]0x131)k Bak$([char]0x131)r Elmas$([char]0x131)" `
    "Mumlanm$([char]0x131)$([char]0x15F) Y$([char]0x131)pranm$([char]0x131)$([char]0x15F) Bak$([char]0x131)r Elmas$([char]0x131)" `
    "Mumlanm$([char]0x131)$([char]0x15F) Okside Bak$([char]0x131)r Elmas$([char]0x131)" `
    ("${ss}6Mumlanm$([char]0x131)$([char]0x15F) - art$([char]0x131)k okside olmaz.") `
    ("${ss}7Oksidasyon: %s%%") `
    ("${ss}7Tamamen okside.") `
    "Ye$([char]0x15F)il Elma" `
    "Bak$([char]0x131)r Elman$([char]0x131)n tamamen okside olmas$([char]0x131)na izin ver." `
    "Mumla M$([char]0xFC)h$([char]0xFC)rlendi" `
    "Bir Bak$([char]0x131)r Elmay$([char]0x131) petek ile mumla." `
    ("${ss}6Bak$([char]0x131)r Elman$([char]0x131)n $([char]0xFC)zerinde pas belirtileri g$([char]0xF6)r$([char]0xFC)l$([char]0xFC)yor...") `
    ("${ss}6Bak$([char]0x131)r Elman y$([char]0x131)prand$([char]0x131).") `
    ("${ss}6Bak$([char]0x131)r Elman tamamen okside oldu!")
$translations["tr_tr"] = $trEntries

# ---------- Chinese Simplified ----------
$zhCnEntries = MakeEntries $null `
    "$([char]0x6591)$([char]0x9A73)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)" `
    "$([char]0x9508)$([char]0x5316)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)" `
    "$([char]0x6C27)$([char]0x5316)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)" `
    "$([char]0x6D82)$([char]0x8721)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)" `
    "$([char]0x6D82)$([char]0x8721)$([char]0x6591)$([char]0x9A73)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)" `
    "$([char]0x6D82)$([char]0x8721)$([char]0x9508)$([char]0x5316)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)" `
    "$([char]0x6D82)$([char]0x8721)$([char]0x6C27)$([char]0x5316)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)" `
    ("${ss}6$([char]0x5DF2)$([char]0x6D82)$([char]0x8721) - $([char]0x4E0D)$([char]0x518D)$([char]0x7EE7)$([char]0x7EED)$([char]0x6C27)$([char]0x5316)$([char]0x3002)") `
    ("${ss}7$([char]0x6C27)$([char]0x5316)$([char]0x7A0B)$([char]0x5EA6)$([char]0xFF1A)%s%%") `
    ("${ss}7$([char]0x5DF2)$([char]0x5B8C)$([char]0x5168)$([char]0x6C27)$([char]0x5316)$([char]0x3002)") `
    "$([char]0x7EFF)$([char]0x8272)$([char]0x82F9)$([char]0x679C)" `
    "$([char]0x8BA9)$([char]0x4F60)$([char]0x7684)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)$([char]0x5B8C)$([char]0x5168)$([char]0x6C27)$([char]0x5316)$([char]0x3002)" `
    "$([char]0x8721)$([char]0x5C01)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)" `
    "$([char]0x7528)$([char]0x8702)$([char]0x5DE2)$([char]0x5BF9)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)$([char]0x8FDB)$([char]0x884C)$([char]0x6D82)$([char]0x8721)$([char]0x5904)$([char]0x7406)$([char]0x3002)" `
    ("${ss}6$([char]0x4F60)$([char]0x7684)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)$([char]0x5F00)$([char]0x59CB)$([char]0x51FA)$([char]0x73B0)$([char]0x9508)$([char]0x6591)...") `
    ("${ss}6$([char]0x4F60)$([char]0x7684)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)$([char]0x5DF2)$([char]0x9508)$([char]0x5316)$([char]0x3002)") `
    ("${ss}6$([char]0x4F60)$([char]0x7684)$([char]0x94DC)$([char]0x82F9)$([char]0x679C)$([char]0x5DF2)$([char]0x5B8C)$([char]0x5168)$([char]0x6C27)$([char]0x5316)$([char]0xFF01)")
$translations["zh_cn"] = $zhCnEntries

# ---------- Chinese Traditional ----------
$zhTwEntries = MakeEntries $null `
    "$([char]0x6591)$([char]0x99C1)$([char]0x9285)$([char]0x860B)$([char]0x679C)" `
    "$([char]0x9508)$([char]0x5316)$([char]0x9285)$([char]0x860B)$([char]0x679C)" `
    "$([char]0x6C27)$([char]0x5316)$([char]0x9285)$([char]0x860B)$([char]0x679C)" `
    "$([char]0x69C1)$([char]0x8A60)$([char]0x9285)$([char]0x860B)$([char]0x679C)" `
    "$([char]0x69C1)$([char]0x8A60)$([char]0x6591)$([char]0x99C1)$([char]0x9285)$([char]0x860B)$([char]0x679C)" `
    "$([char]0x69C1)$([char]0x8A60)$([char]0x9508)$([char]0x5316)$([char]0x9285)$([char]0x860B)$([char]0x679C)" `
    "$([char]0x69C1)$([char]0x8A60)$([char]0x6C27)$([char]0x5316)$([char]0x9285)$([char]0x860B)$([char]0x679C)" `
    ("${ss}6$([char]0x5DF2)$([char]0x69C1)$([char]0x8A60) - $([char]0x4E0D)$([char]0x518D)$([char]0x7E7C)$([char]0x7E8C)$([char]0x6C27)$([char]0x5316)$([char]0x3002)") `
    ("${ss}7$([char]0x6C27)$([char]0x5316)$([char]0x7A0B)$([char]0x5EA6)$([char]0xFF1A)%s%%") `
    ("${ss}7$([char]0x5DF2)$([char]0x5B8C)$([char]0x5168)$([char]0x6C27)$([char]0x5316)$([char]0x3002)") `
    "$([char]0x7DA0)$([char]0x8272)$([char]0x860B)$([char]0x679C)" `
    "$([char]0x8B93)$([char]0x4F60)$([char]0x7684)$([char]0x9285)$([char]0x860B)$([char]0x679C)$([char]0x5B8C)$([char]0x5168)$([char]0x6C27)$([char]0x5316)$([char]0x3002)" `
    "$([char]0x8A60)$([char]0x5C01)$([char]0x9285)$([char]0x860B)$([char]0x679C)" `
    "$([char]0x7528)$([char]0x8702)$([char]0x5DE2)$([char]0x5C0D)$([char]0x9285)$([char]0x860B)$([char]0x679C)$([char]0x9032)$([char]0x884C)$([char]0x69C1)$([char]0x8A60)$([char]0x8655)$([char]0x7406)$([char]0x3002)" `
    ("${ss}6$([char]0x4F60)$([char]0x7684)$([char]0x9285)$([char]0x860B)$([char]0x679C)$([char]0x958B)$([char]0x59CB)$([char]0x51FA)$([char]0x73FE)$([char]0x9508)$([char]0x6591)...") `
    ("${ss}6$([char]0x4F60)$([char]0x7684)$([char]0x9285)$([char]0x860B)$([char]0x679C)$([char]0x5DF2)$([char]0x9508)$([char]0x5316)$([char]0x3002)") `
    ("${ss}6$([char]0x4F60)$([char]0x7684)$([char]0x9285)$([char]0x860B)$([char]0x679C)$([char]0x5DF2)$([char]0x5B8C)$([char]0x5168)$([char]0x6C27)$([char]0x5316)$([char]0xFF01)")
$translations["zh_tw"] = $zhTwEntries

# ======== Apply to all lang files ========
$errors = @()
$updated = 0

Get-ChildItem -Path $langDir -Filter "*.json" | ForEach-Object {
    $file    = $_.FullName
    $locale  = $_.BaseName
    $content = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8)

    $entries = $translations[$locale]
    if (-not $entries) {
        $entries = $translations["en_us"]
        Write-Host "  [fallback en_us] $locale"
    }

    # Build JSON fragment
    $fragment = ""
    foreach ($pair in $entries) {
        $k = (Esc $pair[0])
        $v = (Esc $pair[1])
        $fragment += ",`n  `"$k`": `"$v`""
    }

    # Insert before final closing brace
    $lastBrace = $content.LastIndexOf('}')
    if ($lastBrace -lt 0) {
        $errors += "$locale : no closing brace found"
        return
    }
    $newContent = $content.Substring(0, $lastBrace) + $fragment + "`n}"

    # Validate JSON
    try {
        $null = $newContent | ConvertFrom-Json
    } catch {
        $errors += "$locale : JSON invalid after edit -- $_"
        return
    }

    [System.IO.File]::WriteAllText($file, $newContent, $utf8NoBom)
    Write-Host "  Updated: $locale"
    $updated++
}

Write-Host ""
Write-Host "Done. Updated $updated / $(($translations.Keys).Count) mapped + fallbacks files."
if ($errors.Count -gt 0) {
    Write-Host "ERRORS:"
    $errors | ForEach-Object { Write-Host "  $_" }
}
