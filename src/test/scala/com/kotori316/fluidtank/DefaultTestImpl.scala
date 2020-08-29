package com.kotori316.fluidtank

import org.junit.jupiter.api.Test

trait DefaultTestImpl extends DefaultTestInterface {
  override def nonDefault() = 150
}

class DefaultTest extends DefaultTestImpl {
  @Test
  def test(): Unit = {
    assert(this.nonDefault() == 150)
  }

  @Test
  def test2(): Unit = {
    assert(this.getDefault == 100)
  }
}
