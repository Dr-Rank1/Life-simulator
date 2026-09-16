#!/usr/bin/env python3
"""
Backfill missing Android string translations using the Gemini API.

Reads the default values/strings.xml as the source of truth, compares
with each locale (values-es, values-fr, values-hi, values-sw, values-pt),
and uses Gemini to translate any missing strings.

Usage:
    export GEMINI_API_KEY=your-key-here
    python3 scripts/backfill_translations.py

Requires:
    pip install google-genai
"""

import os
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

try:
    from google import genai
except ImportError:
    print("ERROR: google-genai package not installed. Run: pip install google-genai")
    sys.exit(1)

# Configuration
RES_DIR = Path(__file__).resolve().parent.parent / "app" / "src" / "main" / "res"
DEFAULT_STRINGS = RES_DIR / "values" / "strings.xml"

LOCALES = {
    "values-es": "Spanish",
    "values-fr": "French",
    "values-hi": "Hindi",
    "values-sw": "Swahili",
    "values-pt": "Portuguese (Brazil)",
}

MODEL = "gemini-2.0-flash"
BATCH_SIZE = 30


def parse_string_names(path):
    """Parse an Android strings.xml and return {name: value} dict."""
    tree = ET.parse(path)
    root = tree.getroot()
    result = {}
    for elem in root.findall("string"):
        name = elem.get("name")
        if elem.get("translatable") == "false":
            continue
        text = ET.tostring(elem, encoding="unicode", method="xml")
        match = re.search(r">(.*?)</string>", text, re.DOTALL)
        if match and name:
            result[name] = match.group(1).strip()
    return result


def find_missing(default_strings, locale_path):
    """Return {name: default_value} for strings missing in the locale file."""
    if not locale_path.exists():
        return dict(default_strings)
    locale_strings = parse_string_names(locale_path)
    missing = {}
    for name, value in default_strings.items():
        if name not in locale_strings:
            missing[name] = value
    return missing


def translate_batch(client, strings, language):
    """Translate a batch of strings using Gemini. Returns {name: translated}."""
    if not strings:
        return {}

    entries = "\n".join(
        f"  {name} = {value}" for name, value in strings.items()
    )
    prompt = (
        f"You are translating Android app strings to {language}.\n\n"
        "RULES:\n"
        "1. Preserve ALL format specifiers exactly as-is: %1$s, %1$d, %2$s, etc.\n"
        '2. Preserve escaped characters like \\" and \\\'\n'
        "3. Do NOT translate proper nouns or brand names.\n"
        "4. Keep translations natural and concise for a mobile UI.\n"
        "5. Return ONLY the translations in the exact format: name = translated text\n"
        "6. One translation per line. No extra commentary.\n\n"
        f"Translate these strings:\n{entries}"
    )

    response = client.models.generate_content(model=MODEL, contents=prompt)

    result = {}
    if response.text:
        for line in response.text.strip().split("\n"):
            line = line.strip()
            if " = " in line:
                name, _, value = line.partition(" = ")
                name = name.strip()
                value = value.strip()
                if name in strings:
                    result[name] = value
    return result


def append_strings_to_file(locale_path, translations):
    """Append translated strings to the locale strings.xml file."""
    if not locale_path.exists():
        lines = ['<?xml version="1.0" encoding="utf-8"?>\n', "<resources>\n"]
        for name, value in translations.items():
            lines.append(f'    <string name="{name}">{value}</string>\n')
        lines.append("</resources>\n")
        locale_path.parent.mkdir(parents=True, exist_ok=True)
        locale_path.write_text("".join(lines), encoding="utf-8")
        return

    content = locale_path.read_text(encoding="utf-8")
    insert_point = content.rfind("</resources>")
    if insert_point == -1:
        print(f"  WARNING: Could not find </resources> in {locale_path}")
        return

    new_entries = "\n".join(
        f'    <string name="{name}">{value}</string>'
        for name, value in translations.items()
    )
    new_content = content[:insert_point] + "\n" + new_entries + "\n" + content[insert_point:]
    locale_path.write_text(new_content, encoding="utf-8")


def main():
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key:
        print("ERROR: Set the GEMINI_API_KEY environment variable.")
        print("  export GEMINI_API_KEY=your-key-here")
        sys.exit(1)

    client = genai.Client(api_key=api_key)

    print(f"Reading default strings from {DEFAULT_STRINGS}")
    default_strings = parse_string_names(DEFAULT_STRINGS)
    print(f"  Found {len(default_strings)} translatable strings.\n")

    for locale_dir, language in LOCALES.items():
        locale_path = RES_DIR / locale_dir / "strings.xml"
        missing = find_missing(default_strings, locale_path)

        if not missing:
            print(f"[{locale_dir}] {language}: All strings present.")
            continue

        print(f"[{locale_dir}] {language}: {len(missing)} missing strings.")

        all_translations = {}
        keys = list(missing.keys())

        for i in range(0, len(keys), BATCH_SIZE):
            batch_keys = keys[i : i + BATCH_SIZE]
            batch = {k: missing[k] for k in batch_keys}
            print(f"  Translating batch {i // BATCH_SIZE + 1} ({len(batch)} strings)...")
            translated = translate_batch(client, batch, language)
            all_translations.update(translated)

        if all_translations:
            append_strings_to_file(locale_path, all_translations)
            print(f"  Added {len(all_translations)} translations to {locale_path}")
        else:
            print(f"  WARNING: No translations received from API.")

        print()

    print("Done! Run ./gradlew lintDebug to verify string format consistency.")


if __name__ == "__main__":
    main()
