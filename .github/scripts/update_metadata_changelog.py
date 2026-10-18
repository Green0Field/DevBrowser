#!/usr/bin/env python3
import sys
import os

def update_metadata_changelog(short_desc: str, version_no_v: str, version_code: str):
    shortlog_path = "release_shortlog.txt"
    shortlog_content = ""
    if os.path.exists(shortlog_path):
        with open(shortlog_path, "r", encoding="utf-8") as f:
            shortlog_content = f.read().strip()

    # Clean up excessive indentation (e.g. 6 spaces from git shortlog) to 2 spaces
    cleaned_lines = []
    for line in shortlog_content.splitlines():
        if line.startswith("      "):
            cleaned_lines.append("  " + line[6:])
        else:
            cleaned_lines.append(line)
    cleaned_shortlog = "\n".join(cleaned_lines)

    lines = []
    if short_desc.strip():
        lines.append(f"**Short Description:** {short_desc.strip()}")
    lines.append("```")
    lines.append(cleaned_shortlog)
    lines.append("```")

    full_text = "\n".join(lines).strip()

    # Enforce F-Droid's strict limit of max 500 characters
    if len(full_text) > 490:
        trimmed_lines = []
        for line in cleaned_lines:
            trimmed_lines.append(line)
            candidate = ""
            if short_desc.strip():
                candidate += f"**Short Description:** {short_desc.strip()}\n"
            candidate += "```\n" + "\n".join(trimmed_lines) + "\n...\n```"
            if len(candidate) > 480:
                trimmed_lines.pop()
                break

        full_text = ""
        if short_desc.strip():
            full_text += f"**Short Description:** {short_desc.strip()}\n"
        full_text += "```\n" + "\n".join(trimmed_lines) + "\n...\n```"

        if len(full_text) > 490:
            full_text = full_text[:486] + "...\n```"

    dest_dir = "fastlane/metadata/android/en-US/changelogs"
    os.makedirs(dest_dir, exist_ok=True)

    targets = set()
    if version_code.strip():
        targets.add(f"{version_code.strip()}.txt")
    elif version_no_v.strip():
        targets.add(f"{version_no_v.strip()}.txt")

    for target in targets:
        target_path = os.path.join(dest_dir, target)
        with open(target_path, "w", encoding="utf-8") as f:
            f.write(full_text + "\n")
        print(f"Updated metadata changelog: {target_path} ({len(full_text)} chars)")

if __name__ == "__main__":
    desc = sys.argv[1] if len(sys.argv) > 1 else ""
    v_no_v = sys.argv[2] if len(sys.argv) > 2 else ""
    v_code = sys.argv[3] if len(sys.argv) > 3 else ""
    update_metadata_changelog(desc, v_no_v, v_code)
