package com.kotori316.fluidtank.transport

import com.kotori316.fluidtank.BeforeAllTest
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.{Assertions, Test}

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

private[transport] final class NoBiasTest extends BeforeAllTest {
  final val data = List(2, 6, 8, 5, 3, 8, 5, 4, 32, 5, 6)

  @Test
  def scalaIteratorRemove(): Unit = {
    val a = ArrayBuffer.from(data)
    val b = a.iterator
    val b1 = a.asJava.iterator()

    assertAll(
      () => {
        Assertions.assertEquals(2, b.next())
        a.remove(0)
        Assertions.assertEquals(6, a.iterator.next())
        //        Assertions.assertEquals(6, b.next())
        Assertions.assertEquals(8, b.next()) // The result changed.
      },
      () => {
        Assertions.assertThrows(classOf[UnsupportedOperationException], () => {
          b1.next()
          b1.remove()
        })
      }
    )
  }
}
