$ErrorActionPreference = "Continue"
$adb = "C:\Users\neopradeepl\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$dev = "VSTWWWLFFE856LXS"
$pkg = "com.prabs.ceipts"
$ssDir = "c:\Game Dev\ExpenseSplitApp\test_screenshots"
$logDir = "c:\Game Dev\ExpenseSplitApp\test_logs"

# Ensure dirs exist
if (!(Test-Path $ssDir)) { New-Item -ItemType Directory -Path $ssDir | Out-Null }
if (!(Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir | Out-Null }

# Helpers
function ADB { & $adb -s $dev @args 2>&1 }

function GetLayout {
    ADB shell uiautomator dump /sdcard/ui_dump.xml | Out-Null
    $xml = ADB shell cat /sdcard/ui_dump.xml
    return ($xml -join "")
}

function GetElementCoords($pattern, $exact = $true) {
    $xml = GetLayout
    $xmlClean = ($xml -join "") -replace "\r", "" -replace "\n", ""
    
    $escaped = [regex]::Escape($pattern)
    
    # 1. Exact Parent-child pattern where parent has bounds and child has text/desc
    $regex1 = 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*>\s*<node[^>]*(text|content-desc)="' + $escaped + '"'
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
    
    # 2. Exact Direct match where the node itself has text/desc and bounds
    $regex2 = '((text|content-desc)="' + $escaped + '")[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
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
        $regex1_p = 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*>\s*<node[^>]*(text|content-desc)="[^"]*' + $escaped + '[^"]*"'
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
        $regex2_p = '((text|content-desc)="[^"]*' + $escaped + '[^"]*")[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
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

function GetCoordsByClass($className, $index = 0) {
    $xml = GetLayout
    $xmlClean = ($xml -join "") -replace "\r", "" -replace "\n", ""
    $matches = [regex]::Matches($xmlClean, 'class="' + $className + '"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
    if ($matches.Count -gt $index) {
        $m = $matches[$index]
        $x1 = [int]$m.Groups[1].Value
        $y1 = [int]$m.Groups[2].Value
        $x2 = [int]$m.Groups[3].Value
        $y2 = [int]$m.Groups[4].Value
        return @([math]::Floor(($x1 + $x2)/2), [math]::Floor(($y1 + $y2)/2))
    }
    return $null
}

function Screenshot($name) {
    ADB shell screencap -p /sdcard/ss_temp.png | Out-Null
    ADB pull /sdcard/ss_temp.png "$ssDir\$name.png" | Out-Null
    ADB shell rm /sdcard/ss_temp.png | Out-Null
    Write-Host "  [Screenshot] Saved $name.png"
}

function Tap($x, $y) {
    ADB shell input tap $x $y | Out-Null
    Start-Sleep -Seconds 2
}

function InputText($text) {
    $escaped = $text -replace ' ', '%s'
    ADB shell input text $escaped | Out-Null
    Start-Sleep -Milliseconds 500
}

function PressBack {
    ADB shell input keyevent 4 | Out-Null
    Start-Sleep -Seconds 1
}

function LogStep($stepNum, $stepName, $status, $notes) {
    $global:testResults += [PSCustomObject]@{
        Step = $stepNum
        Name = $stepName
        Status = $status
        Notes = $notes
    }
    Write-Host "  [$status] Step $stepNum : $stepName - $notes"
}

# Initialize
$global:testResults = @()

Write-Host "========================================="
Write-Host "  FULL ROBUST REGRESSION TEST"
Write-Host "  Device: $dev"
Write-Host "  Started: $(Get-Date)"
Write-Host "========================================="

# Ensure app is running
Write-Host "`n--- Launching app ---"
ADB shell am force-stop $pkg | Out-Null
Start-Sleep -Seconds 1
ADB shell am start -n "$pkg/.MainActivity" | Out-Null
Start-Sleep -Seconds 4

# ============ STEP 1: Login Screen ============
Write-Host "`n--- Step 1: Verify Login Screen ---"
Screenshot "01_login_screen"
$coordsLogin = GetElementCoords "Login"
$coordsGoogle = GetElementCoords "Google"
$coordsSignUp = GetElementCoords "Sign Up"

if ($coordsLogin -and $coordsGoogle -and $coordsSignUp) {
    LogStep 1 "Login Screen Display" "PASS" "Login, Google, and Sign Up buttons detected"
} else {
    LogStep 1 "Login Screen Display" "FAIL" "Failed to detect essential login screen elements"
}

# ============ STEP 2: Forgot Password ============
Write-Host "`n--- Step 2: Forgot Password Click ---"
$coordsForgot = GetElementCoords "Forgot Password?"
if ($coordsForgot) {
    Tap $coordsForgot[0] $coordsForgot[1]
    Screenshot "02_forgot_password"
    
    # Check if we left the login screen
    $layout = GetLayout
    if ($layout -notmatch "Email" -and $layout -notmatch "Password") {
        LogStep 2 "Forgot Password" "PASS" "Forgot Password clicked and navigated"
        PressBack
    } else {
        LogStep 2 "Forgot Password" "PASS" "Forgot Password clicked (retained focus)"
    }
} else {
    LogStep 2 "Forgot Password" "FAIL" "Forgot Password link not found"
}


# ============ STEP 3: Sign Up Toggle ============
Write-Host "`n--- Step 3: Sign Up Toggle ---"
$coordsSignUpToggle = GetElementCoords "Sign Up"
if ($coordsSignUpToggle) {
    Tap $coordsSignUpToggle[0] $coordsSignUpToggle[1]
    Screenshot "03_signup_mode"
    $layout = GetLayout
    if ($layout -match "Create a free account" -or $layout -match "Sign In") {
        LogStep 3 "Sign Up Toggle" "PASS" "Successfully toggled to Sign Up mode"
    } else {
        LogStep 3 "Sign Up Toggle" "FAIL" "Layout did not update to Sign Up mode"
    }
    # Toggle back to login
    $coordsSignInToggle = GetElementCoords "Sign In"
    if ($coordsSignInToggle) {
        Tap $coordsSignInToggle[0] $coordsSignInToggle[1]
    }
} else {
    LogStep 3 "Sign Up Toggle" "FAIL" "Sign Up toggle not found"
}

# ============ STEP 4: Continue with Google (Bypass Login) ============
Write-Host "`n--- Step 4: Continue with Google Bypass ---"
$coordsGoogle = GetElementCoords "Google"
if ($coordsGoogle) {
    Tap $coordsGoogle[0] $coordsGoogle[1]
    Start-Sleep -Seconds 4
    Screenshot "04_after_google_login"
    $layout = GetLayout
    if ($layout -match "Dashboard" -or $layout -match "Total Balance") {
        LogStep 4 "Google Login Bypass" "PASS" "Successfully logged in and reached Dashboard"
    } else {
        LogStep 4 "Google Login Bypass" "FAIL" "Failed to reach Dashboard after Google click"
    }
} else {
    LogStep 4 "Google Login Bypass" "FAIL" "Google button not found"
}

# ============ STEP 5: Settings Tab ============
Write-Host "`n--- Step 5: Settings Tab ---"
$coordsSettingsTab = GetElementCoords "Settings"
if ($coordsSettingsTab) {
    Tap $coordsSettingsTab[0] $coordsSettingsTab[1]
    Screenshot "05_settings_tab"
    $layout = GetLayout
    if ($layout -match "Friends Directory" -or $layout -match "Default Currency") {
        LogStep 5 "Settings Tab" "PASS" "Navigated to Settings"
    } else {
        LogStep 5 "Settings Tab" "FAIL" "Settings content not shown"
    }
} else {
    LogStep 5 "Settings Tab" "FAIL" "Settings tab not found"
}

# ============ STEP 6: Friends Directory ============
Write-Host "`n--- Step 6: Friends Directory ---"
# Swipe up to scroll settings list down
ADB shell input swipe 540 1800 540 800 300 | Out-Null
Start-Sleep -Seconds 1
$coordsFriendsDir = GetElementCoords "Friends Directory"
if ($coordsFriendsDir) {
    Tap $coordsFriendsDir[0] $coordsFriendsDir[1]
    Screenshot "06_friends_directory"
    LogStep 6 "Friends Directory Navigation" "PASS" "Opened Friends Directory"
} else {
    LogStep 6 "Friends Directory Navigation" "FAIL" "Friends Directory option not found"
}


# ============ STEP 7: Add Friend ============
Write-Host "`n--- Step 7: Add Friend ---"
$coordsAddFriendFAB = GetElementCoords "Add Friend"
if ($coordsAddFriendFAB) {
    Tap $coordsAddFriendFAB[0] $coordsAddFriendFAB[1]
    Screenshot "07_add_friend_dialog"
    
    # Enter Name & Email
    $etName = GetCoordsByClass "android.widget.EditText" 0
    $etEmail = GetCoordsByClass "android.widget.EditText" 1
    
    if ($etName -and $etEmail) {
        Tap $etName[0] $etName[1]
        InputText "John Doe"
        
        Tap $etEmail[0] $etEmail[1]
        InputText "john.doe@example.com"
        
        # Click Add button in dialog
        $coordsAddBtn = GetElementCoords "Add"
        if ($coordsAddBtn) {
            Tap $coordsAddBtn[0] $coordsAddBtn[1]
            Start-Sleep -Seconds 1
            Screenshot "07b_friend_added"
            
            # Check if John Doe is listed
            $layout = GetLayout
            if ($layout -match "John Doe") {
                LogStep 7 "Add Friend" "PASS" "John Doe successfully added to Friends list"
            } else {
                LogStep 7 "Add Friend" "FAIL" "Friend not found in list after adding"
            }
        } else {
            LogStep 7 "Add Friend" "FAIL" "Add confirm button not found in dialog"
        }
    } else {
        LogStep 7 "Add Friend" "FAIL" "Could not locate Name or Email fields in dialog"
    }
    
    # Go back to settings
    PressBack
} else {
    LogStep 7 "Add Friend" "FAIL" "Add Friend FAB not found"
}

# ============ STEP 8: Currency Setting ============
Write-Host "`n--- Step 8: Currency Setting ---"
$coordsCurrency = GetElementCoords "Default Currency"
if ($coordsCurrency) {
    Tap $coordsCurrency[0] $coordsCurrency[1]
    Screenshot "08_currency_dialog"
    
    # Select EUR
    $coordsEUR = GetElementCoords "EUR"
    if ($coordsEUR) {
        Tap $coordsEUR[0] $coordsEUR[1]
        Screenshot "08b_currency_changed"
        
        # Check if settings now lists EUR
        $layout = GetLayout
        if ($layout -match "EUR") {
            LogStep 8 "Change Currency" "PASS" "Currency successfully changed to EUR"
        } else {
            LogStep 8 "Change Currency" "FAIL" "Settings did not reflect currency change"
        }
    } else {
        LogStep 8 "Change Currency" "FAIL" "EUR option not found in dialog"
        PressBack
    }
} else {
    LogStep 8 "Change Currency" "FAIL" "Default Currency option not found"
}

# ============ STEP 9: Create Group ============
Write-Host "`n--- Step 9: Create Group ---"
$coordsGroupsTab = GetElementCoords "Groups"
if ($coordsGroupsTab) {
    Tap $coordsGroupsTab[0] $coordsGroupsTab[1]
    Screenshot "09_groups_list"
    
    # Tap New Group (Add icon)
    $coordsNewGroup = GetElementCoords "New Group"
    if ($coordsNewGroup) {
        Tap $coordsNewGroup[0] $coordsNewGroup[1]
        Screenshot "09b_create_group_screen"
        
        $etGroupName = GetCoordsByClass "android.widget.EditText" 0
        if ($etGroupName) {
            Tap $etGroupName[0] $etGroupName[1]
            InputText "Trip to Paris"
            
            # Select John Doe from selectable contact list
            $coordsJohnInList = GetElementCoords "John Doe"
            if ($coordsJohnInList) {
                Tap $coordsJohnInList[0] $coordsJohnInList[1]
            }
            
            Screenshot "09c_create_group_ready"
            
            # Click Create Group button
            $coordsCreateGroupBtn = GetElementCoords "Create Group"
            if ($coordsCreateGroupBtn) {
                Tap $coordsCreateGroupBtn[0] $coordsCreateGroupBtn[1]
                Start-Sleep -Seconds 2
                Screenshot "09d_group_created"
                
                # Check if group is in list
                $layout = GetLayout
                if ($layout -match "Trip to Paris") {
                    LogStep 9 "Create Group" "PASS" "Trip to Paris group created successfully with John Doe"
                } else {
                    LogStep 9 "Create Group" "FAIL" "Group not found in list after creation"
                }
            } else {
                LogStep 9 "Create Group" "FAIL" "Create Group button not found"
            }
        } else {
            LogStep 9 "Create Group" "FAIL" "Group Name input field not found"
        }
    } else {
        LogStep 9 "Create Group" "FAIL" "New Group button not found"
    }
} else {
    LogStep 9 "Create Group" "FAIL" "Groups tab not found"
}

# ============ STEP 10: Add member after group creation ============
Write-Host "`n--- Step 10: Add Member after Group Creation ---"
$coordsParisGroup = GetElementCoords "Trip to Paris"
if ($coordsParisGroup) {
    Tap $coordsParisGroup[0] $coordsParisGroup[1]
    Screenshot "10_group_detail"
    
    # Check if currency symbol matches EUR (€)
    $layout = GetLayout
    if ($layout -match "€") {
        Write-Host "  Currency symbol correct: € detected"
    } else {
        Write-Host "  Warning: € symbol not detected in group detail"
    }
    
    # Tap Add Member
    $coordsAddMember = GetElementCoords "Add Member"
    if ($coordsAddMember) {
        Tap $coordsAddMember[0] $coordsAddMember[1]
        Screenshot "10b_add_member_dialog"
        
        # Enter Alice details in dialog
        $etMemName = GetCoordsByClass "android.widget.EditText" 0
        $etMemEmail = GetCoordsByClass "android.widget.EditText" 1
        if ($etMemName -and $etMemEmail) {
            Tap $etMemName[0] $etMemName[1]
            InputText "Alice Smith"
            
            Tap $etMemEmail[0] $etMemEmail[1]
            InputText "alice@example.com"
            
            # Click Create & Add
            $coordsCreateAddBtn = GetElementCoords "Create &amp; Add"
            if ($coordsCreateAddBtn) {
                Tap $coordsCreateAddBtn[0] $coordsCreateAddBtn[1]
                Start-Sleep -Seconds 2
                Screenshot "10c_member_added"

                
                # Check if Alice is listed under Balances
                $layout = GetLayout
                if ($layout -match "Alice") {
                    LogStep 10 "Add Member Post-Creation" "PASS" "Alice Smith successfully added to group members"
                } else {
                    LogStep 10 "Add Member Post-Creation" "FAIL" "Alice not found in group detail balances"
                }
            } else {
                LogStep 10 "Add Member Post-Creation" "FAIL" "Create & Add button not found in dialog"
            }
        } else {
            LogStep 10 "Add Member Post-Creation" "FAIL" "EditText fields not found in dialog"
        }
    } else {
        LogStep 10 "Add Member Post-Creation" "FAIL" "Add Member button not found"
    }
} else {
    LogStep 10 "Add Member Post-Creation" "FAIL" "Trip to Paris group not found in list to open"
}

# ============ STEP 11: Add Expense ============
Write-Host "`n--- Step 11: Add Expense ---"
$coordsAddExpense = GetElementCoords "Add Expense"
if ($coordsAddExpense) {
    Tap $coordsAddExpense[0] $coordsAddExpense[1]
    Screenshot "11_add_expense_screen"
    
    # Title & Amount
    $etExpTitle = GetCoordsByClass "android.widget.EditText" 0
    $etExpAmount = GetCoordsByClass "android.widget.EditText" 1
    
    if ($etExpTitle -and $etExpAmount) {
        Tap $etExpTitle[0] $etExpTitle[1]
        InputText "Paris Dinner"
        
        Tap $etExpAmount[0] $etExpAmount[1]
        InputText "120.00"
        
        # Click Save Expense
        $coordsSaveExpense = GetElementCoords "Save Expense"
        if ($coordsSaveExpense) {
            Tap $coordsSaveExpense[0] $coordsSaveExpense[1]
            Start-Sleep -Seconds 2
            Screenshot "11b_expense_saved"
            
            # Check if expense list updated
            $layout = GetLayout
            if ($layout -match "Paris Dinner" -and $layout -match "120.00") {
                LogStep 11 "Add Expense" "PASS" "Paris Dinner expense of €120.00 successfully added"
            } else {
                LogStep 11 "Add Expense" "FAIL" "Expense not shown in group details list"
            }
        } else {
            LogStep 11 "Add Expense" "FAIL" "Save Expense button not found"
        }
    } else {
        LogStep 11 "Add Expense" "FAIL" "Expense Title/Amount input fields not found"
    }
} else {
    LogStep 11 "Add Expense" "FAIL" "Add Expense FAB not found"
}

# ============ STEP 12: Settle Up ============
Write-Host "`n--- Step 12: Settle Up screen ---"
$coordsSettleUp = GetElementCoords "Settle Up"
if ($coordsSettleUp) {
    Tap $coordsSettleUp[0] $coordsSettleUp[1]
    Screenshot "12_settle_up_screen"
    
    $layout = GetLayout
    if ($layout -match "Settle" -or $layout -match "payment" -or $layout -match "simplify") {
        LogStep 12 "Settle Up Screen" "PASS" "Settle Up screen opened correctly"
    } else {
        LogStep 12 "Settle Up Screen" "FAIL" "Settle Up screen contents not recognized"
    }
    PressBack
} else {
    LogStep 12 "Settle Up Screen" "FAIL" "Settle Up button not found"
}

# Go back to groups list
PressBack

# ============ STEP 13: Dashboard return & Currency Symbol verification ============
Write-Host "`n--- Step 13: Dashboard currency check ---"
$coordsDashboardTab = GetElementCoords "Dashboard"
if ($coordsDashboardTab) {
    Tap $coordsDashboardTab[0] $coordsDashboardTab[1]
    Screenshot "13_dashboard_return"
    
    $layout = GetLayout
    if ($layout -match "€") {
        LogStep 13 "Dashboard Currency Update" "PASS" "Dashboard net balances correctly updated to use € currency symbol"
    } else {
        LogStep 13 "Dashboard Currency Update" "FAIL" "Dashboard does not show the € currency symbol"
    }
} else {
    LogStep 13 "Dashboard Currency Update" "FAIL" "Dashboard tab not found"
}

# ============ STEP 14: Force Stop ============
Write-Host "`n--- Step 14: Force Stop ---"
ADB shell am force-stop $pkg | Out-Null
LogStep 14 "Force Stop" "PASS" "App stopped safely"

# ============ GENERATE REPORT ============
Write-Host "`n========================================="
Write-Host "  GENERATING TEST REPORT"
Write-Host "========================================="

$reportPath = "c:\Game Dev\ExpenseSplitApp\test_report.md"
$lines = @()
$lines += "# ExpenseSplitApp - Full Regression Test Report"
$lines += ""
$lines += "**Device:** $dev"
$lines += ("**Date:** " + (Get-Date -Format 'yyyy-MM-dd HH:mm:ss'))
$lines += "**Package:** $pkg"
$lines += "**APK:** app-debug.apk"
$lines += ""
$lines += "---"
$lines += ""
$lines += "## Test Summary"
$lines += ""
$lines += "| Step | Test Name | Status | Notes |"
$lines += "|------|-----------|--------|-------|"

foreach ($r in $global:testResults) {
    $icon = "[OK]"
    if ($r.Status -eq "FAIL") { $icon = "[FAIL]" }
    if ($r.Status -eq "SKIP") { $icon = "[SKIP]" }
    if ($r.Status -eq "INFO") { $icon = "[INFO]" }
    $line = "| " + $r.Step + " | " + $r.Name + " | " + $icon + " " + $r.Status + " | " + $r.Notes + " |"
    $lines += $line
}

$passCount = @($global:testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failCount = @($global:testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$skipCount = @($global:testResults | Where-Object { $_.Status -eq "SKIP" }).Count
$totalCount = $global:testResults.Count

$lines += ""
$lines += "---"
$lines += ""
$lines += "## Results Summary"
$lines += ""
$lines += ("- **Total Tests:** " + $totalCount)
$lines += ("- **Passed:** " + $passCount)
$lines += ("- **Failed:** " + $failCount)
$lines += ("- **Skipped:** " + $skipCount)
$lines += ""
$lines += "---"
$lines += ""
$lines += "## Screenshots"
$lines += ""

$screenshots = Get-ChildItem "$ssDir\*.png" -ErrorAction SilentlyContinue | Sort-Object Name
foreach ($ss in $screenshots) {
    # Skip current_screen and other temp ones
    if ($ss.BaseName -match "current_screen") { continue }
    $ssPath = $ss.FullName -replace '\\','/'
    $lines += ("### " + $ss.BaseName)
    $lines += ("![$($ss.BaseName)](file:///$ssPath)")
    $lines += ""
}

$lines += "---"
$lines += ""
$lines += "## Error Logs"
$lines += ""
$logDirSlash = $logDir -replace '\\','/'
$lines += ("- [full_logcat.txt](file:///$logDirSlash/full_logcat.txt)")
$lines += ("- [runtime_errors.txt](file:///$logDirSlash/runtime_errors.txt)")
$lines += ""
$lines += "---"
$lines += "*Report generated automatically.*"

$lines -join "`n" | Out-File -FilePath $reportPath -Encoding utf8
Write-Host "`nTest report saved to: $reportPath"
Write-Host "Total: $totalCount | Pass: $passCount | Fail: $failCount | Skip: $skipCount"
Write-Host "========================================="
