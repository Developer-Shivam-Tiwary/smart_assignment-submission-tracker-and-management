#!/usr/bin/env python3

import os
import shutil
import subprocess
import sys

PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
SRC_DIR = os.path.join(PROJECT_ROOT, "src", "main", "java")
RESOURCES_DIR = os.path.join(PROJECT_ROOT, "src", "main", "resources")
BIN_DIR = os.path.join(PROJECT_ROOT, "bin")
LIB_DIR = os.path.join(PROJECT_ROOT, "lib")


def compile_java():
    print("=" * 60)
    print("Compiling Java Backend...")
    print("=" * 60)

    os.makedirs(BIN_DIR, exist_ok=True)

    java_files = []

    for root, _, files in os.walk(SRC_DIR):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))

    if not java_files:
        print("[ERROR] No Java files found!")
        return False

    with open("sources.txt", "w") as f:
        f.write("\n".join(java_files))

    result = subprocess.run([
        "javac",
        "-encoding",
        "UTF-8",
        "-cp",
        f"{LIB_DIR}/*",
        "-d",
        BIN_DIR,
        "@sources.txt"
    ])

    if os.path.exists("sources.txt"):
        os.remove("sources.txt")

    if result.returncode != 0:
        print("[ERROR] Compilation failed!")
        return False

    db_prop = os.path.join(RESOURCES_DIR, "database.properties")

    if os.path.exists(db_prop):
        shutil.copy(db_prop, BIN_DIR)

    print("[SUCCESS] Compilation completed.")
    return True


def run_backend():
    if not compile_java():
        sys.exit(1)

    print("=" * 60)
    print("Starting Backend Server...")
    print("=" * 60)

    if os.name == "nt":
        classpath = f"{BIN_DIR};{LIB_DIR}/*"
    else:
        classpath = f"{BIN_DIR}:{LIB_DIR}/*"

    subprocess.run([
        "java",
        "-cp",
        classpath,
        "com.smartassignment.server.AppServer"
    ])


if __name__ == "__main__":
    run_backend()