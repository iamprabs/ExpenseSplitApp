import os
import shutil
from PIL import Image

# 1. Update Icons
image_path = r"C:\Users\neopradeepl\.gemini\antigravity-ide\brain\caae814e-55cb-4756-927d-2263ed06af5d\media__1781675534099.png"
res_dir = r"c:\Game Dev\ExpenseSplitApp\app\src\main\res"

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

try:
    img = Image.open(image_path)
    for dpi, size in sizes.items():
        resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
        dpi_dir = os.path.join(res_dir, f"mipmap-{dpi}")
        if not os.path.exists(dpi_dir):
            os.makedirs(dpi_dir)
            
        # Overwrite legacy/round icons
        resized_img.save(os.path.join(dpi_dir, "ic_launcher.webp"), "WEBP")
        resized_img.save(os.path.join(dpi_dir, "ic_launcher_round.webp"), "WEBP")
        # Also save as png just in case
        resized_img.save(os.path.join(dpi_dir, "ic_launcher.png"), "PNG")
        resized_img.save(os.path.join(dpi_dir, "ic_launcher_round.png"), "PNG")
    print("Icons generated successfully.")
except Exception as e:
    print("Error processing image:", e)

# 2. Rename directories: com\example\ceipts -> com\prabs\ceipts
# We need to copy the contents, then delete old dir to avoid lock issues on 'example'
old_dirs = [
    r"c:\Game Dev\ExpenseSplitApp\app\src\main\java\com\example\ceipts",
    r"c:\Game Dev\ExpenseSplitApp\app\src\test\java\com\example\ceipts",
    r"c:\Game Dev\ExpenseSplitApp\app\src\androidTest\java\com\example\ceipts"
]

for old_dir in old_dirs:
    if os.path.exists(old_dir):
        # The new target is com\prabs\ceipts
        new_dir = old_dir.replace("\\example", "\\prabs")
        if not os.path.exists(new_dir):
            os.makedirs(new_dir)
        # Move all contents from com\example\ceipts to com\prabs\ceipts
        for item in os.listdir(old_dir):
            s = os.path.join(old_dir, item)
            d = os.path.join(new_dir, item)
            shutil.move(s, d)
        print(f"Moved contents from {old_dir} to {new_dir}")
        # Try to remove com\example\ceipts
        try:
            os.rmdir(old_dir)
        except OSError as e:
            print("Could not remove old directory:", e)
