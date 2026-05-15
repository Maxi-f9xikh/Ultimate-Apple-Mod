Add-Type -AssemblyName System.Drawing

$texDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\textures\item"

# Load iron_apple as shape/shading template
$script:tmpl = [System.Drawing.Bitmap]::new("$texDir\iron_apple.png")

# Build brightness-indexed color map from template (same approach as gen_textures.ps1)
$seen = [System.Collections.Generic.HashSet[string]]::new()
$rawC = [System.Collections.Generic.List[System.Drawing.Color]]::new()
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $p = $script:tmpl.GetPixel($x, $y)
        if ($p.A -gt 0) {
            $k = "$($p.R)_$($p.G)_$($p.B)"
            if ($seen.Add($k)) { $rawC.Add($p) }
        }
    }
}
$tC = [array]($rawC | Sort-Object { [int]$_.R + [int]$_.G + [int]$_.B })
$nC = $tC.Count
Write-Host "Template: $nC unique colors"

$script:ci = @{}
for ($i = 0; $i -lt $nC; $i++) {
    $c = $tC[$i]
    $script:ci["$($c.R)_$($c.G)_$($c.B)"] = $i
}

function HI([string]$h) {
    $r = [Convert]::ToInt32($h.Substring(1,2),16)
    $g = [Convert]::ToInt32($h.Substring(3,2),16)
    $b = [Convert]::ToInt32($h.Substring(5,2),16)
    [System.Drawing.Color]::FromArgb(255,$r,$g,$b).ToArgb()
}
function ToColor([int]$argb) { [System.Drawing.Color]::FromArgb($argb) }

function Make-Tex([string]$Name, [int[]]$Pal, [hashtable]$Ov) {
    $bmp = [System.Drawing.Bitmap]::new(16,16,[System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $src = $script:tmpl.GetPixel($x,$y)
            if ($src.A -eq 0) { continue }
            $k = "$($src.R)_$($src.G)_$($src.B)"
            $idx = if ($script:ci.ContainsKey($k)) { [int]$script:ci[$k] } else { 0 }
            $bmp.SetPixel($x,$y,(ToColor $Pal[$idx]))
        }
    }
    if ($Ov) {
        foreach ($key in $Ov.Keys) {
            $pts = $key.Split(',')
            $bmp.SetPixel([int]$pts[0],[int]$pts[1],(ToColor ([int]$Ov[$key])))
        }
    }
    $bmp.Save("$texDir\$Name.png",[System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "  OK  $Name.png"
}

# Shared: standard brown stem
$stemBrown = @{
    "9,1"=(HI "#6B4010"); "8,2"=(HI "#5A3010")
    "9,2"=(HI "#6B4010"); "8,3"=(HI "#5A3010")
}

# ---------------------------------------------------------------------------
# 1. DIRT APPLE — palette matches vanilla dirt block, clumpy texture spots
# ---------------------------------------------------------------------------
# Vanilla dirt block: primary ~#866043, dark ~#604A2A, highlight ~#A07848
$pDirt = [int[]]@(
    (HI "#160800"),(HI "#3A2010"),(HI "#5A3418"),(HI "#7A4C2C"),
    (HI "#926040"),(HI "#A87850"),(HI "#BE9060"),(HI "#D0A870"),(HI "#E0BE84"))

$oDirt = $stemBrown.Clone()
# Scattered dark-pixel clumps to suggest dirt's rough, lumpy texture
$oDirt["5,5"]  = (HI "#3A2010"); $oDirt["10,6"] = (HI "#3A2010")
$oDirt["4,8"]  = (HI "#3A2010"); $oDirt["12,8"] = (HI "#3A2010")
$oDirt["3,10"] = (HI "#5A3418"); $oDirt["11,10"]= (HI "#3A2010")
$oDirt["6,12"] = (HI "#3A2010"); $oDirt["9,13"] = (HI "#3A2010")
$oDirt["8,7"]  = (HI "#5A3418"); $oDirt["5,11"] = (HI "#5A3418")
Make-Tex "dirt_apple" $pDirt $oDirt

# ---------------------------------------------------------------------------
# 2. LAPIS LAZULI APPLE — deep blue body, natural navy variation, no gold
# ---------------------------------------------------------------------------
$pLapis = [int[]]@(
    (HI "#040818"),(HI "#0A1438"),(HI "#141E58"),(HI "#1E2E80"),
    (HI "#2840A8"),(HI "#3454C8"),(HI "#4468D4"),(HI "#5680E0"),(HI "#6C98E8"))

$oLapis = $stemBrown.Clone()
# Dark navy patches — natural lapis colour variation, no gold whatsoever
$oLapis["4,5"]  = (HI "#08102C"); $oLapis["11,6"] = (HI "#08102C")
$oLapis["3,9"]  = (HI "#0A1438"); $oLapis["12,9"] = (HI "#0A1438")
$oLapis["6,11"] = (HI "#08102C"); $oLapis["10,7"] = (HI "#0A1438")
Make-Tex "lapislazuli_apple" $pLapis $oLapis

# ---------------------------------------------------------------------------
# 3. EMERALD APPLE — clean vivid green, single specular highlight strip
# ---------------------------------------------------------------------------
$pEmerald = [int[]]@(
    (HI "#010A01"),(HI "#042004"),(HI "#093808"),(HI "#105010"),
    (HI "#1A6A1A"),(HI "#248424"),(HI "#309E30"),(HI "#44BA44"),(HI "#60D460"))

$oEmerald = $stemBrown.Clone()
# Single bright highlight column (upper-right facing light source)
$oEmerald["11,5"] = (HI "#80E880"); $oEmerald["12,6"] = (HI "#80E880")
$oEmerald["12,7"] = (HI "#60D460"); $oEmerald["12,8"] = (HI "#60D460")
# Deep shadow column (opposite side)
$oEmerald["3,10"] = (HI "#020A02"); $oEmerald["4,11"] = (HI "#020A02")
$oEmerald["4,12"] = (HI "#042004")
Make-Tex "emerald_apple" $pEmerald $oEmerald

# ---------------------------------------------------------------------------
# 4. NETHERITE APPLE — charcoal dark body, ancient-debris veins, gold trim
# ---------------------------------------------------------------------------
$pNetherite = [int[]]@(
    (HI "#080808"),(HI "#101010"),(HI "#1A1818"),(HI "#242220"),
    (HI "#302E28"),(HI "#3E3A30"),(HI "#4C4840"),(HI "#5C5650"),(HI "#6C6660"))

# Stem: dark gray to blend with netherite body
$oNetherite = @{
    "9,1"=(HI "#404040"); "8,2"=(HI "#363434")
    "9,2"=(HI "#404040"); "8,3"=(HI "#363434")
}
# Ancient-debris reddish-brown veins (two short horizontal streaks)
$oNetherite["5,6"]  = (HI "#8A3E16"); $oNetherite["6,6"]  = (HI "#A84E20")
$oNetherite["7,6"]  = (HI "#8A3E16")
$oNetherite["9,10"] = (HI "#8A3E16"); $oNetherite["10,10"] = (HI "#A84E20")
$oNetherite["11,10"] = (HI "#8A3E16")
# Gold trim — top and bottom accent pixels (netherite requires gold to craft)
$oNetherite["4,8"]  = (HI "#C8960C"); $oNetherite["11,8"] = (HI "#C8960C")
$oNetherite["5,13"] = (HI "#A87808"); $oNetherite["10,13"] = (HI "#A87808")
Make-Tex "netherite_apple" $pNetherite $oNetherite

# ---------------------------------------------------------------------------
# 5. TOTEM APPLE — gold body, Totem-of-Undying face:
#    dark brow ridges, white hollow 3x3 eyes with dark pupils,
#    U-shaped open grin with two white teeth.
# ---------------------------------------------------------------------------
$pTotem = [int[]]@(
    (HI "#1A1000"),(HI "#3A2400"),(HI "#5A3800"),(HI "#7A5000"),
    (HI "#9A6808"),(HI "#BA8418"),(HI "#D4A028"),(HI "#ECC048"),(HI "#FAE070"))

$oTotem = $stemBrown.Clone()
# Eyebrows — dark bar above each eye (totem's intense brow ridge)
$oTotem["4,5"]  = (HI "#1A1000"); $oTotem["5,5"]  = (HI "#1A1000"); $oTotem["6,5"]  = (HI "#1A1000")
$oTotem["9,5"]  = (HI "#1A1000"); $oTotem["10,5"] = (HI "#1A1000"); $oTotem["11,5"] = (HI "#1A1000")
# Left eye — hollow 3x3 white ring with dark pupil in centre
$oTotem["4,6"]  = (HI "#FFFFFF"); $oTotem["5,6"]  = (HI "#FFFFFF"); $oTotem["6,6"]  = (HI "#FFFFFF")
$oTotem["4,7"]  = (HI "#FFFFFF"); $oTotem["5,7"]  = (HI "#1A1000"); $oTotem["6,7"]  = (HI "#FFFFFF")
$oTotem["4,8"]  = (HI "#FFFFFF"); $oTotem["5,8"]  = (HI "#FFFFFF"); $oTotem["6,8"]  = (HI "#FFFFFF")
# Right eye — hollow 3x3 white ring with dark pupil in centre
$oTotem["9,6"]  = (HI "#FFFFFF"); $oTotem["10,6"] = (HI "#FFFFFF"); $oTotem["11,6"] = (HI "#FFFFFF")
$oTotem["9,7"]  = (HI "#FFFFFF"); $oTotem["10,7"] = (HI "#1A1000"); $oTotem["11,7"] = (HI "#FFFFFF")
$oTotem["9,8"]  = (HI "#FFFFFF"); $oTotem["10,8"] = (HI "#FFFFFF"); $oTotem["11,8"] = (HI "#FFFFFF")
# Nose bridge (two small dark pixels between the eyes)
$oTotem["7,8"]  = (HI "#3A2400"); $oTotem["8,8"]  = (HI "#3A2400")
# Wide U-shaped grin — sides + bottom outline, gold bg shows inside as "open mouth"
$oTotem["5,10"] = (HI "#1A1000"); $oTotem["10,10"]= (HI "#1A1000")
$oTotem["5,11"] = (HI "#1A1000"); $oTotem["10,11"]= (HI "#1A1000")
$oTotem["6,12"] = (HI "#1A1000"); $oTotem["7,12"] = (HI "#1A1000")
$oTotem["8,12"] = (HI "#1A1000"); $oTotem["9,12"] = (HI "#1A1000")
# Two white teeth visible inside the grin
$oTotem["7,11"] = (HI "#FFFFFF"); $oTotem["8,11"] = (HI "#FFFFFF")
Make-Tex "totem_apple" $pTotem $oTotem

# ---------------------------------------------------------------------------
# 6. QUANTUM APPLE — cosmic deep purple, rainbow sparkle constellation
#    (represents all effects simultaneously)
# ---------------------------------------------------------------------------
$pQuantum = [int[]]@(
    (HI "#02000A"),(HI "#08001C"),(HI "#120030"),(HI "#1E0048"),
    (HI "#2C0060"),(HI "#3C007C"),(HI "#500098"),(HI "#6600B4"),(HI "#8020CC"))

# Stem: deep purple-violet
$oQuantum = @{
    "9,1"=(HI "#8020CC"); "8,2"=(HI "#6600B4")
    "9,2"=(HI "#8020CC"); "8,3"=(HI "#6600B4")
}
# ── Rainbow sparkle constellation — one cluster per "effect colour" ──
# Red (Strength)
$oQuantum["4,5"]  = (HI "#FF3030"); $oQuantum["3,6"]  = (HI "#CC1010")
# Orange (Fire Resistance)
$oQuantum["11,5"] = (HI "#FF8800"); $oQuantum["12,6"] = (HI "#CC5500")
# Yellow (Night Vision / Lifesteal)
$oQuantum["6,4"]  = (HI "#FFE800"); $oQuantum["7,4"]  = (HI "#CCA800")
# Lime-green (Regeneration)
$oQuantum["9,4"]  = (HI "#88FF40"); $oQuantum["10,4"] = (HI "#55CC18")
# Cyan (Water Breathing / Time Freeze)
$oQuantum["3,9"]  = (HI "#00FFCC"); $oQuantum["3,10"] = (HI "#00B890")
# Blue (Absorption / Moon Gravity)
$oQuantum["12,9"] = (HI "#3090FF"); $oQuantum["12,10"] = (HI "#1060CC")
# Purple (Totem Protection)
$oQuantum["5,12"] = (HI "#CC44FF"); $oQuantum["4,13"] = (HI "#9920CC")
# Pink (Curse of Rotten reversed)
$oQuantum["9,12"] = (HI "#FF88CC"); $oQuantum["10,13"] = (HI "#CC4488")
# ── Bright white-violet starburst in the centre ──
$oQuantum["7,8"]  = (HI "#FFFFFF"); $oQuantum["8,8"]  = (HI "#FFFFFF")
$oQuantum["7,9"]  = (HI "#FFFFFF"); $oQuantum["8,9"]  = (HI "#FFFFFF")
$oQuantum["6,8"]  = (HI "#C8A0FF"); $oQuantum["9,8"]  = (HI "#C8A0FF")
$oQuantum["7,7"]  = (HI "#C8A0FF"); $oQuantum["8,7"]  = (HI "#C8A0FF")
$oQuantum["7,10"] = (HI "#C8A0FF"); $oQuantum["8,10"] = (HI "#C8A0FF")
Make-Tex "quantum_apple" $pQuantum $oQuantum

# ---------------------------------------------------------------------------
# 7. NUCLEAR APPLE — near-black body, blazing orange-white core, danger glow
# ---------------------------------------------------------------------------
$pNuclear = [int[]]@(
    (HI "#060000"),(HI "#1A0800"),(HI "#340C00"),(HI "#541600"),
    (HI "#7A2400"),(HI "#A63800"),(HI "#D45200"),(HI "#F07000"),(HI "#FF9020"))

# Stem: glowing orange-red
$oNuclear = @{
    "9,1"=(HI "#CC3800"); "8,2"=(HI "#A82C00")
    "9,2"=(HI "#CC3800"); "8,3"=(HI "#A82C00")
}
# Blazing nuclear core — white-hot centre with orange glow ring
$oNuclear["7,8"]  = (HI "#FFFFFF"); $oNuclear["8,8"]  = (HI "#FFFFFF")
$oNuclear["7,9"]  = (HI "#FFFFFF"); $oNuclear["8,9"]  = (HI "#FFFFFF")
$oNuclear["7,7"]  = (HI "#FFE000"); $oNuclear["8,7"]  = (HI "#FFE000")
$oNuclear["7,10"] = (HI "#FFE000"); $oNuclear["8,10"] = (HI "#FFE000")
$oNuclear["6,8"]  = (HI "#FFE000"); $oNuclear["6,9"]  = (HI "#FFE000")
$oNuclear["9,8"]  = (HI "#FFE000"); $oNuclear["9,9"]  = (HI "#FFE000")
# Outer glow ring — bright orange
$oNuclear["5,7"]  = (HI "#FF6000"); $oNuclear["6,7"]  = (HI "#FF8800")
$oNuclear["9,7"]  = (HI "#FF8800"); $oNuclear["10,7"] = (HI "#FF6000")
$oNuclear["5,10"] = (HI "#FF6000"); $oNuclear["6,10"] = (HI "#FF8800")
$oNuclear["9,10"] = (HI "#FF8800"); $oNuclear["10,10"]= (HI "#FF6000")
$oNuclear["5,8"]  = (HI "#FF6000"); $oNuclear["5,9"]  = (HI "#FF6000")
$oNuclear["10,8"] = (HI "#FF6000"); $oNuclear["10,9"] = (HI "#FF6000")
Make-Tex "nuclear_apple" $pNuclear $oNuclear

$script:tmpl.Dispose()
Write-Host "`nDone. 7 apple textures written."
