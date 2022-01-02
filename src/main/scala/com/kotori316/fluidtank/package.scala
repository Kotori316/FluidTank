package com.kotori316

import alexiil.mc.lib.attributes.fluid.amount.{FluidAmount => BCAmount}

package object fluidtank {
  final implicit val numericBCAmount: Fractional[BCAmount] = FractionalBCAmount

  private object FractionalBCAmount extends Fractional[BCAmount] {
    override def plus(x: BCAmount, y: BCAmount): BCAmount = x add y

    override def minus(x: BCAmount, y: BCAmount): BCAmount = x sub y

    override def times(x: BCAmount, y: BCAmount): BCAmount = x mul y

    override def div(x: BCAmount, y: BCAmount): BCAmount = x div y

    override def negate(x: BCAmount): BCAmount = x.negate()

    override def fromInt(x: Int): BCAmount = BCAmount.of(x, 1)

    override def parseString(str: String): Option[BCAmount] =
      BCAmount.tryParse(str) match {
        case amount: BCAmount => Option(amount)
        case _: String => Option.empty
      }

    override def toInt(x: BCAmount): Int = x.asInt(1)

    override def toLong(x: BCAmount): Long = x.asLong(1)

    override def toFloat(x: BCAmount): Float = x.asInexactDouble().toFloat

    override def toDouble(x: BCAmount): Double = x.asInexactDouble()

    override def compare(x: BCAmount, y: BCAmount): Int = x compareTo y
  }
}
