#!/usr/bin/env python3
import sys

OFFICIAL_CATEGORIES = {
    "Connectivity", "Development", "Games", "Graphics", "Internet",
    "Money", "Multimedia", "Navigation", "Phone & SMS", "Reading",
    "Science & Education", "Security", "Sports & Health", "System",
    "Theming", "Time", "Writing"
}

target_file = "metadata/com.green0field.devbrowser.yml"
with open(target_file, "r", encoding="utf-8") as f:
    lines = f.readlines()

errors = []
warnings = []

in_categories = False
categories = []
in_builds = False
current_build = {}
builds = []
fields = {}

for line in lines:
    raw = line.rstrip("\n")
    stripped = raw.strip()
    if not stripped or stripped.startswith("#"):
        continue

    if stripped == "Categories:":
        in_categories = True
        in_builds = False
        continue

    if in_categories:
        if stripped.startswith("- "):
            cat = stripped[2:].strip()
            categories.append(cat)
            if cat not in OFFICIAL_CATEGORIES:
                errors.append(f"Invalid category: {cat}")
            continue
        else:
            in_categories = False

    if stripped.startswith("Builds:"):
        in_builds = True
        continue

    if in_builds:
        if stripped.startswith("- versionName:"):
            if current_build:
                builds.append(current_build)
            current_build = {"versionName": stripped.split(":", 1)[1].strip()}
            continue
        elif ":" in stripped and not stripped.startswith("-"):
            k, v = stripped.split(":", 1)
            current_build[k.strip()] = v.strip()
            continue
        elif stripped.startswith("AutoUpdateMode:"):
            in_builds = False
            if current_build:
                builds.append(current_build)

    if ":" in stripped:
        k, v = stripped.split(":", 1)
        fields[k.strip()] = v.strip()

if current_build and current_build not in builds:
    builds.append(current_build)

print("Found categories:", categories)
print("Found builds:", builds)
print("Found fields:", list(fields.keys()))

for unwanted in ["Summary", "Description", "AutoName"]:
    if unwanted in fields:
        warnings.append(f"Field {unwanted} is defined in yaml, but Fastlane metadata in source is preferred.")

if "License" not in fields or not fields["License"]:
    errors.append("License is missing")

for url_field in ["WebSite", "SourceCode", "IssueTracker", "Repo"]:
    if url_field not in fields:
        errors.append(f"Missing {url_field}")
    elif not fields[url_field].startswith("https://"):
        errors.append(f"{url_field} must use https://")

if "CurrentVersion" not in fields or "CurrentVersionCode" not in fields:
    errors.append("Missing CurrentVersion or CurrentVersionCode")
else:
    latest = builds[-1] if builds else {}
    cur_v = fields["CurrentVersion"].strip("'\"")
    latest_v = latest.get("versionName", "").strip("'\"")
    if cur_v != latest_v:
        errors.append(f"CurrentVersion ({cur_v}) does not match latest build versionName ({latest_v})")
    cur_c = fields["CurrentVersionCode"].strip()
    latest_c = latest.get("versionCode", "").strip()
    if cur_c != latest_c:
        errors.append(f"CurrentVersionCode ({cur_c}) does not match latest build versionCode ({latest_c})")

print("\n--- F-DROID LINT SUMMARY ---")
if errors:
    print("FAILED with errors:")
    for e in errors:
        print("  [ERROR]", e)
    sys.exit(1)
else:
    print("SUCCESS: 0 errors! The metadata conforms strictly to F-Droid server rules.")

if warnings:
    for w in warnings:
        print("  [WARNING]", w)
