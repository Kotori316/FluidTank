# FluidTank

For Minecraft 1.18.1 (1.18 branch)

[![](http://cf.way2muchnoise.eu/versions/largefluidtank.svg)][CurseForge]
[![](http://cf.way2muchnoise.eu/full_largefluidtank_downloads.svg)][CurseForge]

[![](https://github.com/Kotori316/FluidTank/workflows/Java%20CI/badge.svg)](https://github.com/Kotori316/FluidTank/actions)

[CurseForge]: https://www.curseforge.com/minecraft/mc-mods/largefluidtank

LICENSE: MIT License  
Copyright (c) 2019-2022 Kotori316

Add large tanks that can have up to 1048576 buckets of fluid.

There are 7 tiers in this mod, Wood, Stone, Iron, Gold, Diamond, Emerald and Star. The capacity of wood tank is 4000 mB
but star one can have 1048576000 mB. These tanks can be connected vertically to get more capacity.

All tanks have Fluid Capability to transfer fluid, and their items also have Fluid Capability for items.

See [wiki page](https://github.com/Kotori316/FluidTank/wiki) to get more information.

[Minecraft CurseForge][CurseForge] - https://www.curseforge.com/minecraft/mc-mods/largefluidtank

## APIs

* **REQUIRED** [Forge](https://github.com/MinecraftForge/MinecraftForge)
  * For Minecraft 1.18.1, newer than 1.18.1-39.0.0

* **REQUIRED** [Scala](https://github.com/scala/scala)
  * Version 2.13.7
  * Licenced under the Apache License 2.0

* **REQUIRED** [Cats](https://github.com/typelevel/cats)
  * Version 2.6.2
  * Licensed under the MIT License
  * Using modified version. [Source](https://github.com/Kotori316/cats)

* ~[Just Enough Items (JEI)](https://github.com/mezz/JustEnoughItems)~
  * Licensed under the MIT License
  * Not used since 1.14.4

* ~[Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2)~
  * Licensed under [LGPLv3](https://github.com/AppliedEnergistics/Applied-Energistics-2#license)

* ~[Hwyla](https://github.com/TehNut/HWYLA/tree/1.16_forge)~
  * Licensed under CC BY-NC-SA 4.0

* [Jade](https://github.com/Snownee/Jade)
  * Licensed under [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)

* [The One Prove](https://github.com/McJtyMods/TheOneProbe/tree/1.18)
  * Licensed under the MIT License Copyright © 2016 McJty

* ~[LookAtThat](https://github.com/Geek202/LookAtThat)~
  * Licensed under the MIT License Copyright (c) 2019 Tom_The_Geek

## Maven repo

See [here](https://dev.azure.com/Kotori316/minecraft/_packaging?_a=package&feed=mods%40Local&package=com.kotori316%3Afluidtank&protocolType=maven&view=versions)
to get other versions.

```groovy
repositories {
    maven {
        name "Kotori316 Azure Maven"
        url = uri("https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1")
    }
}
dependencies {
    // https://dev.azure.com/Kotori316/minecraft/_packaging?_a=package&feed=mods%40Local&package=com.kotori316%3Afluidtank&protocolType=maven&view=versions
    implementation(fg.deobf("com.kotori316:fluidtank:VERSION"))
}
```
