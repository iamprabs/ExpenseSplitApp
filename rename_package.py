import os
import glob

old_pkg = "com.example.expensesplit"
new_pkg = "com.example.ceipts"

# 1. Replace string in all kt, xml, kts files
directory = r"c:\Game Dev\ExpenseSplitApp"
for root, dirs, files in os.walk(directory):
    for file in files:
        if file.endswith(".kt") or file.endswith(".xml") or file.endswith(".kts"):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            if old_pkg in content:
                new_content = content.replace(old_pkg, new_pkg)
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f"Updated {path}")

# 2. Rename directories
old_dirs = [
    r"c:\Game Dev\ExpenseSplitApp\app\src\main\java\com\example\expensesplit",
    r"c:\Game Dev\ExpenseSplitApp\app\src\test\java\com\example\expensesplit",
    r"c:\Game Dev\ExpenseSplitApp\app\src\androidTest\java\com\example\expensesplit"
]

for old_dir in old_dirs:
    if os.path.exists(old_dir):
        new_dir = old_dir.replace("expensesplit", "ceipts")
        os.rename(old_dir, new_dir)
        print(f"Renamed {old_dir} to {new_dir}")
