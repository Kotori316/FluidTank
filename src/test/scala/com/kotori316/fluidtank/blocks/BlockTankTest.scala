package com.kotori316.fluidtank.blocks

import com.kotori316.fluidtank.tiles.Tier
import com.kotori316.fluidtank.{BeforeAllTest, ModObjects}
import org.junit.jupiter.api.{Assertions, DynamicTest, TestFactory}

import scala.jdk.CollectionConverters._

object BlockTankTest extends BeforeAllTest {
  def eachTier(tier: Tier, tanks: List[BlockTank]): Unit = {
    Assertions.assertEquals(1, tanks.size, s"Tier($tier) must have only one tank.")
  }

  @TestFactory
  def eachTierTest(): java.util.Collection[DynamicTest] = {
    ModObjects.blockTanks.groupBy(_.tier)
      .map { case (tier, value) =>
        DynamicTest.dynamicTest(s"$tier", () => eachTier(tier, value))
      }
      .asJavaCollection
  }
}
