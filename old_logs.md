## v15.2.8
* Added item pipe.
* Internal changes
* Changed the number of fluid pipe crafted in the easy recipe.

## v15.2.7
* **Requires library mod newer than [2.13.2-build2](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force/files/2954109)**
* Internal changes
* Update forge to 31.2.0
* Update mapping

## v15.2.6
* Changed pipe transfer rate to 16,000 mB/t.
* Pipes can be colored by dye.
* Disabled the rainbow renderer by default. (Config option)
* Translucent pipe color.
* Not to connect to dyed pipe when white player placed pipe.
* Added "Void Tank".
* Changed pipe shape when pipe is facing fluid handler tile.
* Simplified model json files.

## v15.2.5
* Fixed rendering issue. [#45](https://github.com/Kotori316/FluidTank/issues/45)

## v15.2.4
* Internal changes.
* Changed nbt serialization of Tier.
  * This change will cause error of tanks. Replace tanks to fix problem.

## v15.2.3
* Added top support and fixed JEI recipe.
* Stop blocks from working when right clicked with shift key.

## v15.2.2
* Sync supplier with the client to show correct item name.
* Changed item name
* Added option to disable fluid supplier.
* Use own color to render pipe.
* Warn if ingredient is empty
* Removed config entries for recipes.
* Moved sub item to recipe json.

## v15.2.1
* Working on recipe issue
## v15.2.0
* Update to Minecraft 1.15.2
  * **REQUIRES NEW LIBRARY. Install from [here](https://www.curseforge.com/minecraft/mc-mods/scalable-cats-force/files/2871351).**
* Fixed tank recipe was wrong.
* Log recipe information to debug logger.
## v15.0.5
- Fixed tanks with EMPTY fluid was trying to render their content.
## v15.0.4
- Add support of Hwyla.
- ~Add a rainbow renderer for the pipe.~ Disabled by default.
- Some changes in the item renderer. (Call `RenderHelper#disableStandardItemLighting()` every time.)
## v15.0.3
- Changed texture of "Infinite Fluid Supplier".
- Changed the item name from "Infinite Fluid Supplier" tp "Infinite Water Supplier".
## v15.0.2
- Implemented item renderer.
- Fixed - creative tank had no renderer.
- Improvement - render lighting (the brightness of content.)
- Remove SNAPSHOT
## v15.0.1-SNAPSHOT
- Render for tanks in world.
- **Please wait for the renderer for tank items.**

## v15.0.0-SNAPSHOT
- Initial build for Minecraft 1.15.1
- **NO RENDERERS ARE AVAILABLE.** I'm still working.
- Other functions work fine. (Inserting or taking fluid)
