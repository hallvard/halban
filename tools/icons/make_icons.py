"""Makes the icons of the application, from the images of the grid cells of the game, so the icon is the game as it looks:

  - tools/icons/app-icon.svg, made from the small level below, with the images (embedded, so the file is self-contained)
  - halban-parent/sokoban-app/src/main/resources/no/hal/sokoban/app/app-icon.png, the icon of the window
  - halban-parent/sokoban-app/src/android/res/mipmap-*/ic_launcher.png, the launcher icons of the Android app (the Gluon plugin copies
    the res folder of the source path over its default icons, and the manifest refers to @mipmap/ic_launcher)

Needs rsvg-convert (librsvg). The results are in the repository, so this is only run when the icon or the images are changed:
  python3 tools/icons/make_icons.py
"""
import base64
import subprocess
from pathlib import Path

HERE = Path(__file__).parent
ROOT = HERE.parent.parent
APP = ROOT / "halban-parent/sokoban-app/src"
IMAGES = ROOT / "halban-parent/sokoban-game/src/main/resources/no/hal/sokoban/fx/images/default"

# A simple board: the player is behind a box, about to push it onto the target. The box on a target above it is a joke, since it
# cannot have been pushed there. The characters are those of the level files.
LEVEL = [
    "#####",
    "# * #",
    "#@$.#",
    "#   #",
    "#####",
]
IMAGE_OF = {"#": "wall", " ": "empty", ".": "target", "@": "player", "$": "box", "*": "box_on_target"}
SIZE = 512
# the board fills the icon, so the wall around it is one cell thick, as in the game (the corners are cut by the rounded shape)
TILE = SIZE / len(LEVEL)


def make_svg(target):
    uris = {name: "data:image/png;base64," + base64.b64encode((IMAGES / f"{name}16x16.png").read_bytes()).decode() for name in IMAGE_OF.values()}
    out = [f'<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" width="{SIZE}" height="{SIZE}" viewBox="0 0 {SIZE} {SIZE}">',
           f'<defs><clipPath id="shape"><rect width="{SIZE}" height="{SIZE}" rx="96"/></clipPath></defs>',
           '<g clip-path="url(#shape)" image-rendering="pixelated">']
    for row, line in enumerate(LEVEL):
        for col, char in enumerate(line):
            # the edges are whole numbers, and each cell goes a little under the next, or there are thin lines between the cells
            x, y = round(col * TILE), round(row * TILE)
            w, h = round((col + 1) * TILE) - x + 2, round((row + 1) * TILE) - y + 2
            out.append(f'<image x="{x}" y="{y}" width="{w}" height="{h}" xlink:href="{uris[IMAGE_OF[char]]}"/>')
    out.append('</g></svg>')
    target.write_text("\n".join(out))
    print("wrote", target.relative_to(ROOT))


def png(size, target):
    target.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["rsvg-convert", "-w", str(size), "-h", str(size), str(HERE / "app-icon.svg"), "-o", str(target)], check=True)
    print("wrote", target.relative_to(ROOT))


if __name__ == "__main__":
    make_svg(HERE / "app-icon.svg")
    png(256, APP / "main/resources/no/hal/sokoban/app/app-icon.png")
    for density, size in {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}.items():
        png(size, APP / f"android/res/mipmap-{density}/ic_launcher.png")
