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

$script:tmpl.Dispose()
Write-Host "All 9 item textures done!"

# ===========================================================================
# MIXER BLOCK TEXTURE  (64×64 atlas, texture_size [64,64])
# UV unit = 4px.  Key face regions:
#   x:0-8,  y:0-11   = right body east face    (side view from right)
#   x:0-8,  y:11-22  = front body north face   ← MOST VISIBLE FRONT FACE
#   x:16-24,y:11-22  = left body west face     (side view from left)
#   x:8-16, y:0-11   = right body west (inner)
#   x:16-24,y:0-11   = left body east  (inner)
#   x:24-30,y:0-11   = front east/west thin strips
#   Everything else  = legs, corners, top/bottom faces (use dark/mid tones)
# ===========================================================================
$blockTexDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\textures\block"
New-Item -ItemType Directory -Force -Path $blockTexDir | Out-Null

$mixBmp = New-Object System.Drawing.Bitmap(64, 64, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$mg = [System.Drawing.Graphics]::FromImage($mixBmp)
$mg.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor

function MC { param([int]$r,[int]$g,[int]$b)
    [System.Drawing.Color]::FromArgb(255,$r,$g,$b) }
function MSB { param($c) [System.Drawing.SolidBrush]::new($c) }

$mDarkEdge  = MC 44  50  56     # #2C3238 shadow/border pixels
$mDark      = MC 64  72  80     # #404850 dark body panels
$mMid       = MC 96 104 112     # #606870 mid body
$mLight     = MC 136 146 154    # #88929A light face panels
$mSheen     = MC 176 184 192    # #B0B8C0 highlight sheen
$mGreen1    = MC 50  90  40     # #325A28 dark green stripe
$mGreen2    = MC 78 134  62     # #4E863E mid green
$mGreen3    = MC 110 172  88    # #6EAC58 bright highlight on stripe

# ── Fill base ───────────────────────────────────────────────────────────────
$mg.Clear($mMid)
# Darkest fill for the "far" areas (x>30) — small legs/corner elements
$mg.FillRectangle((MSB $mDark), 30, 0, 34, 64)

# ── Right body east face  (x:0-8, y:0-11) — player sees from right ──────────
$mg.FillRectangle((MSB $mLight), 0, 0, 8, 11)
$mg.FillRectangle((MSB $mDarkEdge), 0, 0, 1, 11)    # left shadow edge
$mg.FillRectangle((MSB $mDarkEdge), 0, 0, 8, 1)     # top shadow
$mg.FillRectangle((MSB $mSheen), 7, 1, 1, 10)        # right sheen stripe

# ── Right body west face  (x:8-16, y:0-11) — inner, darker ─────────────────
$mg.FillRectangle((MSB $mDark), 8, 0, 8, 11)
$mg.FillRectangle((MSB $mDarkEdge), 8, 0, 1, 11)

# ── Left body east face   (x:16-24, y:0-11) — inner ────────────────────────
$mg.FillRectangle((MSB $mMid), 16, 0, 8, 11)

# ── Front body east/west thin strips  (x:24-30, y:0-11) ─────────────────────
$mg.FillRectangle((MSB $mLight), 24, 0, 6, 11)

# ── FRONT north face  (x:0-8, y:11-22)  ← THE MAIN VISIBLE FACE ─────────────
$mg.FillRectangle((MSB $mLight), 0, 11, 8, 11)
# Borders
$mg.FillRectangle((MSB $mDarkEdge), 0, 11, 1, 11)   # left border
$mg.FillRectangle((MSB $mDarkEdge), 7, 11, 1, 11)   # right border
$mg.FillRectangle((MSB $mDarkEdge), 0, 11, 8, 1)    # top border
$mg.FillRectangle((MSB $mDarkEdge), 0, 21, 8, 1)    # bottom border
# Green apple stripe (3px tall) in the vertical middle of the face
$mg.FillRectangle((MSB $mGreen1), 1, 14, 6, 5)       # dark green base
$mg.FillRectangle((MSB $mGreen2), 1, 15, 6, 3)       # mid stripe
$mg.FillRectangle((MSB $mGreen3), 2, 15, 4, 1)       # bright top row of stripe

# ── Front south face  (x:8-16, y:11-22) — back of front body ────────────────
$mg.FillRectangle((MSB $mMid), 8, 11, 8, 11)
$mg.FillRectangle((MSB $mDarkEdge), 8, 11, 1, 11)

# ── Left body west face  (x:16-24, y:11-22) — player sees from left ─────────
$mg.FillRectangle((MSB $mLight), 16, 11, 8, 11)
$mg.FillRectangle((MSB $mDarkEdge), 16, 11, 1, 11)
$mg.FillRectangle((MSB $mDarkEdge), 23, 11, 1, 11)
$mg.FillRectangle((MSB $mDarkEdge), 16, 11, 8, 1)
$mg.FillRectangle((MSB $mDarkEdge), 16, 21, 8, 1)
$mg.FillRectangle((MSB $mGreen1), 17, 14, 6, 5)
$mg.FillRectangle((MSB $mGreen2), 17, 15, 6, 3)
$mg.FillRectangle((MSB $mGreen3), 18, 15, 4, 1)

# ── Front body east/west thin strips  (x:24-30, y:11-22) ────────────────────
$mg.FillRectangle((MSB $mMid), 24, 11, 6, 11)

# ── Back body faces  (y:22-33) ───────────────────────────────────────────────
$mg.FillRectangle((MSB $mDark), 0, 22, 16, 11)
$mg.FillRectangle((MSB $mDarkEdge), 0, 22, 16, 1)   # top separator

# ── Legs and misc small elements  (y:33-64 and x:0-30) ──────────────────────
$mg.FillRectangle((MSB $mDark), 0, 33, 30, 31)

$mg.Dispose()
$mixBmp.Save("$blockTexDir\mixer_block_unten.png", [System.Drawing.Imaging.ImageFormat]::Png)
$mixBmp.Dispose()
Write-Host "  Created block/mixer_block_unten.png"
Write-Host "All textures done!"
