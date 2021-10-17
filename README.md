# Pokémon Crystal Map, Music and Story Randomiser

A toolbox for various Pokémon Crystal map, music and story randomisers currently only usable by coders.

## Existing Capabilities
* randomising map visuals without randomising collision
* shuffling music pointers
* shuffling sfx pointers
* shuffling warps

## Accessibility

This project currently requires a basic knowledge of coding to use, though this is under development. For a full guide on downloading and running this project in Eclipse, see the wiki (https://github.com/TheG-Meister/pkc-mms-rando/wiki/Loading-and-running-in-Eclipse)

To create a ROM:
1. Clone a pret/pokecrystal style repository
2. Clone the source code for this project
3. Edit input file paths to those of the cloned pokecrystal repository
4. Change the main method in Notes.java to randomise the files you want
5. Compile and run this project
7. Merge the output files with a pret/pokecrystal style repository
8. Make the repository

To create a ROM that has also been randomised by the Universal Pokémon Randomiser:
1. Perform all the steps above
2. Make the original pret/pokecrystal style repository
3. Create a patch file from the vanilla ROM to the randomised ROM
4. Randomise the vanilla ROM with UPR
5. Patch the UPR-randomised ROM with the patch file from step 3
