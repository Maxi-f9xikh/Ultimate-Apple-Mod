Add-Type -AssemblyName System.Drawing

$texDir = "E:\Programieren\Projekte\Ultimate Apple Mod\forge\src\main\resources\assets\ultimate_apple_mod\textures\item"
$tmpl = [System.Drawing.Bitmap]::new("$texDir\iron_apple.png")

$seen = [System.Collections.Generic.HashSet[string]]::new()
$rawC = [System.Collections.Generic.List[System.Drawing.Color]]::new()
for ($y = 0; $y -lt 16; $y++) {
    for ($x = 0; $x -lt 16; $x++) {
        $p = $tmpl.GetPixel($x, $y)
        if ($p.A -gt 0) {
            $k = ($p.R).ToString() + '_' + ($p.G).ToString() + '_' + ($p.B).ToString()
            if ($seen.Add($k)) { $rawC.Add($p) }
        }
    }
}
$tC = [array]($rawC | Sort-Object { [int]$_.R + [int]$_.G + [int]$_.B })
$nC = $tC.Count
$ci = @{}
for ($i = 0; $i -lt $nC; $i++) {
    $c = $tC[$i]
    $k = ($c.R).ToString() + '_' + ($c.G).ToString() + '_' + ($c.B).ToString()
    $ci[$k] = $i
}

function HI([string]$h) {
    $r = [Convert]::ToInt32($h.Substring(1,2), 16)
    $g = [Convert]::ToInt32($h.Substring(3,2), 16)
    $b = [Convert]::ToInt32($h.Substring(5,2), 16)
    [System.Drawing.Color]::FromArgb(255, $r, $g, $b).ToArgb()
}
function ToColor([int]$argb) { [System.Drawing.Color]::FromArgb($argb) }

function Make-Tex([string]$Name, [int[]]$Pal, [hashtable]$Ov) {
    $bmp = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $src = $tmpl.GetPixel($x, $y)
            if ($src.A -eq 0) { continue }
            $k = ($src.R).ToString() + '_' + ($src.G).ToString() + '_' + ($src.B).ToString()
            $idx = if ($ci.ContainsKey($k)) { [int]$ci[$k] } else { 0 }
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
    Write-Host ("  Created $Name.png")
}

# ===========================================================================
# TOTEM APPLE  -  warm gold body, green rectangular eyes + nose
#   matching vanilla Minecraft Totem of Undying style.
# ===========================================================================
$pTo = [int[]]@(
    (HI "#1A1000"),(HI "#362000"),(HI "#604000"),(HI "#8A6010"),
    (HI "#B08428"),(HI "#D0A030"),(HI "#E4BC40"),(HI "#F0D055"),(HI "#FAE88A"))
$oTo = @{
    # stem
    "9,1"=(HI "#5C3800"); "8,2"=(HI "#4A2C00"); "9,2"=(HI "#5C3800"); "8,3"=(HI "#4A2C00")

    # forehead accents (above eyes)
    "5,5"=(HI "#1A0A00"); "6,5"=(HI "#1A0A00"); "8,5"=(HI "#1A0A00"); "9,5"=(HI "#1A0A00")

    # LEFT EYE dark frame (x=2-6, y=6-9)
    "2,6"=(HI "#1A0A00"); "3,6"=(HI "#1A0A00"); "4,6"=(HI "#1A0A00"); "5,6"=(HI "#1A0A00"); "6,6"=(HI "#1A0A00")
    "2,7"=(HI "#1A0A00"); "6,7"=(HI "#1A0A00")
    "2,8"=(HI "#1A0A00"); "6,8"=(HI "#1A0A00")
    "2,9"=(HI "#1A0A00"); "3,9"=(HI "#1A0A00"); "4,9"=(HI "#1A0A00"); "5,9"=(HI "#1A0A00"); "6,9"=(HI "#1A0A00")
    # LEFT EYE green fill (x=3-5, y=7-8)
    "3,7"=(HI "#30C838"); "4,7"=(HI "#50E058"); "5,7"=(HI "#30C838")
    "3,8"=(HI "#30C838"); "4,8"=(HI "#50E058"); "5,8"=(HI "#30C838")

    # RIGHT EYE dark frame (x=8-12, y=6-9)
    "8,6"=(HI "#1A0A00"); "9,6"=(HI "#1A0A00"); "10,6"=(HI "#1A0A00"); "11,6"=(HI "#1A0A00"); "12,6"=(HI "#1A0A00")
    "8,7"=(HI "#1A0A00"); "12,7"=(HI "#1A0A00")
    "8,8"=(HI "#1A0A00"); "12,8"=(HI "#1A0A00")
    "8,9"=(HI "#1A0A00"); "9,9"=(HI "#1A0A00"); "10,9"=(HI "#1A0A00"); "11,9"=(HI "#1A0A00"); "12,9"=(HI "#1A0A00")
    # RIGHT EYE green fill (x=9-11, y=7-8)
    "9,7"=(HI "#30C838"); "10,7"=(HI "#50E058"); "11,7"=(HI "#30C838")
    "9,8"=(HI "#30C838"); "10,8"=(HI "#50E058"); "11,8"=(HI "#30C838")

    # NOSE marks (two dark pixels below center gap)
    "5,10"=(HI "#1A0A00"); "6,10"=(HI "#1A0A00")
    "8,10"=(HI "#1A0A00"); "9,10"=(HI "#1A0A00")

    # CHIN ornament
    "4,12"=(HI "#1A0A00"); "5,12"=(HI "#1A0A00"); "6,12"=(HI "#1A0A00")
    "8,12"=(HI "#1A0A00"); "9,12"=(HI "#1A0A00"); "10,12"=(HI "#1A0A00")
}
Make-Tex "totem_apple" $pTo $oTo

$tmpl.Dispose()
Write-Host "Done."
