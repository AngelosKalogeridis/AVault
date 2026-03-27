# 🔒 AVault — Audio & Video Downloader

A desktop app that downloads and converts audio **and** video from YouTube, SoundCloud, TikTok, Instagram, Vimeo, and [hundreds more platforms](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md) — with optional lyrics and subtitle support.

![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey?style=flat-square&logo=windows)
![Free](https://img.shields.io/badge/Free-Personal%20Use-green?style=flat-square)

---

## ✨ Features

### 🎵 Audio Mode
- Output formats: **MP3, WAV, FLAC**
- **MP3 at 320 kbps / 48 kHz**, lossless source quality for WAV & FLAC
- Embeds **cover art, title, artist, album, and year** metadata automatically
- **Lyrics support** — download as a separate `.lrc` file or embed directly into the MP3
- Optional **playlist download** support

### 🎬 Video Mode
- Output formats: **MP4, MKV**
- Choose your **resolution** — from 144p up to 4K, based on what the source offers
- Embeds **subtitles** directly into the container (no sidecar files left behind)
- Supports both manual and **auto-generated subtitle tracks**
- Embeds **thumbnail and metadata** automatically

### ⚙️ General
- 🔗 Supports **YouTube, SoundCloud, TikTok, Instagram, Vimeo**, and [many more](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md)
- 📋 **Analyze** a URL first to preview available subtitle/lyrics tracks and resolutions
- 📂 Choose your own output directory
- 📊 Live progress bar and real-time conversion log
- ⏹️ **Cancel** button to stop at any time
- 🔄 **Reset** button to quickly start a new download
- ⟳ Built-in **Update yt-dlp** button to keep the tool up to date
- 🪟 Standalone `.exe` — no installation or Java required

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

| Format | Subtitles | Metadata & Thumbnail |
|---|---|---|
| **MP4** | ✅ Embedded (SRT/mov_text) | ✅ |
| **MKV** | ✅ Embedded (SRT/ASS) | ✅ |

---

## 🔧 Built With

| Tool | Purpose |
|---|---|
| [Java 21](https://www.oracle.com/java/) | Core language |
| [JavaFX 21](https://openjfx.io/) | Desktop UI framework |
| [yt-dlp](https://github.com/yt-dlp/yt-dlp) | Downloading audio & video streams |
| [FFmpeg](https://ffmpeg.org/) | Encoding, merging & metadata tagging |
| [JAudioTagger](https://www.jthink.net/jaudiotagger/) | Embedding lyrics into MP3 |
| [Maven](https://maven.apache.org/) | Build & dependency management |
| [jpackage](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html) | Packaging into a standalone `.exe` |

---

## 🖥️ Screenshots

<table align="center">
  <tr>
    <td align="center"><img width="376" alt="Audio Mode" src="https://github.com/user-attachments/assets/3856799e-acf1-4982-954d-bca15738ab80" /></td>
    <td align="center"><img width="376" alt="Video Mode" src="https://github.com/user-attachments/assets/5c13d618-8a37-4c2f-9602-0ba2bb606259" /></td>
  </tr>
  <tr>
    <td align="center">🎵 Audio Mode</td>
    <td align="center">🎬 Video Mode</td>
  </tr>
</table>

---

## ⚠️ Disclaimer

This tool is intended for **personal use only**. Please respect copyright laws and each platform's Terms of Service. Only download content you have the right to download.

> ❌ **DRM-protected platforms are not supported** — this includes Spotify, Deezer, Apple Music, Tidal, and similar services.
