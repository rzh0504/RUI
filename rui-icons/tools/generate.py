#!/usr/bin/env python3
"""Regenerate the rui-icons sources from the Compose Icons Lucide pack.

Usage:
    python rui-icons/tools/generate.py
    python rui-icons/tools/generate.py --sources path/to/icons-lucide-1.1.0-sources.jar

The manifest (`icons.txt`) decides which Lucide files are vendored, their
semantic RuiCommonIcons names, and which ones mirror in RTL. Only generated
files under src/commonMain/kotlin/rui/icons are touched.

Each glyph gets a private file with an internal lazy vector; the public API is
one object with member getters, so consumers only import RuiCommonIcons.
"""

from __future__ import annotations

import argparse
import io
import re
import sys
import urllib.request
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src" / "commonMain" / "kotlin" / "rui" / "icons"
MANIFEST = Path(__file__).resolve().with_name("icons.txt")
SOURCES_URL = (
    "https://repo1.maven.org/maven2/com/composables/"
    "icons-lucide/1.1.0/icons-lucide-1.1.0-sources.jar"
)
SOURCE_JAR = "commonMain/com/composables/icons/lucide/{name}.kt"

OBJECT_KDOC = """/**
 * The common icon set: a curated Lucide subset, named for Rui's vocabulary
 * rather than the source glyph names.
 *
 * Every icon is a 24dp [androidx.compose.ui.graphics.vector.ImageVector]
 * with 2dp rounded strokes, ready for `RuiIcon` from the rui module.
 * Direction-dependent glyphs carry `autoMirror`, so they flip automatically
 * in RTL layouts. [All] exposes the catalogue used by the sample gallery.
 */
public object RuiCommonIcons {
{members}

    /** Every curated icon in manifest order — the catalogue's gallery source. */
    public val All: List<RuiCommonIcon>
        get() = allIcons
}
"""

ALL_FILE = """package rui.icons

import androidx.compose.ui.graphics.vector.ImageVector

/** One entry of [RuiCommonIcons.All]. */
public class RuiCommonIcon internal constructor(
    public val name: String,
    public val image: ImageVector,
)

internal val allIcons: List<RuiCommonIcon> = listOf(
{entries}
)
"""


def load_manifest() -> list[tuple[str, str, bool]]:
    entries: list[tuple[str, str, bool]] = []
    rui_names: set[str] = set()
    for number, raw in enumerate(MANIFEST.read_text(encoding="utf-8").splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        parts = [part.strip() for part in line.split("|")]
        if len(parts) != 3 or parts[2] not in {"0", "1"}:
            raise SystemExit(f"{MANIFEST}:{number}: expected Name|RuiName|0/1")
        lucide, rui, mirror = parts
        if rui in rui_names:
            raise SystemExit(f"{MANIFEST}:{number}: duplicate Rui name {rui}")
        rui_names.add(rui)
        entries.append((lucide, rui, mirror == "1"))
    if not entries:
        raise SystemExit("icons.txt is empty")
    return entries


def load_sources(path: str | None) -> bytes:
    if path:
        return Path(path).read_bytes()
    print(f"Downloading {SOURCES_URL}")
    with urllib.request.urlopen(SOURCES_URL) as response:
        return response.read()


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--sources", help="local icons-lucide sources jar")
    args = parser.parse_args()

    entries = load_manifest()
    jar = zipfile.ZipFile(io.BytesIO(load_sources(args.sources)))
    OUT.mkdir(parents=True, exist_ok=True)
    for stale in OUT.glob("*.kt"):
        stale.unlink()

    all_entries: list[str] = []
    object_members: list[str] = []
    for lucide, rui, mirror in entries:
        source_name = SOURCE_JAR.format(name=lucide)
        try:
            text = jar.read(source_name).decode("utf-8")
        except KeyError:
            raise SystemExit(f"Lucide source not found: {source_name}")

        text = text.replace(
            "package com.composables.icons.lucide",
            "package rui.icons",
            1,
        )
        # Some generated files carry unused foundation imports (e.g. the
        # "Image" icon imports androidx.compose.foundation.Image); these
        # vectors only need compose.ui.
        text = re.sub(
            r"^import androidx\.compose\.foundation\.[^\n]*\n",
            "",
            text,
            flags=re.MULTILINE,
        )
        internal_name = f"lucide{rui}"
        pattern = re.compile(
            rf"public val Lucide\.{re.escape(lucide)}\s*:\s*ImageVector"
        )
        if not pattern.search(text):
            raise SystemExit(f"property Lucide.{lucide} not found in {source_name}")
        text = pattern.sub(
            f"internal val {internal_name}: ImageVector",
            text,
            count=1,
        )

        if mirror:
            text, replacements = re.subn(
                r"(\n\s*viewportHeight = [^\n]+)(\n)",
                r"\1,\n            autoMirror = true,\2",
                text,
                count=1,
            )
            if replacements != 1:
                raise SystemExit(f"could not mark {lucide} autoMirror")

        header = (
            f"// Lucide icon \"{lucide.lower()}\" (ISC), vector definition from "
            "Compose Icons (MIT).\n"
            "// Regenerate with rui-icons/tools/generate.py; see NOTICE.\n\n"
        )
        (OUT / f"{rui}.kt").write_text(header + text, encoding="utf-8")
        object_members.append(
            f"    public val {rui}: ImageVector get() = {internal_name}"
        )
        all_entries.append(
            f'    RuiCommonIcon("{rui}", RuiCommonIcons.{rui}),'
        )

    (OUT / "RuiCommonIcons.kt").write_text(
        "package rui.icons\n\n"
        "import androidx.compose.ui.graphics.vector.ImageVector\n\n"
        + OBJECT_KDOC.replace("{members}", "\n".join(object_members)),
        encoding="utf-8",
    )
    (OUT / "All.kt").write_text(
        ALL_FILE.replace("{entries}", "\n".join(all_entries)),
        encoding="utf-8",
    )
    print(f"Generated {len(entries)} icons in {OUT}")


if __name__ == "__main__":
    try:
        main()
    except SystemExit as error:
        print(error, file=sys.stderr)
        raise
