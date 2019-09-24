FluidTank for 1.14.4 and Forge

**This mod requires [Scalable Cat's Force](https://minecraft.curseforge.com/projects/scalable-cats-force) to work.**

**You have to download version 2.13.1 of [Scalable Cat's Force](https://minecraft.curseforge.com/projects/scalable-cats-force).**
I don't support any other environment.

**This version requires forge newer than 28.1.0 (Forge Recommend version)**

## v14.8.1
- Prevented recursive transfer by checking source and destination pos and equality of handler instance.
- Fixed output side was ignored.
- Changed amount to transfer in a tick.
- Fixed creative tank didn't push to pipes.
- Added helper method to check output and input.
- Removed log when connection is full
- Added code to reset buffer translation. This will prevent some render issue with other mods.