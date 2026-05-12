Add-Type -AssemblyName System.Drawing

$texDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\textures\item"

# Load iron_apple as shape/shading template
$script:tmpl = [System.Drawing.Bitmap]::new("$texDir\iron_apple.png")

# Collect unique colors sorted by brightness (R+G+B sum)
$seen = [System.Collections.Generic.HashSet[string]]::new()
$rawC = [System.Collections.Generic.List[System.Drawing.Color]]::new()
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $p = $script:tmpl.GetPixel($x, $y)
        if ($p.A -gt 0) {
            $k = ($p.R).ToString() + '_' + ($p.G).ToString() + '_' + ($p.B).ToString()
            if ($seen.Add($k)) { $rawC.Add($p) }
        }
    }
}
$tC = [array]($rawC | Sort-Object { [int]$_.R + [int]$_.G + [int]$_.B })
$nC = $tC.Count
Write-Host ("Template: " + $nC + " unique colors")

# Brightness-rank lookup: "R_G_B" -> 0..N
$script:ci = @{}
for ($i = 0; $i -lt $nC; $i++) {
    $c = $tC[$i]
    $k = ($c.R).ToString() + '_' + ($c.G).ToString() + '_' + ($c.B).ToString()
    $script:ci[$k] = $i
}

# "#RRGGBB" -> signed Int32 ARGB (A=255)
function HI([string]$h) {
    $r = [Convert]::ToInt32($h.Substring(1,2), 16)
    $g = [Convert]::ToInt32($h.Substring(3,2), 16)
    $b = [Convert]::ToInt32($h.Substring(5,2), 16)
    [System.Drawing.Color]::FromArgb(255, $r, $g, $b).ToArgb()
}

# Color from Int32 ARGB
function ToColor([int]$argb) { [System.Drawing.Color]::FromArgb($argb) }

# Build texture: remap template palette ranks to new colors, then apply overrides
# $Pal = Int32 ARGB array indexed by brightness rank (0 = darkest)
# $Ov  = hashtable "x,y" -> Int32 ARGB
function Make-Tex([string]$Name, [int[]]$Pal, [hashtable]$Ov) {
    $bmp = [System.Drawing.Bitmap]::new(16, 16,
           [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $src = $script:tmpl.GetPixel($x, $y)
            if ($src.A -eq 0) { continue }
            $k = ($src.R).ToString() + '_' + ($src.G).ToString() + '_' + ($src.B).ToString()
            $idx = if ($script:ci.ContainsKey($k)) { [int]$script:ci[$k] } else { 0 }
            $bmp.SetPixel($x, $y, (ToColor $Pal[$idx]))
        }
    }
    if ($Ov) {
        foreach ($key in $Ov.Keys) {
            $pts = $key.Split(',')
            $bmp.SetPixel([int]$pts[0], [int]$pts[1], (ToColor ([int]$Ov[$key])))
        }
    }
    $bmp.Save("$texDir\$Name.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host ("  Created " + $Name + ".png")
}

# Correct stem pixel positions (0-indexed):
#   (9,1)           <- top
#   (8,2) (9,2)     <- middle
#   (8,3)           <- bottom

# ===========================================================================
# 1. COPPER APPLE  -  warm copper body + green oxidation patches
# ===========================================================================
$pC = [int[]]@(
    (HI "#1E0800"),(HI "#4A1C00"),(HI "#7A3C14"),(HI "#A85A2A"),
    (HI "#C87B3D"),(HI "#D89550"),(HI "#E8AF6E"),(HI "#F2C88A"),(HI "#FDE0A8"))
$oC = @{
    # stem - brown
    "9,1"=(HI "#6B4010"); "8,2"=(HI "#5A3010"); "9,2"=(HI "#6B4010"); "8,3"=(HI "#5A3010")
    # teal oxidation cluster lower-left
    "2,7"=(HI "#3A6E50"); "3,7"=(HI "#52967A"); "2,8"=(HI "#52967A"); "3,8"=(HI "#3A6E50")
    "2,9"=(HI "#3A6E50"); "3,9"=(HI "#52967A"); "2,10"=(HI "#3A6E50"); "3,10"=(HI "#52967A")
    "2,11"=(HI "#3A6E50"); "3,11"=(HI "#266050"); "4,10"=(HI "#52967A"); "4,11"=(HI "#3A6E50")
}
Make-Tex "copper_apple" $pC $oC

# ===========================================================================
# 2. APPLE BOMB  -  black sphere, brown fuse + yellow spark, red stripe
# ===========================================================================
$pB = [int[]]@(
    (HI "#080808"),(HI "#141414"),(HI "#222222"),(HI "#303030"),
    (HI "#3E3E3E"),(HI "#505050"),(HI "#686868"),(HI "#848484"),(HI "#A0A0A0"))
$oB = @{
    # fuse: yellow spark at very top, brown rope below
    "9,1"=(HI "#FFD000")
    "8,2"=(HI "#7A4A18"); "9,2"=(HI "#6B4010")
    "8,3"=(HI "#6B4010")
    # red stripe y=7 (full body width x=2-13)
    "2,7"=(HI "#881010"); "3,7"=(HI "#CC2020"); "4,7"=(HI "#DD3030")
    "5,7"=(HI "#EE3535"); "6,7"=(HI "#CC2020")
    "7,7"=(HI "#EE3535"); "8,7"=(HI "#CC2020"); "9,7"=(HI "#EE3535")
    "10,7"=(HI "#CC2020"); "11,7"=(HI "#DD3030"); "12,7"=(HI "#CC2020"); "13,7"=(HI "#881010")
}
Make-Tex "apple_bomb" $pB $oB

# ===========================================================================
# 3. DRAGON APPLE  -  deep purple body, scattered magenta breath particles
# ===========================================================================
$pD = [int[]]@(
    (HI "#06000C"),(HI "#120020"),(HI "#200038"),(HI "#340055"),
    (HI "#4A0078"),(HI "#60109A"),(HI "#8030B8"),(HI "#A050D0"),(HI "#C878E8"))
$oDr = @{
    # stem - dark purple-brown
    "9,1"=(HI "#2A1040"); "8,2"=(HI "#1E0830"); "9,2"=(HI "#2A1040"); "8,3"=(HI "#1E0830")
    # dragon breath particles scattered across body
    "3,5"=(HI "#E040E8"); "7,5"=(HI "#F060F0"); "11,5"=(HI "#E040E8")
    "2,6"=(HI "#F060F0"); "10,6"=(HI "#E040E8"); "13,6"=(HI "#F060F0")
    "5,7"=(HI "#E040E8"); "9,7"=(HI "#F060F0")
    "3,8"=(HI "#F060F0"); "11,8"=(HI "#E040E8")
    "6,9"=(HI "#E040E8"); "10,9"=(HI "#F060F0")
    "4,10"=(HI "#E040E8"); "8,11"=(HI "#F060F0")
}
Make-Tex "dragon_apple" $pD $oDr

# ===========================================================================
# 4. NETHER STAR APPLE  -  bright gold body, white 8-pointed star
# ===========================================================================
$pN = [int[]]@(
    (HI "#2E1E00"),(HI "#4A3200"),(HI "#6E4C00"),(HI "#927008"),
    (HI "#B09020"),(HI "#C8A835"),(HI "#DCC050"),(HI "#EED870"),(HI "#FFF090"))
$oN = @{
    # stem - dark gold/brown
    "9,1"=(HI "#4A3200"); "8,2"=(HI "#3A2800"); "9,2"=(HI "#4A3200"); "8,3"=(HI "#3A2800")
    # 8-pointed star: vertical arm
    "7,4"=(HI "#FFFFFF"); "7,5"=(HI "#FFFFF0"); "7,6"=(HI "#FFFFFF"); "7,7"=(HI "#FFFFF0")
    "7,8"=(HI "#FFFFFF")
    "7,9"=(HI "#FFFFF0"); "7,10"=(HI "#FFFFFF"); "7,11"=(HI "#FFFFF0"); "7,12"=(HI "#FFFFFF")
    # horizontal arm
    "3,8"=(HI "#FFFFFF"); "4,8"=(HI "#FFFFF0"); "5,8"=(HI "#FFFFFF"); "6,8"=(HI "#FFFFF0")
    "8,8"=(HI "#FFFFF0"); "9,8"=(HI "#FFFFFF"); "10,8"=(HI "#FFFFF0"); "11,8"=(HI "#FFFFFF")
    # diagonal arms
    "4,5"=(HI "#FFFFFF"); "5,6"=(HI "#FFFFF0"); "6,7"=(HI "#FFFFFF")
    "8,7"=(HI "#FFFFFF"); "9,6"=(HI "#FFFFF0"); "10,5"=(HI "#FFFFFF")
    "4,11"=(HI "#FFFFFF"); "5,10"=(HI "#FFFFF0"); "6,9"=(HI "#FFFFFF")
    "8,9"=(HI "#FFFFFF"); "9,10"=(HI "#FFFFF0"); "10,11"=(HI "#FFFFFF")
}
Make-Tex "nether_star_apple" $pN $oN

# ===========================================================================
# 5. TOTEM APPLE  -  yellow-gold body, proper outlined eyes + totem face
#    Matches the Minecraft Totem of Undying: large bordered eyes, nose, teeth
# ===========================================================================
$pTo = [int[]]@(
    (HI "#1A1000"),(HI "#362000"),(HI "#604000"),(HI "#8A6010"),
    (HI "#B08428"),(HI "#D0A030"),(HI "#E4BC40"),(HI "#F0D055"),(HI "#FAE88A"))
$oTo = @{
    # stem - dark golden-brown
    "9,1"=(HI "#5C3800"); "8,2"=(HI "#4A2C00"); "9,2"=(HI "#5C3800"); "8,3"=(HI "#4A2C00")
    # left eye: 4x4 box (x=3-6, y=5-8), dark border, white fill, pupil at (5,7)
    "3,5"=(HI "#1A0A00"); "4,5"=(HI "#1A0A00"); "5,5"=(HI "#1A0A00"); "6,5"=(HI "#1A0A00")
    "3,6"=(HI "#1A0A00"); "4,6"=(HI "#FFFFFF"); "5,6"=(HI "#FFFFFF"); "6,6"=(HI "#1A0A00")
    "3,7"=(HI "#1A0A00"); "4,7"=(HI "#FFFFFF"); "5,7"=(HI "#1A0A00"); "6,7"=(HI "#1A0A00")
    "3,8"=(HI "#1A0A00"); "4,8"=(HI "#1A0A00"); "5,8"=(HI "#1A0A00"); "6,8"=(HI "#1A0A00")
    # right eye: 4x4 box (x=9-12, y=5-8), dark border, white fill, pupil at (11,7)
    "9,5"=(HI "#1A0A00"); "10,5"=(HI "#1A0A00"); "11,5"=(HI "#1A0A00"); "12,5"=(HI "#1A0A00")
    "9,6"=(HI "#1A0A00"); "10,6"=(HI "#FFFFFF"); "11,6"=(HI "#FFFFFF"); "12,6"=(HI "#1A0A00")
    "9,7"=(HI "#1A0A00"); "10,7"=(HI "#FFFFFF"); "11,7"=(HI "#1A0A00"); "12,7"=(HI "#1A0A00")
    "9,8"=(HI "#1A0A00"); "10,8"=(HI "#1A0A00"); "11,8"=(HI "#1A0A00"); "12,8"=(HI "#1A0A00")
    # nose: two dark dots
    "7,9"=(HI "#1A0A00"); "8,9"=(HI "#1A0A00")
    # mouth: solid dark upper lip + alternating white teeth / dark gaps below
    "5,10"=(HI "#1A0A00"); "6,10"=(HI "#1A0A00"); "7,10"=(HI "#1A0A00")
    "8,10"=(HI "#1A0A00"); "9,10"=(HI "#1A0A00"); "10,10"=(HI "#1A0A00")
    "5,11"=(HI "#FFFFFF"); "6,11"=(HI "#1A0A00"); "7,11"=(HI "#FFFFFF")
    "8,11"=(HI "#1A0A00"); "9,11"=(HI "#FFFFFF"); "10,11"=(HI "#1A0A00")
}
Make-Tex "totem_apple" $pTo $oTo

# ===========================================================================
# 6. VOID APPLE  -  near-black body, bright purple portal glow
# ===========================================================================
$pV = [int[]]@(
    (HI "#000006"),(HI "#06000C"),(HI "#0E0018"),(HI "#180025"),
    (HI "#240035"),(HI "#360048"),(HI "#500068"),(HI "#700090"),(HI "#9800C0"))
$oV = @{
    # stem - very dark purple (nearly invisible, void-like)
    "9,1"=(HI "#0C0015"); "8,2"=(HI "#06000C"); "9,2"=(HI "#0C0015"); "8,3"=(HI "#06000C")
    # void portal oval in center of apple
    "6,6"=(HI "#8820C8"); "7,6"=(HI "#A030E0"); "8,6"=(HI "#8820C8")
    "5,7"=(HI "#6810A8"); "6,7"=(HI "#C040F0"); "7,7"=(HI "#D850FF")
    "8,7"=(HI "#C040F0"); "9,7"=(HI "#6810A8")
    "5,8"=(HI "#6810A8"); "6,8"=(HI "#C040F0"); "7,8"=(HI "#D850FF")
    "8,8"=(HI "#C040F0"); "9,8"=(HI "#6810A8")
    "6,9"=(HI "#8820C8"); "7,9"=(HI "#A030E0"); "8,9"=(HI "#8820C8")
}
Make-Tex "void_apple" $pV $oV

# ===========================================================================
# 7. TIME FREEZE APPLE  -  ice blue body, white snowflake crystal
# ===========================================================================
$pTF = [int[]]@(
    (HI "#081828"),(HI "#102840"),(HI "#1C3C5E"),(HI "#30557E"),
    (HI "#487099"),(HI "#6890B4"),(HI "#88AECE"),(HI "#A8C8E4"),(HI "#CCE4F4"))
$oTF = @{
    # stem - icy blue-gray (frozen appearance)
    "9,1"=(HI "#B0C8E0"); "8,2"=(HI "#90A8C8"); "9,2"=(HI "#B0C8E0"); "8,3"=(HI "#90A8C8")
    # snowflake vertical arm
    "7,4"=(HI "#FFFFFF"); "7,5"=(HI "#F0F4FF"); "7,6"=(HI "#FFFFFF"); "7,7"=(HI "#F0F4FF")
    "7,8"=(HI "#FFFFFF")
    "7,9"=(HI "#F0F4FF"); "7,10"=(HI "#FFFFFF"); "7,11"=(HI "#F0F4FF"); "7,12"=(HI "#FFFFFF")
    # snowflake horizontal arm
    "3,8"=(HI "#FFFFFF"); "4,8"=(HI "#F0F4FF"); "5,8"=(HI "#FFFFFF"); "6,8"=(HI "#F0F4FF")
    "8,8"=(HI "#F0F4FF"); "9,8"=(HI "#FFFFFF"); "10,8"=(HI "#F0F4FF"); "11,8"=(HI "#FFFFFF")
    # diagonal sparkle tips
    "5,6"=(HI "#F0F4FF"); "9,6"=(HI "#F0F4FF"); "5,10"=(HI "#F0F4FF"); "9,10"=(HI "#F0F4FF")
    # side branches on vertical arm
    "6,5"=(HI "#E0EEFF"); "8,5"=(HI "#E0EEFF"); "6,11"=(HI "#E0EEFF"); "8,11"=(HI "#E0EEFF")
}
Make-Tex "time_freeze_apple" $pTF $oTF

# ===========================================================================
# 8. PRISM APPLE  -  prismarine teal body, bright crystal glow center,
#    dark prismarine edge patches + scattered sea sparkles
# ===========================================================================
$pPr = [int[]]@(
    (HI "#0E2C28"),(HI "#1A4440"),(HI "#245A54"),(HI "#2E7268"),
    (HI "#3A9080"),(HI "#4EAE9A"),(HI "#66C8B2"),(HI "#82DEC8"),(HI "#A0F0DC"))
$oPr = @{
    # stem - brown (vanilla-style)
    "9,1"=(HI "#6B4010"); "8,2"=(HI "#5A3010"); "9,2"=(HI "#6B4010"); "8,3"=(HI "#5A3010")
    # crystal glow at center: bright near-white aqua core fading outward
    "7,6"=(HI "#A8F8F0"); "8,6"=(HI "#A8F8F0")
    "6,7"=(HI "#8CF0E0"); "7,7"=(HI "#D0FFFC"); "8,7"=(HI "#D0FFFC"); "9,7"=(HI "#8CF0E0")
    "6,8"=(HI "#8CF0E0"); "7,8"=(HI "#D0FFFC"); "8,8"=(HI "#D0FFFC"); "9,8"=(HI "#8CF0E0")
    "7,9"=(HI "#A8F8F0"); "8,9"=(HI "#A8F8F0")
    # dark prismarine texture patches at body edges
    "2,6"=(HI "#0E2C28"); "3,6"=(HI "#1A4440")
    "2,7"=(HI "#1A4440"); "2,8"=(HI "#0E2C28")
    "12,6"=(HI "#1A4440"); "13,6"=(HI "#0E2C28")
    "12,7"=(HI "#0E2C28"); "13,7"=(HI "#1A4440")
    "3,11"=(HI "#0E2C28"); "3,12"=(HI "#1A4440")
    "11,11"=(HI "#1A4440"); "12,12"=(HI "#0E2C28")
    # sea sparkle highlights scattered around
    "5,5"=(HI "#82DEC8"); "10,5"=(HI "#82DEC8")
    "4,10"=(HI "#66C8B2"); "11,10"=(HI "#66C8B2")
    "5,13"=(HI "#82DEC8"); "9,13"=(HI "#82DEC8")
}
Make-Tex "prism_apple" $pPr $oPr

# ===========================================================================
# 9. TNT APPLE  -  red body, gray fuse + yellow spark, central striped band
# ===========================================================================
$pTNT = [int[]]@(
    (HI "#200000"),(HI "#3A0000"),(HI "#5A0808"),(HI "#7A1010"),
    (HI "#9A2020"),(HI "#BC3030"),(HI "#D84040"),(HI "#EE6060"),(HI "#FFB0B0"))
$oTNT = @{
    # fuse: yellow spark, then gray rope (correct stem positions)
    "9,1"=(HI "#FFD000")
    "8,2"=(HI "#8A8070"); "9,2"=(HI "#7A7060")
    "8,3"=(HI "#7A7060")
    # TNT label band y=7: light gray
    "2,7"=(HI "#C8C8C8"); "3,7"=(HI "#D0D0D0"); "4,7"=(HI "#C8C8C8"); "5,7"=(HI "#D0D0D0")
    "6,7"=(HI "#C8C8C8"); "7,7"=(HI "#D0D0D0"); "8,7"=(HI "#C8C8C8"); "9,7"=(HI "#D0D0D0")
    "10,7"=(HI "#C8C8C8"); "11,7"=(HI "#D0D0D0"); "12,7"=(HI "#C8C8C8"); "13,7"=(HI "#D0D0D0")
    # TNT label band y=8: alternating black/white stripes (TNT text area)
    "2,8"=(HI "#E0E0E0"); "3,8"=(HI "#202020"); "4,8"=(HI "#E0E0E0"); "5,8"=(HI "#202020")
    "6,8"=(HI "#E0E0E0"); "7,8"=(HI "#202020"); "8,8"=(HI "#E0E0E0"); "9,8"=(HI "#202020")
    "10,8"=(HI "#E0E0E0"); "11,8"=(HI "#202020"); "12,8"=(HI "#E0E0E0"); "13,8"=(HI "#202020")
    # TNT label band y=9: light gray
    "2,9"=(HI "#C8C8C8"); "3,9"=(HI "#D0D0D0"); "4,9"=(HI "#C8C8C8"); "5,9"=(HI "#D0D0D0")
    "6,9"=(HI "#C8C8C8"); "7,9"=(HI "#D0D0D0"); "8,9"=(HI "#C8C8C8"); "9,9"=(HI "#D0D0D0")
    "10,9"=(HI "#C8C8C8"); "11,9"=(HI "#D0D0D0"); "12,9"=(HI "#C8C8C8"); "13,9"=(HI "#D0D0D0")
}
Make-Tex "tnt_apple" $pTNT $oTNT

# ===========================================================================
# 10. COAL APPLE  -  black coal body, dark brown veins, brown stem
# ===========================================================================
$pCo = [int[]]@(
    (HI "#0A0808"),(HI "#181412"),(HI "#242020"),(HI "#342C28"),
    (HI "#443C38"),(HI "#564E4A"),(HI "#6A605C"),(HI "#807874"),(HI "#A09490"))
$oCo = @{
    # stem - earthy brown (like a real apple stem)
    "9,1"=(HI "#6B4010"); "8,2"=(HI "#5A3010"); "9,2"=(HI "#6B4010"); "8,3"=(HI "#5A3010")
    # coal vein 1 - diagonal crack upper-left
    "3,5"=(HI "#2A1A10"); "4,6"=(HI "#1E1008"); "5,6"=(HI "#2A1A10")
    "5,7"=(HI "#1E1008"); "6,7"=(HI "#2A1A10")
    # coal vein 2 - horizontal streak middle-right
    "8,9"=(HI "#1E1008");  "9,9"=(HI "#2A1A10"); "10,9"=(HI "#1E1008"); "11,9"=(HI "#2A1A10")
    # coal vein 3 - lower cluster
    "4,11"=(HI "#2A1A10"); "5,11"=(HI "#1E1008"); "5,12"=(HI "#2A1A10")
    "9,12"=(HI "#1E1008"); "10,12"=(HI "#2A1A10")
    # subtle gray sheen highlight (top-right of apple body)
    "10,5"=(HI "#8A7C78"); "11,5"=(HI "#7A6C68")
    "11,6"=(HI "#7A6C68"); "12,6"=(HI "#6A5C58")
}
Make-Tex "coal_apple" $pCo $oCo

# ===========================================================================
# 11. EXPOSED COPPER APPLE  -  warm copper body + small early teal patches
# ===========================================================================
$pEx = [int[]]@(
    (HI "#1E0800"),(HI "#4A1C00"),(HI "#7A3C14"),(HI "#A85A2A"),
    (HI "#C87B3D"),(HI "#D89550"),(HI "#E8AF6E"),(HI "#F2C88A"),(HI "#FDE0A8"))
$oEx = @{
    "9,1"=(HI "#6B4010"); "8,2"=(HI "#5A3010"); "9,2"=(HI "#6B4010"); "8,3"=(HI "#5A3010")
    # small teal patch top-right
    "10,4"=(HI "#52967A"); "11,4"=(HI "#3A6E50")
    "10,5"=(HI "#3A6E50"); "11,5"=(HI "#52967A"); "12,5"=(HI "#3A6E50")
    # tiny dot lower-left
    "3,11"=(HI "#52967A"); "4,11"=(HI "#3A6E50")
}
Make-Tex "exposed_copper_apple" $pEx $oEx

# ===========================================================================
# 12. WEATHERED COPPER APPLE  -  teal dominant, copper hints remain
# ===========================================================================
$pWe = [int[]]@(
    (HI "#0E2818"),(HI "#1A4030"),(HI "#265848"),(HI "#347060"),
    (HI "#428C78"),(HI "#56A890"),(HI "#6EC4A8"),(HI "#88D8BC"),(HI "#A4ECD0"))
$oWe = @{
    "9,1"=(HI "#6B4010"); "8,2"=(HI "#5A3010"); "9,2"=(HI "#6B4010"); "8,3"=(HI "#5A3010")
    # copper remnant patches
    "3,5"=(HI "#C87B3D"); "4,5"=(HI "#A85A2A"); "3,6"=(HI "#A85A2A")
    "10,9"=(HI "#C87B3D"); "11,9"=(HI "#A85A2A"); "11,10"=(HI "#C87B3D")
    "6,12"=(HI "#A85A2A"); "7,12"=(HI "#C87B3D")
}
Make-Tex "weathered_copper_apple" $pWe $oWe

# ===========================================================================
# 13. OXIDIZED COPPER APPLE  -  fully teal/green, no copper remaining
# ===========================================================================
$pOx = [int[]]@(
    (HI "#0A2018"),(HI "#143428"),(HI "#1E4838"),(HI "#285C48"),
    (HI "#347060"),(HI "#408878"),(HI "#50A090"),(HI "#64B8A8"),(HI "#7ED0BC"))
$oOx = @{
    # stem - darkened, greenish-brown (fully oxidised)
    "9,1"=(HI "#2A4030"); "8,2"=(HI "#1E3025"); "9,2"=(HI "#2A4030"); "8,3"=(HI "#1E3025")
    # subtle dark texture variation
    "4,6"=(HI "#1E4838");  "5,6"=(HI "#143428")
    "9,8"=(HI "#143428");  "10,8"=(HI "#1E4838")
    "3,10"=(HI "#1E4838"); "4,10"=(HI "#143428"); "3,11"=(HI "#143428")
}
Make-Tex "oxidized_copper_apple" $pOx $oOx

# ===========================================================================
# Helper: add yellow honeycomb dots to an existing bitmap
# ===========================================================================
function Add-WaxDots([System.Drawing.Bitmap]$bmp) {
    $wax = [System.Drawing.Color]::FromArgb(255, 240, 180, 30)  # honey yellow
    $waxDark = [System.Drawing.Color]::FromArgb(255, 200, 140, 10)
    # Small hex-dot pattern
    foreach ($pt in @("5,4","9,4","13,4","3,7","7,7","11,7","5,10","9,10","13,10","3,13","7,13","11,13")) {
        $xy = $pt.Split(','); $bmp.SetPixel([int]$xy[0],[int]$xy[1],$wax)
    }
    foreach ($pt in @("6,4","10,4","4,7","8,7","12,7","6,10","10,10","4,13","8,13","12,13")) {
        $xy = $pt.Split(','); $bmp.SetPixel([int]$xy[0],[int]$xy[1],$waxDark)
    }
}

# ===========================================================================
# 14-17. WAXED VARIANTS  -  base stage + honeycomb dot pattern overlay
# ===========================================================================
$waxedPairs = @(
    @("copper_apple",           "waxed_copper_apple"),
    @("exposed_copper_apple",   "waxed_exposed_copper_apple"),
    @("weathered_copper_apple", "waxed_weathered_copper_apple"),
    @("oxidized_copper_apple",  "waxed_oxidized_copper_apple")
)
foreach ($pair in $waxedPairs) {
    $srcPath = "$texDir\$($pair[0]).png"
    $dstName = $pair[1]
    $src = [System.Drawing.Bitmap]::new($srcPath)
    $dst = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    # Copy all pixels
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $dst.SetPixel($x, $y, $src.GetPixel($x, $y))
        }
    }
    # Overlay wax dots (only on non-transparent pixels)
    $wax     = [System.Drawing.Color]::FromArgb(255, 240, 180, 30)
    $waxDark = [System.Drawing.Color]::FromArgb(255, 200, 140, 10)
    foreach ($pt in @("5,4","9,4","13,4","3,7","7,7","11,7","5,10","9,10","13,10","7,13","11,13")) {
        $xy = $pt.Split(','); $px = [int]$xy[0]; $py = [int]$xy[1]
        if ($dst.GetPixel($px,$py).A -gt 0) { $dst.SetPixel($px,$py,$wax) }
    }
    foreach ($pt in @("6,4","10,4","4,7","8,7","12,7","6,10","10,10","8,13","12,13")) {
        $xy = $pt.Split(','); $px = [int]$xy[0]; $py = [int]$xy[1]
        if ($dst.GetPixel($px,$py).A -gt 0) { $dst.SetPixel($px,$py,$waxDark) }
    }
    $dst.Save("$texDir\$dstName.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $src.Dispose(); $dst.Dispose()
    Write-Host "  Created $dstName.png"
}

$script:tmpl.Dispose()
Write-Host "All 17 item textures done!"

# ===========================================================================
# MIXER BLOCK TEXTURE  (64×64 atlas, texture_size [64,64])
# UV unit = 4px.  Key face regions (UV × 4 = pixel):
#   x:0-8,  y:0-11   = side body east (right side outer)
#   x:0-8,  y:11-22  = FRONT north face  ← most visible
#   x:16-24,y:11-22  = side body west (left side outer)
#   x:8-16, y:0-11   = inner right
#   x:16-24,y:0-11   = inner left
#   x:24-30,y:0-22   = thin front strips
#   x:0-30, y:22-33  = back faces
#   x:0-30, y:33-64  = legs / feet / small corner details
#   x:30-64,y:0-64   = remaining small pieces
# Design goal: real blender look — dark collar top, silver body, dark base feet
# ===========================================================================
$blockTexDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\textures\block"
New-Item -ItemType Directory -Force -Path $blockTexDir | Out-Null

$mixBmp = New-Object System.Drawing.Bitmap(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$mg = [System.Drawing.Graphics]::FromImage($mixBmp)
$mg.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor

# Helper: create a SolidBrush from RGB
function MB { param([int]$r,[int]$g,[int]$b)
    [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,$r,$g,$b)) }

# ── Real blender palette: very light silver body, near-black collar/feet ──────
$bBlack  = MB  18  18  18   # near black  — collar top, base feet
$bDark   = MB  45  45  45   # dark        — edges, inner/back faces
$bMid    = MB 110 110 110   # mid gray    — secondary faces, transitions
$bSilver = MB 185 185 185   # light silver — main visible body (like brushed alu)
$bSheen  = MB 225 225 225   # bright sheen — highlight lines

# ── 1. Fill entire atlas with dark as safe default ───────────────────────────
$mg.Clear([System.Drawing.Color]::FromArgb(255, 45, 45, 45))

# ── 2. Feet / legs (y:33-64 and x:30-64) — near black ───────────────────────
$mg.FillRectangle(($bBlack), 0,  33, 64, 31)
$mg.FillRectangle(($bDark),  30,  0, 34, 33)

# ── 3. Back/inner faces (x:0-30, y:22-33) ────────────────────────────────────
$mg.FillRectangle(($bDark),  0,  22, 30, 11)

# ── 4. Inner side faces (x:8-24, y:0-11) ─────────────────────────────────────
$mg.FillRectangle(($bDark),  8,   0,  8, 11)
$mg.FillRectangle(($bMid),  16,   0,  8, 11)

# ── 5. Thin edge strips (x:24-30, y:0-22) ────────────────────────────────────
$mg.FillRectangle(($bMid),  24,   0,  6, 22)

# ── 6. Side right outer (x:0-8, y:0-11) ─────────────────────────────────────
$mg.FillRectangle(($bSilver),  0,  0,  8, 11)   # silver main face
$mg.FillRectangle(($bBlack),   0,  0,  8,  3)   # BLACK collar at top
$mg.FillRectangle(($bDark),    0,  3,  1,  8)   # shadow left edge
$mg.FillRectangle(($bSheen),   6,  4,  1,  6)   # bright sheen stripe

# ── 7. FRONT face (x:0-8, y:11-22) — most visible face ──────────────────────
$mg.FillRectangle(($bSilver),  0, 11,  8, 11)   # silver body
$mg.FillRectangle(($bBlack),   0, 11,  8,  3)   # BLACK collar band
$mg.FillRectangle(($bBlack),   0, 19,  8,  3)   # BLACK base band
$mg.FillRectangle(($bDark),    0, 14,  1,  5)   # left shadow
$mg.FillRectangle(($bDark),    7, 14,  1,  5)   # right shadow
$mg.FillRectangle(($bSheen),   2, 15,  4,  1)   # horizontal sheen line
$mg.FillRectangle(($bSheen),   2, 17,  3,  1)   # second sheen line

# ── 8. Front back panel (x:8-16, y:11-22) ────────────────────────────────────
$mg.FillRectangle(($bMid),   8, 11,  8, 11)
$mg.FillRectangle(($bDark),  8, 11,  1, 11)

# ── 9. Side left outer (x:16-24, y:11-22) ────────────────────────────────────
$mg.FillRectangle(($bSilver), 16, 11,  8, 11)   # silver main face
$mg.FillRectangle(($bBlack),  16, 11,  8,  3)   # BLACK collar band
$mg.FillRectangle(($bBlack),  16, 19,  8,  3)   # BLACK base band
$mg.FillRectangle(($bDark),   16, 14,  1,  5)
$mg.FillRectangle(($bDark),   23, 14,  1,  5)
$mg.FillRectangle(($bSheen),  18, 15,  4,  1)

$mg.Dispose()
$mixBmp.Save("$blockTexDir\mixer_block_unten.png", [System.Drawing.Imaging.ImageFormat]::Png)
$mixBmp.Dispose()
Write-Host "  Created block/mixer_block_unten.png"

# ===========================================================================
# GREEN SHAKE GLASS TEXTURE  (16×16)
# Used by mixer_block_mit_shake.json for the jar walls when a shake is ready.
# Looks like green stained glass / smoothie jar.
# ===========================================================================
$glassBmp = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

# Fill transparent
for ($gy = 0; $gy -lt 16; $gy++) {
    for ($gx = 0; $gx -lt 16; $gx++) {
        $glassBmp.SetPixel($gx, $gy, [System.Drawing.Color]::Transparent)
    }
}

# Glass look: light greenish-white frame border, bright green fill, inner highlight
$gFrame  = [System.Drawing.Color]::FromArgb(255, 190, 230, 170)  # light green-white (frame)
$gFill   = [System.Drawing.Color]::FromArgb(255,  88, 185,  68)  # fresh green (body)
$gDark   = [System.Drawing.Color]::FromArgb(255,  52, 130,  40)  # darker green (shadow side)
$gSheen  = [System.Drawing.Color]::FromArgb(255, 210, 245, 195)  # near-white highlight

# Outer border
for ($i = 0; $i -lt 16; $i++) {
    $glassBmp.SetPixel($i,  0, $gFrame)
    $glassBmp.SetPixel($i, 15, $gFrame)
    $glassBmp.SetPixel( 0, $i, $gFrame)
    $glassBmp.SetPixel(15, $i, $gFrame)
}
# Inner fill (green body)
for ($gy = 1; $gy -lt 15; $gy++) {
    for ($gx = 1; $gx -lt 15; $gx++) {
        $glassBmp.SetPixel($gx, $gy, $gFill)
    }
}
# Inner top-left highlight (like light reflection on glass)
for ($i = 1; $i -lt 15; $i++) {
    $glassBmp.SetPixel($i,  1, $gSheen)   # top inner row
    $glassBmp.SetPixel( 1, $i, $gSheen)   # left inner column
}
# Bottom-right shadow (depth)
for ($i = 1; $i -lt 15; $i++) {
    $glassBmp.SetPixel($i, 14, $gDark)
    $glassBmp.SetPixel(14, $i, $gDark)
}

$glassBmp.Save("$blockTexDir\mixer_block_glas_shake.png", [System.Drawing.Imaging.ImageFormat]::Png)
$glassBmp.Dispose()
Write-Host "  Created block/mixer_block_glas_shake.png"

# ===========================================================================
# MIXER BODY TEXTURE  (16×16)  — silver brushed-aluminum look
# ===========================================================================
$bodyBmp = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$bodyG   = [System.Drawing.Graphics]::FromImage($bodyBmp)
$bodyG.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor

function SB([int]$r,[int]$g,[int]$b) {
    [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(255,$r,$g,$b)) }

# Base silver fill
$bodyG.Clear([System.Drawing.Color]::FromArgb(255, 172, 172, 172))
# Top dark band (collar transition)
$bodyG.FillRectangle((SB 40 40 40),  0,  0, 16, 1)
$bodyG.FillRectangle((SB 60 60 60),  0,  1, 16, 1)
# Highlight just below top band
$bodyG.FillRectangle((SB 200 200 200), 0, 2, 16, 1)
# Center sheen lines
$bodyG.FillRectangle((SB 192 192 192), 0, 6, 16, 1)
$bodyG.FillRectangle((SB 198 198 198), 0, 7, 16, 2)
$bodyG.FillRectangle((SB 192 192 192), 0, 9, 16, 1)
# Bottom dark band (foot transition)
$bodyG.FillRectangle((SB 60 60 60),  0, 14, 16, 1)
$bodyG.FillRectangle((SB 40 40 40),  0, 15, 16, 1)
# Left edge shadow
$bodyG.FillRectangle((SB 120 120 120), 0, 0, 1, 16)
# Right edge shadow
$bodyG.FillRectangle((SB 135 135 135), 15, 0, 1, 16)

$bodyG.Dispose()
$bodyBmp.Save("$blockTexDir\mixer_body.png", [System.Drawing.Imaging.ImageFormat]::Png)
$bodyBmp.Dispose()
Write-Host "  Created block/mixer_body.png"

# ===========================================================================
# MIXER DARK TEXTURE  (16×16)  — near-black for collar and feet
# ===========================================================================
$darkBmp = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$darkG   = [System.Drawing.Graphics]::FromImage($darkBmp)

$darkG.Clear([System.Drawing.Color]::FromArgb(255, 22, 22, 22))
# Slight top/bottom edge variation
$darkG.FillRectangle((SB 12 12 12),  0,  0, 16, 1)
$darkG.FillRectangle((SB 32 32 32),  0,  1, 16, 1)
$darkG.FillRectangle((SB 32 32 32),  0, 14, 16, 1)
$darkG.FillRectangle((SB 12 12 12),  0, 15, 16, 1)
# Visible left edge (slight highlight so not totally flat)
$darkG.FillRectangle((SB 42 42 42),  0,  0, 1, 16)

$darkG.Dispose()
$darkBmp.Save("$blockTexDir\mixer_dark.png", [System.Drawing.Imaging.ImageFormat]::Png)
$darkBmp.Dispose()
Write-Host "  Created block/mixer_dark.png"

# ===========================================================================
# MIXER JAR TEXTURE  (16×16)  — clear glass cup walls (transparent center)
# ===========================================================================
$jarBmp = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$jarG   = [System.Drawing.Graphics]::FromImage($jarBmp)
# Fill fully transparent
$jarG.Clear([System.Drawing.Color]::Transparent)
$jarG.Dispose()

# Draw glass border: 2px white/light-blue rim, transparent interior
$rimColor    = [System.Drawing.Color]::FromArgb(255, 210, 235, 240)  # light ice-blue
$rimDark     = [System.Drawing.Color]::FromArgb(180, 150, 195, 205)  # semi-transparent inner rim
$rimHighlight= [System.Drawing.Color]::FromArgb(255, 240, 250, 255)  # bright top

for ($i = 0; $i -lt 16; $i++) {
    # Top/bottom solid rim
    $jarBmp.SetPixel($i,  0, $rimHighlight)
    $jarBmp.SetPixel($i,  1, $rimColor)
    $jarBmp.SetPixel($i, 14, $rimDark)
    $jarBmp.SetPixel($i, 15, $rimDark)
    # Left/right solid rim
    $jarBmp.SetPixel( 0, $i, $rimColor)
    $jarBmp.SetPixel( 1, $i, $rimDark)
    $jarBmp.SetPixel(14, $i, $rimDark)
    $jarBmp.SetPixel(15, $i, $rimColor)
}
# Small highlight reflections on the glass face
$jarBmp.SetPixel(3,  4, [System.Drawing.Color]::FromArgb(200, 240, 250, 255))
$jarBmp.SetPixel(3,  5, [System.Drawing.Color]::FromArgb(150, 240, 250, 255))
$jarBmp.SetPixel(4,  4, [System.Drawing.Color]::FromArgb(120, 240, 250, 255))
$jarBmp.SetPixel(12, 8, [System.Drawing.Color]::FromArgb(120, 240, 250, 255))
$jarBmp.SetPixel(12, 9, [System.Drawing.Color]::FromArgb(80,  240, 250, 255))

$jarBmp.Save("$blockTexDir\mixer_jar.png", [System.Drawing.Imaging.ImageFormat]::Png)
$jarBmp.Dispose()
Write-Host "  Created block/mixer_jar.png"

# ===========================================================================
# MIXER LIQUID TEXTURE  (16×16)  — green shake liquid (inside the jar)
# ===========================================================================
$liqBmp = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$liqG   = [System.Drawing.Graphics]::FromImage($liqBmp)
$liqG.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor

# Base green (like the shake item color)
$liqG.Clear([System.Drawing.Color]::FromArgb(255, 70, 170, 55))
# Lighter top (surface highlight)
$liqG.FillRectangle((SB 110 210 90),  0,  0, 16, 2)
# Darker bottom (depth shadow)
$liqG.FillRectangle((SB 45 130 35),   0, 13, 16, 3)
# Subtle vertical variation (bubbles / texture)
$liqG.FillRectangle((SB 80 185 65),   4,  3,  2, 8)
$liqG.FillRectangle((SB 80 185 65),  10,  5,  2, 6)

$liqG.Dispose()
$liqBmp.Save("$blockTexDir\mixer_liquid.png", [System.Drawing.Imaging.ImageFormat]::Png)
$liqBmp.Dispose()
Write-Host "  Created block/mixer_liquid.png"

Write-Host "All textures done!"
