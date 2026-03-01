# 🎵 Audio Converter + Lyrics

A desktop app that converts audio from YouTube, SoundCloud, TikTok, Instagram, and many more platforms to high-quality MP3, WAV, or FLAC — with optional lyrics support.

![Platform](https://img.shields.io/badge/Platform-Windows-lightgrey?style=flat-square&logo=windows)
![Free](https://img.shields.io/badge/Free-Personal%20Use-green?style=flat-square)

---

## ✨ Features

- 🔗 Supports **YouTube, SoundCloud, TikTok, Instagram, Vimeo**, and [many more](https://github.com/yt-dlp/yt-dlp/blob/master/supportedsites.md)
- 🎵 Output formats: **MP3, WAV, FLAC**
- 🎧 High-quality audio — **MP3 at 320 kbps / 48 kHz**, lossless source quality for WAV & FLAC
- 🖼️ Embeds **cover art, title, artist, album, and year** metadata automatically
- 🎤 **Lyrics support** — download as a separate `.lrc` file or embed directly into the MP3
- 📋 **Analyze** a video first to see all available subtitle/lyrics tracks before downloading
- 📂 Choose your own output directory
- 📊 Live progress bar and real-time conversion log
- ⏹️ **Cancel** button to stop a conversion at any time
- 🔄 Reset button to quickly start a new conversion
- ⟳ Built-in **Update yt-dlp** button to keep the tool up to date
- 🎞️ Optional **playlist download** support
- 🪟 Standalone `.exe` — no installation or Java required

---

## 📦 Download

1. Go to the [**Releases**](../../releases) page
2. Download the latest `AudioConverter.exe`
3. Run it — that's it!

> ⚠️ **Windows only.** `yt-dlp` and `ffmpeg` are bundled inside — no extra setup required.

---

## 🛠️ How It Works

The app uses **[yt-dlp](https://github.com/yt-dlp/yt-dlp)** to fetch the audio stream and **[FFmpeg](https://ffmpeg.org/)** to encode and tag it. Lyrics are embedded using **[JAudioTagger](https://www.jthink.net/jaudiotagger/)**.

### Audio quality

| Format | Bitrate | Sample Rate |
|---|---|---|
| MP3 | 320 kbps | 48,000 Hz |
| WAV | Lossless | Source native |
| FLAC | Lossless | Source native |

### Metadata per format

| | Title / Artist / Album / Year | Cover Art |
|---|---|---|
| **MP3** | ✅ | ✅ |
| **FLAC** | ✅ | ✅ |
| **WAV** | ✅ | ❌ *(ffmpeg limitation)* |

### Lyrics modes (MP3 only)

| Mode | Result |
|---|---|
| No Lyrics | Audio only |
| Separate .lrc file | Lyrics saved as a standalone file alongside the MP3 |
| Embed in MP3 | Lyrics written into the MP3's metadata (USLT tag) |

---

## 🖥️ Screenshots
<p align="center">
    <img width="600" alt="image" src="https://github.com/user-attachments/assets/4d6e7bed-1dba-4c56-9437-0ac01b44b480" />
</p> 

---

## 🔧 Built With

| Tool | Purpose |
|---|---|
| [Java 21](https://www.oracle.com/java/) | Core language |
| [JavaFX 21](https://openjfx.io/) | Desktop UI framework |
| [yt-dlp](https://github.com/yt-dlp/yt-dlp) | Audio downloading |
| [FFmpeg](https://ffmpeg.org/) | Audio encoding & metadata tagging |
| [JAudioTagger](https://www.jthink.net/jaudiotagger/) | Embedding lyrics into MP3 |
| [Maven](https://maven.apache.org/) | Build & dependency management |
| [jpackage](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html) | Packaging into a standalone `.exe` |

---

## ⚠️ Disclaimer

This tool is intended for **personal use only**. Please respect copyright laws and each platform's Terms of Service. Only download content you have the right to download.

> ❌ **DRM-protected platforms are not supported** — this includes Spotify, Deezer, Apple Music, Tidal, and similar services.
