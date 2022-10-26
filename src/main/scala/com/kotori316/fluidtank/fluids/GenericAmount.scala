package com.kotori316.fluidtank.fluids

import java.util.Objects

import cats.implicits.{catsSyntaxEq, catsSyntaxGroup, catsSyntaxSemigroup}
import cats.{Hash, Show}
import com.kotori316.fluidtank.eqCompoundNbt
import net.minecraft.nbt.CompoundTag

class GenericAmount[Content]
(val c: Content, val amount: Long, val nbt: Option[CompoundTag])
(implicit GA: GenericAccess[Content], H: Hash[Content]) {

  def setAmount(newAmount: Long): GenericAmount[Content] = new GenericAmount(this.c, newAmount, this.nbt)

  def empty: GenericAmount[Content] = new GenericAmount(GA.empty, 0, Option.empty)

  def write(tag: CompoundTag): CompoundTag = GA.write(this, tag)

  final def nonEmpty: Boolean = !contentIsEmpty && (this.amount > 0)

  final def isEmpty: Boolean = !nonEmpty

  final def contentIsEmpty: Boolean = GA.isEmpty(this.c)

  final def isGaseous: Boolean = GA.isGaseous(this.c)

  final def getLocalizedName: String = String.valueOf(GA.getKey(this.c))

  def +(that: GenericAmount[Content]): GenericAmount[Content] = {
    if (this.isEmpty) that
    else if (that.isEmpty) this
    else setAmount(this.amount |+| that.amount)
  }

  def -(that: GenericAmount[Content]): GenericAmount[Content] = {
    val subtracted = this.amount |-| that.amount
    (GA.isEmpty(this.c), GA.isEmpty(that.c)) match {
      case (true, _) => that.setAmount(subtracted) // Assume this.SelfType == that.SelfType
      case (false, true) => this.setAmount(subtracted)
      case (false, false) if this.c === that.c => this.setAmount(subtracted)
      case _ /*(false, false)*/ => empty
    }
  }

  def *(times: Long): GenericAmount[Content] = times match {
    case 0 => this.setAmount(0)
    case 1 => this
    case _ => this.setAmount(this.amount * times)
  }

  def contentEqual(that: GenericAmount[Content]): Boolean = {
    this.c === that.c && this.nbt === that.nbt
  }

  override def toString: String = GA.getKey(this.c).getPath + "@" + this.amount + "mB" + this.nbt.fold("")(" " + _.toString)

  override def hashCode(): Int = Objects.hash(this.c, this.amount, this.nbt)

  /**
   * This method doesn't use [[cats.Eq]] instance to check content.
   */
  override def equals(obj: Any): Boolean = obj match {
    case that: GenericAmount[_] => this.c == that.c && this.amount === that.amount && this.nbt === that.nbt
    case _ => false
  }
}

object GenericAmount {
  implicit def showGA[A]: Show[GenericAmount[A]] = Show.fromToString

  implicit def hashGA[A]: Hash[GenericAmount[A]] = new Hash[GenericAmount[A]] {
    override def hash(x: GenericAmount[A]): Int = x.hashCode()

    override def eqv(x: GenericAmount[A], y: GenericAmount[A]): Boolean = x.contentEqual(y) && (x.amount === y.amount)
  }
}
