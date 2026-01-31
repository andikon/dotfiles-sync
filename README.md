# dotfiles-sync

A small, OS-aware Java utility to sync dotfiles between a Git repository and your `$HOME` directory.

Designed to live **inside your dotfiles repo** while also having its **own standalone repository** for focused development.

---

## What it does

`dotfiles-sync` copies files and directories:

- **from repo → `$HOME`** (`write`)
- **from `$HOME` → repo** (`sync`)

All synced files are explicitly defined in the Java source and can be scoped to:
- Linux
- macOS
- Windows

The correct files are selected automatically at runtime.

---

## Commands

| Command | Description |
|------|------------|
| `write` | Apply dotfiles from the repo to `$HOME` |
| `sync`  | Copy dotfiles from `$HOME` back into the repo |

---

## Repository layout (example)

```text
dotfiles/
├── DotfileSync.java/        ← git submodule (this repo)
│   └── DotfileSync.java
├── DotfileSync.sh           ← bash wrapper
├── DotfileSync.ps1          ← PowerShell wrapper
├── git/
├── nvim/
└── zsh/
````

---

## First-time setup

### 1. Clone your dotfiles repo (with submodules)

```bash
git clone --recurse-submodules <your-dotfiles-repo>
```

If already cloned:

```bash
git submodule update --init --recursive
```

---

### 2. Install Java (JDK)

This tool requires a **JDK**, not just a JRE.

Check:

```bash
java -version
javac -version
```

Java 11+ is required. Java 17+ recommended.

---

### 3. Make the wrappers executable (Unix only)

```bash
chmod +x DotfileSync.sh
```

---

## Usage

### Linux / macOS / WSL / Git Bash

```bash
./DotfileSync.sh write
./DotfileSync.sh sync
```

---

### Windows (PowerShell)

```powershell
.\DotfileSync.ps1 write
.\DotfileSync.ps1 sync
```

---

## How the wrappers work

The wrappers:

1. compile `DotfileSync.java` using `javac`
2. output class files to `.dotfiles-sync/`
3. run the program with `java`, forwarding all arguments

No build tools, no dependencies.

You can safely delete `.dotfiles-sync/` at any time.

---

## Adding new dotfiles

All configuration lives at the **top of `DotfileSync.java`** in the `DOTFILES` list.

### 1. Add the file or directory to your repo

Example:

```text
nvim/
└── init.lua
```

---

### 2. Add a new entry to `DOTFILES`

```java
new DotfileEntry(
    Path.of("nvim"),
    Path.of(".config/nvim"),
    EnumSet.of(OS.LINUX, OS.MACOS)
)
```

#### Fields explained

| Field              | Description                             |
| ------------------ | --------------------------------------- |
| `repoRelativePath` | Path relative to the dotfiles repo root |
| `homeRelativePath` | Path relative to `$HOME`                |
| `supportedOS`      | OSes where this entry applies           |

---

### 3. Run sync

Apply to your system:

```bash
./DotfileSync.sh write
```

Or capture local changes:

```bash
./DotfileSync.sh sync
```

---

## Notes

* Files and directories are copied recursively
* Existing files are overwritten
* Missing sources are skipped
* OS is detected automatically at runtime

---

## Why Java?

* Cross-platform
* Strong filesystem APIs
* Easy to extend as the tool grows
* No shell-specific edge cases

---

## License

MIT
