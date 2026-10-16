#!/usr/bin/env python3
import sys
import re

def update_readme_links(version_no_v: str, is_prerelease: bool):
    version_with_v = f"v{version_no_v}"
    link = f"[DevBrowser-{version_no_v}-release.apk](../../releases/download/{version_with_v}/DevBrowser-{version_no_v}-release.apk)"

    try:
        with open("README.md", "r", encoding="utf-8") as f:
            content = f.read()

        # Update line under **Latest Release (includes unstable):**
        content = re.sub(
            r"(\*\*Latest Release \(includes unstable\):\*\*[^\S\r\n]*\r?\n)[^\r\n]+",
            r"\g<1>" + link,
            content
        )

        # Update line under **Latest Stable Release:** if not a pre-release
        if not is_prerelease:
            content = re.sub(
                r"(\*\*Latest Stable Release:\*\*[^\S\r\n]*\r?\n)[^\r\n]+",
                r"\g<1>" + link,
                content
            )

        with open("README.md", "w", encoding="utf-8") as f:
            f.write(content)
        print(f"Updated README.md with link: {link} (is_prerelease={is_prerelease})")
    except Exception as e:
        print(f"Error updating README.md: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: update_readme_links.py <version_no_v> <is_prerelease>", file=sys.stderr)
        sys.exit(1)
    v_no_v = sys.argv[1]
    is_pre = sys.argv[2].lower() == "true"
    update_readme_links(v_no_v, is_pre)
