# Pokecrystal Dissassembly Map, Music and Story Randomiser

pkc-mms-rando provides alternative randomisation options for Pokémon Crystal compared to a typical Pokemon randomiser.

## Capabilities

v0.1.0 offers the following randomisers

### Shuffle music pointers

Replace all occurrences of each music track with a different one. Some tracks do not loop and leave silence afterwards, but nothing can be randomised to silence alone.

### Shuffle sound effect pointers

Replace all occurrences of each sound effect with a diffferent one. **Can greatly reduce game speed** if short, common sounds such as menu bleeps are replaced with long battle sound effects.

### Shuffle warps

Shuffle destinations of selected warps. Two warp shufflers are provided. The first shuffles all warps in the game, leaving out important ones for balancing. The other only shuffles warps that lead to or from gatehouses, caves, train stations and ports. **Neither version is always completable** and **the player can get stuck after hopping ledges**.

### Randomise map blocks

Replaces each block of tiles on every map with a random tile that has the same collision. This randomiser is always completable, but **looks like an absolute mess**.

### Shuffle overworld sprite pointers

Replaces all occurrences of each sprite with a different sprite. In practice, this **can become visually glitchy** if the game loads too many or too few sprites.

## Limitations

pkc-mms-rando is still early in development. It **cannot edit ROM files** and **has no user interface**. Instead, randomisers are accessed from a command-line terminal, and read and write Pokémon Crystal disassembly files. Fortunately, this process can be automated, and generating a ROM is as simple as running a small command-line script using Windows Subsystem for Linux. Check the [Wiki](https://github.com/TheG-Meister/pkc-mms-rando/wiki) for more information, or join us on [Discord](https://discord.gg/nE5nZVqgkE) for update notifications on a user interface and direct ROM editing.

## Getting Started

**Check out the in-depth guide for first timers on the [Wiki](https://github.com/TheG-Meister/pkc-mms-rando/wiki/Getting-Started)!**

1. If on Windows 10 or 11, install [Windows Subsystem for Linux](https://docs.microsoft.com/en-us/windows/wsl/install)
1. Install prerequisites `make`, `gcc`, `git`, `bison`, `pkg-config`, `libpng-dev` and `openjdk-17-jre-headless`
2. `git clone https://github.com/rednex/rgbds -b v0.5.2 --depth=1`
3. `sudo make install -C rgbds`
5. Clone a disassembly and rename the directory to `input/`
6. Place `pkc-mms-rando-0.1.1.jar` and `run.sh` in the same directory as `input/`
7. `bash run.sh` adding options for randomisers, such as `--warps` or `--music-pointers`
8. Wait a few minutes for a file ending in `.gbc` to appear

### Command-line options

```
-h, --help                        - prints help information and exits
-v, --version                     - prints program version and exits
-d <dir>, --disassembly-in <dir>  - reads a disassembly from the provided directory
-D <dir>, --disassembly-out <dir> - writes out modified disassembly files to the
                                      provided directory (requires -d)
--warps                           - shuffles all warp destinations (requires -d)
--warp-areas                      - shuffles some warp destinations (requires -d)
--overworld-sprite-pointers       - shuffles overworld sprite pointers (requires -d)
--music-pointers                  - shuffles music pointers (requires -d)
--sfx-pointers                    - shuffles sound effect pointers (requires -d)
--map-blocks                      - randomises map blocks (requires -d)
```
### Further randomisation with Universal Pokemon Randomizer

All randomisers in keep the length of the ROM the same. This means that randomisation with the Universal Pokemon Randomizer (UPR) or UPR ZX can be added on top of pkc-mms-rando randomisation using a simple patch. I do this using Lunar IPS:

1. Download [Lunar IPS](https://fusoya.eludevisibility.org/lips/)
2. Make your original disassembly into a ROM
3. Randomise this ROM using UPR or UPR ZX
4. Open Lunar IPS, and use it to create a patch file from the vanilla ROM to the UPR-randomised ROM
5. Run the patch file, and select a pkc-mms-rando-randomised ROM to patch

## Resources

* [Wiki](https://github.com/TheG-Meister/pkc-mms-rando/wiki)
* [Discord](https://discord.gg/nE5nZVqgkE)

## Contributors

This project is maintained by Grant Futcher. Contact me on Discord (The G-Meister#4275) or via email (grantfutcher@live.co.uk).
