$adb = "C:\Users\neopradeepl\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$dev = "VSTWWWLFFE856LXS"

function ADB { & $adb -s $dev @args 2>&1 }

function GetLayout {
    ADB shell uiautomator dump /sdcard/ui_dump.xml | Out-Null
    $xml = ADB shell cat /sdcard/ui_dump.xml
    return ($xml -join "")
}

function GetElementCoords($pattern, $exact = $true) {
    # Read the XML and remove carriage returns/newlines to make it a single line
    $xml = GetLayout
    $xmlClean = ($xml -join "") -replace "\r", "" -replace "\n", ""
    
    # 1. Parent-child pattern where parent has bounds and child has text/desc
    $regex1 = 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*>\s*<node[^>]*(text|content-desc)="' + $pattern + '"'
    if ($xmlClean -match $regex1) {
        $x1 = [int]$Matches[1]
        $y1 = [int]$Matches[2]
        $x2 = [int]$Matches[3]
        $y2 = [int]$Matches[4]
        if ($x1 -ne 0 -or $y1 -ne 0 -or $x2 -ne 0 -or $y2 -ne 0) {
            $x = [math]::Floor(($x1 + $x2) / 2)
            $y = [math]::Floor(($y1 + $y2) / 2)
            return @($x, $y)
        }
    }
    
    # 2. Direct match where the node itself has text/desc and bounds
    $regex2 = '((text|content-desc)="' + $pattern + '")[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
    if ($xmlClean -match $regex2) {
        $x1 = [int]$Matches[3]
        $y1 = [int]$Matches[4]
        $x2 = [int]$Matches[5]
        $y2 = [int]$Matches[6]
        if ($x1 -ne 0 -or $y1 -ne 0 -or $x2 -ne 0 -or $y2 -ne 0) {
            $x = [math]::Floor(($x1 + $x2) / 2)
            $y = [math]::Floor(($y1 + $y2) / 2)
            return @($x, $y)
        }
    }
    
    # Fallback to partial matching if exact was requested but not found
    if ($exact) {
        # Check parent-child pattern with partial match
        $regex1_p = 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*>\s*<node[^>]*(text|content-desc)="[^"]*' + $pattern + '[^"]*"'
        if ($xmlClean -match $regex1_p) {
            $x1 = [int]$Matches[1]
            $y1 = [int]$Matches[2]
            $x2 = [int]$Matches[3]
            $y2 = [int]$Matches[4]
            if ($x1 -ne 0 -or $y1 -ne 0 -or $x2 -ne 0 -or $y2 -ne 0) {
                $x = [math]::Floor(($x1 + $x2) / 2)
                $y = [math]::Floor(($y1 + $y2) / 2)
                return @($x, $y)
            }
        }
        
        # Check direct pattern with partial match
        $regex2_p = '((text|content-desc)="[^"]*' + $pattern + '[^"]*")[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
        if ($xmlClean -match $regex2_p) {
            $x1 = [int]$Matches[3]
            $y1 = [int]$Matches[4]
            $x2 = [int]$Matches[5]
            $y2 = [int]$Matches[6]
            if ($x1 -ne 0 -or $y1 -ne 0 -or $x2 -ne 0 -or $y2 -ne 0) {
                $x = [math]::Floor(($x1 + $x2) / 2)
                $y = [math]::Floor(($y1 + $y2) / 2)
                return @($x, $y)
            }
        }
    }
    
    return $null
}

Write-Host "Finding coordinates for 'Login'..."
$coords = GetElementCoords "Login"
if ($coords) {
    Write-Host "Found Login at $($coords[0]), $($coords[1])"
} else {
    Write-Host "Login not found"
}

Write-Host "Finding coordinates for 'Google'..."
$coords = GetElementCoords "Google"
if ($coords) {
    Write-Host "Found Google at $($coords[0]), $($coords[1])"
} else {
    Write-Host "Google not found"
}

Write-Host "Finding coordinates for 'Sign Up'..."
$coords = GetElementCoords "Sign Up"
if ($coords) {
    Write-Host "Found Sign Up at $($coords[0]), $($coords[1])"
} else {
    Write-Host "Sign Up not found"
}
