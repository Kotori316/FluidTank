# FluidTank

For Minecraft 1.16.5 (1.16 branch)

[![](http://cf.way2muchnoise.eu/versions/largefluidtank.svg)](https://www.curseforge.com/minecraft/mc-mods/largefluidtank)
[![](http://cf.way2muchnoise.eu/full_largefluidtank_downloads.svg)](https://www.curseforge.com/minecraft/mc-mods/largefluidtank)

[![](https://github.com/Kotori316/FluidTank/workflows/Java%20CI/badge.svg)](https://github.com/Kotori316/FluidTank/actions)

LICENSE: MIT License  
Copyright (c) 2019-2021 Kotori316

Add large tanks that can have up to 1048576 buckets of fluid.

There are 7 tiers in this mod, Wood, Stone, Iron, Gold, Diamond, Emerald and Star. The capacity of wood tank is 4000 mB
but star one can have 1048576000 mB.

All tanks have Fluid Capability to transfer fluid, and their items also have Fluid Capability for items.

See [wiki page](https://github.com/Kotori316/FluidTank/wiki) to get more information.

Minecraft CurseForge - https://www.curseforge.com/minecraft/mc-mods/largefluidtank

## APIs

* **REQUIRED** [Forge](https://github.com/MinecraftForge/MinecraftForge)
    * For Minecraft 1.16.5, newer than 1.16.5-36.0.1

* **REQUIRED** [Scala](https://github.com/scala/scala)
    * Version 2.13.4
    * Licenced under the Apache License 2.0

* **REQUIRED** [Cats](https://github.com/typelevel/cats)
    * Version 2.3.0
    * Licensed under the MIT License

* ~[Just Enough Items (JEI)](https://github.com/mezz/JustEnoughItems)~
    * Licensed under the MIT License
    * Not used since 1.14.4

* [Hwyla](https://github.com/TehNut/HWYLA/tree/1.16_forge)
    * Licensed under CC BY-NC-SA 4.0

* [The One Prove](https://github.com/McJtyMods/TheOneProbe/tree/1.16)
    * Licensed under the MIT License Copyright © 2016 McJty

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
