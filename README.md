# 🔒 AVault — Audio & Video Downloader

A desktop app that downloads and converts audio **and** video from YouTube, SoundCloud, TikTok, Instagram, Vimeo, and [hundreds more platforms](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md) — with optional lyrics, subtitle support, and a fully responsive UI that adapts to any window size.

![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey?style=flat-square&logo=windows)
![Platform](https://img.shields.io/badge/Platform-Linux-lightgrey?style=flat-square&logo=linux)
![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![Free](https://img.shields.io/badge/Free-Personal%20Use-green?style=flat-square)

---

## ✨ Features

### 🎵 Audio Mode
- Output formats: **MP3, WAV, FLAC**
- **MP3 at 320 kbps / 48 kHz**, lossless source quality for WAV & FLAC
- Embeds **cover art, title, artist, album, and year** metadata automatically
- **Lyrics support** — download as a separate `.lrc` file or embed directly into the MP3 (USLT tag)
- Optional **playlist download** support

### 🎬 Video Mode
- Output formats: **MP4, MKV**
- Choose your **resolution** — from 144p up to 4K, based on what the source offers
- Embeds **subtitles** directly into the container — no sidecar `.vtt` / `.srt` files left behind
- Supports both manual and **auto-generated subtitle tracks**
- Embeds **thumbnail and metadata** automatically
- MP4 uses `mov_text` (SRT), MKV uses native SRT/ASS — both embed correctly

### 🖥️ UI & Experience
- 🎨 **Live theme switching** — red accent in Audio mode, blue accent in Video mode, applied across all buttons, borders, progress bar, and focus rings
- 🔀 **Animated transitions** — smooth fade when switching between Audio and Video config panels
- 📐 **Fully responsive layout** — all panels, buttons, and the log area stretch to fill the window; works correctly on maximize, snap, and any arbitrary resize
- 📋 **Live log feedback** — every setting change (mode, format, lyrics option, resolution, subtitle track, output folder) is immediately reflected in the terminal — clears and shows the latest status so it never clutters
- 📊 **Real-time progress bar** that tracks download percentage
- ⏹️ **Cancel** button to stop at any time
- 🔄 **Reset** button to quickly start a new download
- ⟳ Built-in **Update yt-dlp** button to keep the tool up to date
- 🪟 Standalone `.exe` — no installation or Java required

### ⚙️ General
- 🔗 Supports **YouTube, SoundCloud, TikTok, Instagram, Vimeo**, and [many more](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md)
- 📋 **Analyze** a URL first to preview available subtitle/lyrics tracks and resolutions before downloading
- 📂 Choose your own output directory

---

## 📦 Download

1. Go to the [**Releases**](../../releases) page
2. Download the latest `AVault.exe`
3. Run it — that's it!

> ⚠️ **Windows only.** `yt-dlp` and `ffmpeg` are bundled inside — no extra setup required.

---

## 🛠️ How It Works

AVault uses **[yt-dlp](https://github.com/yt-dlp/yt-dlp)** to fetch streams and **[FFmpeg](https://ffmpeg.org/)** to encode and tag them. Lyrics are embedded using **[JAudioTagger](https://www.jthink.net/jaudiotagger/)**.

### Audio quality

| Format | Bitrate | Sample Rate |
|---|---|---|
| MP3 | 320 kbps | 48,000 Hz |
| WAV | Lossless | Source native |
| FLAC | Lossless | Source native |

### Audio metadata

| | Title / Artist / Album / Year | Cover Art |
|---|---|---|
| **MP3** | ✅ | ✅ |
| **FLAC** | ✅ | ✅ |
| **WAV** | ✅ | ❌ *(ffmpeg limitation)* |

### Lyrics modes (MP3 only)

| Mode | Result |
|---|---|
| No Lyrics | Audio only |
| Separate .lrc file | Lyrics saved alongside the MP3 |
| Embed in MP3 | Lyrics written into the MP3's metadata (USLT tag) |

### Video quality

| Format | Subtitles | Subtitle Format | Metadata & Thumbnail |
|---|---|---|---|
| **MP4** | ✅ Embedded | SRT (mov_text) | ✅ |
| **MKV** | ✅ Embedded | SRT / ASS | ✅ |

---

## 🏗️ Building from Source

**Requirements**
- Java 21+
- Maven

First, clone the repository and build the project. This step applies to all platforms:

```bash
git clone https://github.com/AngelosKalogeridis/AVault.git
cd AVault
mvn clean package
```

This will generate `avault-1.0-SNAPSHOT.jar` inside the `target/` directory.

Create a folder named `dist` in the root of the project and move the compiled JAR into it:

```bash
mkdir dist
# On Windows: copy target\avault-1.0-SNAPSHOT.jar dist\
# On Linux: cp target/avault-1.0-SNAPSHOT.jar dist/
```

### 🪟 Windows

### Additional Requirements
- `yt-dlp.exe`, `ffmpeg.exe`, `ffprobe.exe` and the compiled `avault-1.0-SNAPSHOT.jar` placed in `dist/`
- `app.ico` icon file in root directory

**Packaging to `.exe`**
```powershell
jpackage `
  --type exe `
  --name "AVault" `
  --app-version "1.0.0" `
  --vendor "AK006" `
  --icon "app.ico" `
  --input "dist" `
  --main-jar "avault-1.0-SNAPSHOT.jar" `
  --main-class "Launcher" `
  --dest "installer-output" `
  --win-shortcut `
  --win-menu `
  --win-dir-chooser `
  --description "AVault — Audio & Video Downloader"
```

> The bundled tools (`yt-dlp.exe`, `ffmpeg.exe`, `ffprobe.exe`) must be placed in `dist/` before running `jpackage`. They are resolved at runtime from the same folder as the JAR.

### 🐧 Linux

### Additional Requirements

- Linux native binaries for `yt-dlp`, `ffmpeg`, and `ffprobe` and the compiled `avault-1.0-SNAPSHOT.jar` placed in `dist/`
- `app.png` icon file in root directory (Linux does not support `.ico`)

**Packaging to `.deb`**
```bash
jpackage \
  --type deb \
  --name "AVault" \
  --app-version "1.0.0" \
  --vendor "AK006" \
  --icon "app.png" \
  --input "dist" \
  --main-jar "avault-1.0-SNAPSHOT.jar" \
  --main-class "Launcher" \
  --dest "installer-output" \
  --linux-shortcut \
  --linux-menu-group "AudioVideo" \
  --linux-app-category "AudioVideo" \
  --description "AVault — Audio & Video Downloader"
```

> The bundled Linux tools (`yt-dlp`, `ffmpeg`, `ffprobe`) must be placed in `dist/` before running `jpackage`. They are resolved at runtime from the same folder as the JAR.

### NOTE: Change --type deb to --type rpm for Fedora/RHEL
---

## 🔧 Built With

| Tool | Purpose |
|---|---|
| [Java 21](https://www.oracle.com/java/) | Core language |
| [JavaFX 21](https://openjfx.io/) | Desktop UI framework |
| [yt-dlp](https://github.com/yt-dlp/yt-dlp) | Downloading audio & video streams |
| [FFmpeg](https://ffmpeg.org/) | Encoding, merging, subtitle conversion & metadata tagging |
| [JAudioTagger](https://www.jthink.net/jaudiotagger/) | Embedding lyrics into MP3 |
| [Maven](https://maven.apache.org/) | Build & dependency management |
| [jpackage](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html) | Packaging into a standalone `.exe` |

---

## 🖥️ Screenshots

<table align="center">
  <tr>
    <td align="center">🎵 Audio Mode</td>
    <td align="center">🎬 Video Mode</td>
  </tr>
  <tr>
    <td align="center"><img width="376" alt="Audio Mode" src="https://github.com/user-attachments/assets/70bf2d6f-ce0d-4bb6-b7de-e2ca18961b2a" /></td>
    <td align="center"><img width="376" alt="Video Mode" src="https://github.com/user-attachments/assets/61bac582-7c11-41c0-95bb-c8e64eb4119d" /></td>
  </tr>
</table>

---

## ⚠️ Disclaimer

This tool is intended for **personal use only**. Please respect copyright laws and each platform's Terms of Service. Only download content you have the right to download.

> ❌ **DRM-protected platforms are not supported** — this includes Spotify, Deezer, Apple Music, Tidal, and similar services.
