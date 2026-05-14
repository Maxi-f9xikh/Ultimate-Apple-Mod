Add-Type -AssemblyName System.Drawing

$texDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\textures\item"

# Load iron_apple as shape/shading template (same as gen_textures.ps1)
$script:tmpl = [System.Drawing.Bitmap]::new("$texDir\iron_apple.png")

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

# ---------------------------------------------------------------------------
# Shared stem colors
# ---------------------------------------------------------------------------
$stemBrown = @{
    "9,1"=(HI "#6B4010"); "8,2"=(HI "#5A3010"); "9,2"=(HI "#6B4010"); "8,3"=(HI "#5A3010")
}
# Waxed variants: golden stem indicates wax coating
$stemGold = @{
    "9,1"=(HI "#C89020"); "8,2"=(HI "#A87020"); "9,2"=(HI "#C89020"); "8,3"=(HI "#A87020")
}

# ---------------------------------------------------------------------------
# 1. COPPER (bare) — warm orange-red, matching vanilla fresh copper block
# ---------------------------------------------------------------------------
$pCopper = [int[]]@(
    (HI "#200A00"),(HI "#4A1800"),(HI "#7A3018"),(HI "#A84E28"),
    (HI "#C86838"),(HI "#D87848"),(HI "#E89260"),(HI "#F0A870"),(HI "#F8C090"))

$oCopper = $stemBrown.Clone()
Make-Tex "copper_apple" $pCopper $oCopper

# ---------------------------------------------------------------------------
# 2. EXPOSED COPPER — muted orange-pink body with early teal patches
# ---------------------------------------------------------------------------
$pExposed = [int[]]@(
    (HI "#241810"),(HI "#4A3228"),(HI "#7A5640"),(HI "#9E7258"),
    (HI "#B88870"),(HI "#C89A80"),(HI "#D4AA90"),(HI "#E0BCA0"),(HI "#ECCEB4"))

$oExposed = $stemBrown.Clone()
# Teal oxidation patches — small cluster lower-left and one spot upper-right
$oExposed["2,8"]  = (HI "#52907A"); $oExposed["3,8"]  = (HI "#407868")
$oExposed["2,9"]  = (HI "#407868"); $oExposed["3,9"]  = (HI "#52907A")
$oExposed["2,10"] = (HI "#52907A"); $oExposed["3,10"] = (HI "#407868")
$oExposed["11,5"] = (HI "#52907A"); $oExposed["12,5"] = (HI "#407868")
Make-Tex "exposed_copper_apple" $pExposed $oExposed

# ---------------------------------------------------------------------------
# 3. WEATHERED COPPER — green/teal dominant, orange remnants
# ---------------------------------------------------------------------------
$pWeathered = [int[]]@(
    (HI "#0E2018"),(HI "#1C3828"),(HI "#2C5040"),(HI "#3E6858"),
    (HI "#508070"),(HI "#609080"),(HI "#70A090"),(HI "#82B2A2"),(HI "#96C4B2"))

$oWeathered = $stemBrown.Clone()
# Copper remnant patches scattered in the green body
$oWeathered["5,5"]  = (HI "#A84E28"); $oWeathered["9,5"]  = (HI "#C06838")
$oWeathered["3,7"]  = (HI "#A84E28"); $oWeathered["11,7"] = (HI "#C06838")
$oWeathered["6,9"]  = (HI "#A84E28"); $oWeathered["10,9"] = (HI "#C06838")
$oWeathered["4,11"] = (HI "#A84E28")
Make-Tex "weathered_copper_apple" $pWeathered $oWeathered

# ---------------------------------------------------------------------------
# 4. OXIDIZED COPPER — fully teal/turquoise, matching vanilla oxidized copper
# ---------------------------------------------------------------------------
$pOxidized = [int[]]@(
    (HI "#0C1E1C"),(HI "#163430"),(HI "#204844"),(HI "#2E6058"),
    (HI "#3E7A70"),(HI "#4E8C80"),(HI "#5E9E90"),(HI "#6EB0A0"),(HI "#82C4B4"))

$oOxidized = $stemBrown.Clone()
Make-Tex "oxidized_copper_apple" $pOxidized $oOxidized

# ---------------------------------------------------------------------------
# 5-8. WAXED VARIANTS — identical body to stage, golden stem = wax indicator
# ---------------------------------------------------------------------------

$oWaxCopper = $stemGold.Clone()
Make-Tex "waxed_copper_apple" $pCopper $oWaxCopper

$oWaxExposed = $oExposed.Clone()
foreach ($k in $stemGold.Keys) { $oWaxExposed[$k] = $stemGold[$k] }
Make-Tex "waxed_exposed_copper_apple" $pExposed $oWaxExposed

$oWaxWeathered = $oWeathered.Clone()
foreach ($k in $stemGold.Keys) { $oWaxWeathered[$k] = $stemGold[$k] }
Make-Tex "waxed_weathered_copper_apple" $pWeathered $oWaxWeathered

$oWaxOxidized = $oOxidized.Clone()
foreach ($k in $stemGold.Keys) { $oWaxOxidized[$k] = $stemGold[$k] }
Make-Tex "waxed_oxidized_copper_apple" $pOxidized $oWaxOxidized

$script:tmpl.Dispose()
Write-Host "`nDone. 8 copper apple textures written."
