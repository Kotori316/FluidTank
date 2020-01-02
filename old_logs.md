## v14.9.1
- Changed texture of "Infinite Fluid Source".

## v14.9.0
- Added "Infinite Fluid Source"

## v14.8.9
- Fixed pipe is removed by flowing water.
- Changed texture of pipe.
- Removed unused element in config.

## v14.8.8
- Enabled storing milk in a tank. [#31](https://github.com/Kotori316/FluidTank/pull/31)

## v14.8.7
- Added de_de.json, German language file.
- Recipe changed - Fluid Pipe. Ender pearl and Ender eye can be used as the center item.
- Recipe changed - Pipe and Chest as Tank can accept invisible wood tank.
- Fixed - ItemFluidHandler of tanks had invalid fluid.
- Fixed - tank on creative tank wasn't filled when placed.

## v14.8.6
- Fixed - Very big tower of tanks (>= 100) causes lag and takes too longer time to load. [#27](https://github.com/Kotori316/FluidTank/issues/27)

## v14.8.5
- Refactored data generation code.
- Update forge to 1.14.4-28.1.87
- Use early return in filling fluid from pipe to reduce accessing to world.
- Enabled GitHub Action and package. Codes will automatically compiled to make jar file.
You can get latest build from https://github.com/Kotori316/FluidTank/packages/56985

## v14.8.4
- Added - tank recipes of non forge metal.
- Improvement - added data provider for recipes to make json file. Renamed class name.

## v14.8.3
- Fixed Assertion Error when "Chest as Tank" was right-clicked with fluid item if item container didn't exist.
- Separating connection is enabled.

## v14.8.2
- Smarter connection.
- Added fluid handler of pipe to fill tanks via pipe. Draining via pipes isn't available.
- helper method to compare.
- Added util method to convert boolean and FluidAction.
- Require group instance to get the distance from a specific coord.
- Changed the condition of facing to get fluid handler from CAT.
- Fixed NPE when pipe connection is changed.

## v14.8.1
- Prevented recursive transfer by checking source and destination pos and equality of handler instance.
- Fixed output side was ignored.
- Changed amount to transfer in a tick.
- Fixed creative tank didn't push to pipes.
- Added helper method to check output and input.
- Removed log when connection is full
- Added code to reset buffer translation. This will prevent some render issue with other mods.

## v14.8.0
- Update to new Scala (2.13.1) **Requires version 2.13.1 of library.**
- Added fluid pipe (BETA). Right click pipe to change mode. (No Connection -> Connected -> Input -> Output)  
  Fluids are transported from input to output.  
  Unlike BC's pipe, the pipe has no internal tank.

## v14.7.5
* Improvement - fill and empty sound is played. Kotori316 2019/09/13 2:42
* Improvement - stacked containers can be used to fill or drain. Kotori316 2019/09/13 2:42

## v14.7.4
* Added new block - Chest as Tank Kotori316 2019/09/11 19:04
* Fixed - empty item had fluid capability. Kotori316 2019/09/11 18:30
* Fixed - returns EMPTY instance instead of null when item tank is drained. Kotori316 2019/09/11 18:29

## v14.7.3
* Re-added IFluidHandlerItem Kotori316 2019/09/03 21:58
* Use forge FluidUtil to fill tank and dill bucket Kotori316 2019/09/03 21:58

## v14.7.2
* Use nbt tag to check equability Kotori316 2019/09/01 14:08
* Early return if fluid to fill is incompatible with connection fluid. Kotori316 2019/09/01 13:36

## v14.7.1
* Update for Forge 28.0.74, implementing new fluid system.
* Fixed a crash due to the update.([#20](https://github.com/Kotori316/FluidTank/issues/20))  
  `Caused by: java.lang.ClassNotFoundException: net.minecraftforge.common.crafting.IConditionSerializer`
* Support FluidHandler provided by forge. This is the test implementation so please tell me if you find some bugs.

## v14.7.0
* Use own class loader to send a message to users not installing library file. Kotori316 2019/08/27 13:05
  This change force you to install my library file.

## v14.6.9
* Update - Scala 2.12.9, Cats 1.6.1, only affects to develop environment. This mod works on both Scala 2.12.9 and 2.12.8.
* Refactoring - Use vanilla loot table system to drop tank. Kotori316 2019/08/27 3:14
* Refactoring - Use method to check creative mode. Kotori316 2019/08/27 3:47

## v14.6.8
* Addition - Added zn_cn.json.
* Fixed - Fixed tr_tr.json was not correct.

## v14.6.7
* Added - Support of The One Probe
* Update - for Minecraft 1.14.4, Forge 1.14.4-28.0.45 [#18](https://github.com/Kotori316/FluidTank/issues/18)
* Update - Dependencies(JEI, HWYLA)

## v14.6.6
* Fixed - JEI recipe was wrong.
* Update - for Minecraft 1.14.4, Forge 1.14.4-28.0.13

## v14.6.5
* Addition - Support for JEI.
* Addition - Support for Hywla.
* Fixed - Can't fill creative tank.
* Fixed - You can drain fluid from normal tanks in creative connection.
* Changed version to {mc_version}.{major}.{minor}

## v6.4
* Addition - Added a config option to use easy recipe to craft wood tank.

## v6.3
* Fix - Data pack is loaded as incompatible. [#15](https://github.com/Kotori316/FluidTank/issues/15)

## v6.2
* Fix - May fix loading error. [#15](https://github.com/Kotori316/FluidTank/issues/15)
* Revert - Tanks do something in every ticks.
* Fix - Fixed NPE when you tried to place filled tank on empty tanks.

## v6.1
* Improvement - Removed tick updating interface from tanks. Initial loading uses ChunkEvent.Load now.

## v6.0
* First release for Minecraft 1.14.3
