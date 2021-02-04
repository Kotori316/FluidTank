## v16.1.4
* Fixed an issue of fluid NBT.
* Respect colors from fluid NBT.
* Rendering improvement. Honey and Chocolate from mod "Create" will be correctly rendered.

Build with net.minecraftforge:forge:1.16.5-36.0.1, Mapping: 20201028-1.16.3

## v16.1.3
* Fixed a problem of drain/fill tanks when world was reloaded.
* Removed usage of tick action of tanks.

Build with net.minecraftforge:forge:1.16.5-36.0.1, Mapping: 20201028-1.16.3

## v16.1.2
* Add a config option to disable convert invisible recipe.
* Add a config option to hide non-creatable tanks from high tier tank recipe.

Build with net.minecraftforge:forge:1.16.5-36.0.1, Mapping: 20201028-1.16.3

## v16.1.1
* Show tank content even if it has less than 1% of its capacity. #69
* Stopped log spam of converting message.
* Fixed that pipe didn't drain from Creative Tank.

Build with net.minecraftforge:forge:1.16.5-36.0.1, Mapping: 20201028-1.16.3

## v16.1.0
* Update for Minecraft 1.16.5
* Support Milk provided by Forge.
  * Milks by this mod will be converted into forge's one if you're in 1.16.5.
* Internal changes.

Build with net.minecraftforge:forge:1.16.5-36.0.1, Mapping: 20201028-1.16.3

## v16.0.9
* Fixed transparent texture not rendered correctly. #68
* Fixed fluid pipe wasn't visually connected.

Build with net.minecraftforge:forge:1.16.4-35.1.4, Mapping: 20201028-1.16.3

## v16.0.8
* Remove usage of unstable mixin. 
The error of 
"java.util.NoSuchElementException: Supplier is empty. Mixin system for `kotori_scala`(Scalable Cats Force) seems not working."
is fixed.

Build with net.minecraftforge:forge:1.16.4-35.0.18, Mapping: 20201028-1.16.3

## v16.0.7
* Update library, requires 2.13.3-build-6
* Many internal changes.
* Added support of mod "look".
* Fixed a potential bug of sync.
* Pipes can be dyed with leather armors.

Build with net.minecraftforge:forge:1.16.4-35.0.18, Mapping: 20201028-1.16.3

## v16.0.6
* Update Russian lang file.

Build with net.minecraftforge:forge:1.16.3-34.1.12, Mapping: 20200723-1.16.1

## v16.0.5
* The log is limited only in dev environment.
* Update library, requesting ScalableCatsForce >= 2.13.3
* Update forge.
* Added lang file of pt_br and fr_fr.

Build with net.minecraftforge:forge:1.16.3-34.1.0, Mapping: 20200723-1.16.1

## v16.0.4
* Support HWYLA
* Fixed a bug that Creative Tank fills normal tanks with empty fluid.

Build with net.minecraftforge:forge:1.16.2-33.0.5, Mapping: 20200723-1.16.1

## v16.0.3
* Update for 1.16.2

Build with net.minecraftforge:forge:1.16.2-33.0.0, Mapping: 20200723-1.16.1

## v16.0.1
* Fixed CAT got fluid from empty inventory.
* Fixed the pipe didn't have dummy bounding box when pipe was not connected to tank.
* Remove shadow from text in CAT tile.

Build with net.minecraftforge:forge:1.16.1-32.0.23, Mapping: 20200514-1.16

## v16.0.0-SNAPSHOT
* First release for Minecraft 1.16.1
* No support of tooltip mod.
* **Changed tank capacity.**
Build with net.minecraftforge:forge:1.16.1-32.0.6
