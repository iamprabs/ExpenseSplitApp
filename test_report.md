# ExpenseSplitApp - Full Regression Test Report

**Device:** VSTWWWLFFE856LXS
**Date:** 2026-06-21 22:37:29
**Package:** com.prabs.ceipts
**APK:** app-debug.apk

---

## Test Summary

| Step | Test Name | Status | Notes |
|------|-----------|--------|-------|
| 1 | Login Screen Display | [OK] PASS | Login, Google, and Sign Up buttons detected |
| 2 | Forgot Password | [OK] PASS | Forgot Password clicked (retained focus) |
| 3 | Sign Up Toggle | [OK] PASS | Successfully toggled to Sign Up mode |
| 4 | Google Login Bypass | [OK] PASS | Successfully logged in and reached Dashboard |
| 5 | Settings Tab | [OK] PASS | Navigated to Settings |
| 6 | Friends Directory Navigation | [FAIL] FAIL | Friends Directory option not found |
| 7 | Add Friend | [FAIL] FAIL | Add Friend FAB not found |
| 8 | Change Currency | [OK] PASS | Currency successfully changed to EUR |
| 9 | Create Group | [OK] PASS | Trip to Paris group created successfully with John Doe |
| 10 | Add Member Post-Creation | [FAIL] FAIL | Create & Add button not found in dialog |
| 11 | Add Expense | [FAIL] FAIL | Add Expense FAB not found |
| 12 | Settle Up Screen | [FAIL] FAIL | Settle Up button not found |
| 13 | Dashboard Currency Update | [FAIL] FAIL | Dashboard tab not found |
| 14 | Force Stop | [OK] PASS | App stopped safely |

---

## Results Summary

- **Total Tests:** 14
- **Passed:** 8
- **Failed:** 6
- **Skipped:** 0

---

## Screenshots

### 01_login_screen
![01_login_screen](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/01_login_screen.png)

### 02_forgot_password
![02_forgot_password](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/02_forgot_password.png)

### 02_login_empty_click
![02_login_empty_click](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/02_login_empty_click.png)

### 03_forgot_password
![03_forgot_password](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/03_forgot_password.png)

### 03_signup_mode
![03_signup_mode](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/03_signup_mode.png)

### 04_after_google_login
![04_after_google_login](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/04_after_google_login.png)

### 04_signup_mode
![04_signup_mode](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/04_signup_mode.png)

### 05_after_login
![05_after_login](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/05_after_login.png)

### 05_settings_tab
![05_settings_tab](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/05_settings_tab.png)

### 06_dashboard
![06_dashboard](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/06_dashboard.png)

### 07_groups_tab
![07_groups_tab](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/07_groups_tab.png)

### 08_create_group
![08_create_group](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/08_create_group.png)

### 08_currency_dialog
![08_currency_dialog](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/08_currency_dialog.png)

### 08b_currency_changed
![08b_currency_changed](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/08b_currency_changed.png)

### 09_groups_list
![09_groups_list](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/09_groups_list.png)

### 09b_create_group_screen
![09b_create_group_screen](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/09b_create_group_screen.png)

### 09c_create_group_ready
![09c_create_group_ready](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/09c_create_group_ready.png)

### 09d_group_created
![09d_group_created](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/09d_group_created.png)

### 10_group_detail
![10_group_detail](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/10_group_detail.png)

### 10b_add_member_dialog
![10b_add_member_dialog](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/10b_add_member_dialog.png)

### 11_add_expense_screen
![11_add_expense_screen](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/11_add_expense_screen.png)

### 11_settings_tab
![11_settings_tab](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/11_settings_tab.png)

### 12_friends_not_found
![12_friends_not_found](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/12_friends_not_found.png)

### 13_currency_not_found
![13_currency_not_found](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/13_currency_not_found.png)

### 13_dashboard_return
![13_dashboard_return](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/13_dashboard_return.png)

### 14_analytics_tab
![14_analytics_tab](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/14_analytics_tab.png)

### 15_dashboard_return
![15_dashboard_return](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/15_dashboard_return.png)

### 16_profile_tap
![16_profile_tap](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/16_profile_tap.png)

### clean_01_login
![clean_01_login](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/clean_01_login.png)

### clean_02_after_google_login
![clean_02_after_google_login](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/clean_02_after_google_login.png)

### clean_03_dashboard
![clean_03_dashboard](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/clean_03_dashboard.png)

### clean_04_groups
![clean_04_groups](file:///C:/Game Dev/ExpenseSplitApp/test_screenshots/clean_04_groups.png)

---

## Error Logs

- [full_logcat.txt](file:///c:/Game Dev/ExpenseSplitApp/test_logs/full_logcat.txt)
- [runtime_errors.txt](file:///c:/Game Dev/ExpenseSplitApp/test_logs/runtime_errors.txt)

---
*Report generated automatically.*
