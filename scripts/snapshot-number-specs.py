#!/usr/bin/env python3
"""Snapshot all real numeric mapping entries for the two-filter contract matrix."""
import json
from pathlib import Path
import sys

if len(sys.argv) != 3:
    sys.exit("usage: snapshot-number-specs.py COMMUNITY_REPO ENTERPRISE_REPO")

entries = []
for repo, directory in zip(("community", "enterprise"), sys.argv[1:]):
    root = Path(directory)
    for path in sorted(root.glob("**/src/main/resources/*.json")):
        try:
            # Keep exponent/decimal bounds exact even beyond Python float capacity.
            types = json.loads(path.read_text(), parse_float=str).get("dataTypes", {})
        except (ValueError, AttributeError):
            continue
        if not isinstance(types, dict):
            continue
        for expression, entry in types.items():
            if not isinstance(entry, dict) or entry.get("to") not in ("TapNumber", "TapFloat", "TapDouble", "TapCoefficientFloat"):
                continue
            entries.append(dict(repo=repo, path=str(path.relative_to(root)), expression=expression,
                                binary=entry.get("binaryFloat") is True or entry.get("to") in ("TapFloat", "TapDouble", "TapCoefficientFloat"), entry=entry))
output = Path(__file__).resolve().parents[1] / "plugin-kit/tapdata-api/src/test/resources/compat/number-specs.json"
output.write_text("[\n" + ",\n".join("  " + json.dumps(entry, separators=(",", ":")) for entry in entries) + "\n]\n")
print(f"Snapshotted {len(entries)} numeric entries")
