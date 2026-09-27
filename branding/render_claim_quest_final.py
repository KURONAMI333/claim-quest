#!/usr/bin/env python3
"""Render the approved Claim Quest icon at distribution sizes.

Owner-selected design (2026-09-27, candidate B of the icon review):
speech bubble filled with a vertical yellow->orange gradient measured from
the official FTB Chunks CurseForge avatar (top ~(255,205,70) -> bottom
~(250,140,60)); 3x3 claim grid (5 filled cells) centred on the bubble-body
centre; outer ground stays the sibling-family cream (mod-074/028 grammar).
Fully self-contained — no third-party assets.

Outputs:
  branding/claim_quest_icon_{512,256,128,64}.png  publish masters
  src/main/resources/logo.png                     in-jar logo (256)

SS=4 (2048 -> 512 LANCZOS).
"""
import shutil
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
BRAND = ROOT / "branding"
SS = 4
SIZE = 512
S = SIZE * SS

CREAM_BG = (240, 235, 224, 255)      # sibling ground (074/028 family)
GLYPH = (246, 241, 231, 255)         # sibling off-white
FTB_TOP = (255, 205, 70)             # measured FTB Chunks avatar gradient
FTB_BOT = (250, 140, 60)

BUB = (0.09, 0.10, 0.91, 0.78)
BUB_CX = (BUB[0] + BUB[2]) / 2       # 0.50
BUB_CY = (BUB[1] + BUB[3]) / 2       # 0.44


def vgrad(size_box, top, bot):
    """Vertical linear gradient RGBA image for a bbox (x0,y0,x1,y1)."""
    x0, y0, x1, y1 = size_box
    w, h = x1 - x0, y1 - y0
    g = Image.new("RGBA", (1, h))
    for y in range(h):
        t = y / max(h - 1, 1)
        c = tuple(int(top[i] + (bot[i] - top[i]) * t) for i in range(3)) + (255,)
        g.putpixel((0, y), c)
    return g.resize((w, h), Image.BILINEAR)


def bubble_mask():
    m = Image.new("L", (S, S), 0)
    d = ImageDraw.Draw(m)
    x0, y0, x1, y1 = [int(v * S) for v in BUB]
    r = int((x1 - x0) * 0.16)
    d.rounded_rectangle([x0, y0, x1, y1], radius=r, fill=255)
    tx0, ty = int(S * 0.42), y1 - 2
    d.polygon([(tx0, ty), (int(S * 0.60), ty), (int(S * 0.40), int(S * 0.94))],
              fill=255)
    return m


def glyph_grid(d: ImageDraw.ImageDraw, color):
    """3x3 claim grid, bbox centred on the bubble-body centre (0.50, 0.44)."""
    n, pad = 3, int(S * 0.055)
    gw = int(S * 0.46)
    gx = int(S * BUB_CX - gw / 2)
    gy = int(S * BUB_CY - gw / 2)
    cell = (gw - pad * (n - 1)) // n
    claimed = {(0, 0), (0, 1), (1, 1), (1, 0), (2, 1)}
    for r in range(n):
        for c in range(n):
            x = gx + c * (cell + pad)
            y = gy + r * (cell + pad)
            if (r, c) in claimed:
                d.rounded_rectangle([x, y, x + cell, y + cell],
                                    radius=int(cell * 0.18), fill=color)
            else:
                d.rounded_rectangle([x, y, x + cell, y + cell],
                                    radius=int(cell * 0.18), outline=color,
                                    width=int(S * 0.012))


def final_icon() -> Image.Image:
    """Bubble fill = FTB gradient, outer = sibling cream, centred grid."""
    img = Image.new("RGBA", (S, S), CREAM_BG)
    grad = vgrad((0, 0, S, S), FTB_TOP, FTB_BOT)
    img.paste(grad, (0, 0), bubble_mask())
    glyph_grid(ImageDraw.Draw(img), GLYPH)
    return img.resize((SIZE, SIZE), Image.LANCZOS)


if __name__ == "__main__":
    final = final_icon()
    for size in (512, 256, 128, 64):
        out = BRAND / f"claim_quest_icon_{size}.png"
        final.resize((size, size), Image.LANCZOS).save(out)
        print("wrote", out)
    logo = ROOT / "src" / "main" / "resources" / "logo.png"
    shutil.copyfile(BRAND / "claim_quest_icon_256.png", logo)
    print("wrote", logo)
