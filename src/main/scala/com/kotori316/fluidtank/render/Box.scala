package com.kotori316.fluidtank.render

import com.kotori316.fluidtank.render.Box.Wrapper
import net.minecraft.client.renderer.BufferBuilder
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.math.{AxisAlignedBB, MathHelper}

sealed class Box(val startX: Double, val startY: Double, val startZ: Double,
                 val endX: Double, val endY: Double, val endZ: Double,
                 val sizeX: Double, val sizeY: Double, val sizeZ: Double,
                 val firstSide: Boolean, val endSide: Boolean) {
  val dx = endX - startX
  val dy = endY - startY
  val dz = endZ - startZ
  val lengthSq = dx * dx + dy * dy + dz * dz
  val length = Math.sqrt(lengthSq)
  val offX = sizeX / 2
  val offY = sizeY / 2
  val offZ = sizeZ / 2
  val maxSize = Math.max(Math.max(sizeX, sizeY), sizeZ)

  def render(buffer: BufferBuilder, sprite: TextureAtlasSprite, alpha: Int = 255, red: Int = 255, green: Int = 255, blue: Int = 255)(implicit lightValue: Box.LightValue): Unit = {
    val n1X = dx
    val n1Y = Box.normalY(dx, dy, dz)
    val n1Z = dz
    val n1Size = Math.sqrt(n1X * n1X + n1Y * n1Y + n1Z * n1Z)
    val n2X = dy * n1Z - dz * n1Y
    val n2Z = dx * n1Y - dy * n1X
    val n2Size = Math.sqrt(n2X * n2X + n2Z * n2Z)
    renderInternal(buffer, sprite, n1X / n1Size / 2, n1Y / n1Size / 2, n1Z / n1Size / 2, n2X / n2Size / 2, n2Z / n2Size / 2, lightValue,
      alpha, red, green, blue)
  }

  protected final def renderInternal(r: BufferBuilder, sprite: TextureAtlasSprite,
                                     n1X: Double, n1Y: Double, n1Z: Double,
                                     n2X: Double, n2Z: Double, lv: Box.LightValue,
                                     alpha: Int, red: Int, green: Int, blue: Int): Unit = {
    val buffer: Wrapper = new Wrapper(r)
    val eX = dx / length * sizeX
    val eY = dy / length * sizeY
    val eZ = dz / length * sizeZ

    val e1X = startX + n1X * sizeX + n2X * sizeX
    val e1Y = startY + n1Y * sizeY
    val e1Z = startZ + n1Z * sizeZ + n2Z * sizeZ

    val e2X = startX - n1X * sizeX + n2X * sizeX
    val e2Y = startY - n1Y * sizeY
    val e2Z = startZ - n1Z * sizeZ + n2Z * sizeZ

    val e3X = startX - n1X * sizeX - n2X * sizeX
    val e3Y = e2Y
    val e3Z = startZ - n1Z * sizeZ - n2Z * sizeZ

    val e4X = startX + n1X * sizeX - n2X * sizeX
    val e4Y = e1Y
    val e4Z = startZ + n1Z * sizeZ - n2Z * sizeZ

    if (firstSide) {
      buffer.pos(e1X, e1Y, e1Z).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e2X, e2Y, e2Z).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e3X, e3Y, e3Z).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e4X, e4Y, e4Z).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()
    }
    val l = Math.sqrt(dx / sizeX * dx / sizeX + dy / sizeY * dy / sizeY + dz / sizeZ * dz / sizeZ)
    val lengthFloor = MathHelper.floor(l)
    var i1 = 0
    while (i1 <= lengthFloor) {
      val i2 = if (i1 == lengthFloor) l else i1 + 1
      buffer.pos(e1X + eX * i2, e1Y + eY * i2, e1Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e1X + eX * i1, e1Y + eY * i1, e1Z + eZ * i1).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e2X + eX * i1, e2Y + eY * i1, e2Z + eZ * i1).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e2X + eX * i2, e2Y + eY * i2, e2Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()

      buffer.pos(e2X + eX * i2, e2Y + eY * i2, e2Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e2X + eX * i1, e2Y + eY * i1, e2Z + eZ * i1).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e3X + eX * i1, e3Y + eY * i1, e3Z + eZ * i1).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e3X + eX * i2, e3Y + eY * i2, e3Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()

      buffer.pos(e3X + eX * i2, e3Y + eY * i2, e3Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e3X + eX * i1, e3Y + eY * i1, e3Z + eZ * i1).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e4X + eX * i1, e4Y + eY * i1, e4Z + eZ * i1).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e4X + eX * i2, e4Y + eY * i2, e4Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()

      buffer.pos(e4X + eX * i2, e4Y + eY * i2, e4Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e4X + eX * i1, e4Y + eY * i1, e4Z + eZ * i1).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e1X + eX * i1, e1Y + eY * i1, e1Z + eZ * i1).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e1X + eX * i2, e1Y + eY * i2, e1Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()

      i1 += 1
    }
    if (endSide) {
      buffer.pos(e1X + dx, e1Y + dy, e1Z + dz).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e2X + dx, e2Y + dy, e2Z + dz).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMinV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e3X + dx, e3Y + dy, e3Z + dz).color(red, green, blue, alpha).tex(sprite.getMaxU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(e4X + dx, e4Y + dy, e4Z + dz).color(red, green, blue, alpha).tex(sprite.getMinU, sprite.getMaxV).lightmap(lv.l1, lv.l2).endVertex()
    }
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case o: Box => startX == o.startX && startY == o.startY && startZ == o.startZ &&
        endX == o.endX && endY == o.endY && endZ == o.endZ &&
        sizeX == o.sizeX && sizeY == o.sizeY && sizeZ == o.sizeZ && firstSide == o.firstSide && endSide == o.endSide
      case _ => false
    }
  }

  override def hashCode(): Int = {
    (startX + startY + startZ + endX + endY + endZ + sizeX + sizeY + sizeZ + (if (firstSide) 1 else 0) + (if (endSide) 1 else 0)).toInt
  }
}

private class BoxX(startX: Double,
                   endX: Double, y: Double, z: Double,
                   sizeX: Double, sizeY: Double, sizeZ: Double, firstSide: Boolean, endSide: Boolean)
  extends Box(startX, y, z, endX, y, z, sizeX, sizeY, sizeZ, firstSide, endSide) {
  override val length: Double = dx

  override def render(r: BufferBuilder, sprite: TextureAtlasSprite, alpha: Int = 255, red: Int = 255, green: Int = 255, blue: Int = 255)(implicit lv: Box.LightValue): Unit = {
    val buffer: Wrapper = new Wrapper(r)
    val count = MathHelper.floor(length / sizeX)
    val minU = sprite.getMinU
    val minV = sprite.getMinV
    val maXV = sprite.getInterpolatedV(sizeX / maxSize * 16)
    val maYU = sprite.getInterpolatedU(sizeY / maxSize * 16)
    val maZU = sprite.getInterpolatedU(sizeZ / maxSize * 16)
    val maZV = sprite.getInterpolatedV(sizeZ / maxSize * 16)
    if (firstSide) {
      //West
      buffer.pos(startX, y + offY, z - offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX, y - offY, z - offZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX, y - offY, z + offZ).color(red, green, blue, alpha).tex(maYU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX, y + offY, z + offZ).color(red, green, blue, alpha).tex(minU, maZV).lightmap(lv.l1, lv.l2).endVertex()
    }
    var i1 = 0
    while (i1 <= count) {
      val i2 = if (i1 == count) length / sizeX else i1 + 1d
      //North
      buffer.pos(startX + sizeX * i2, y + offY, z - offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i2, y - offY, z - offZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i1, y - offY, z - offZ).color(red, green, blue, alpha).tex(maYU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i1, y + offY, z - offZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      //South
      buffer.pos(startX + sizeX * i1, y + offY, z + offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i1, y - offY, z + offZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i2, y - offY, z + offZ).color(red, green, blue, alpha).tex(maYU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i2, y + offY, z + offZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      //Top
      buffer.pos(startX + sizeX * i1, y + offY, z - offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i1, y + offY, z + offZ).color(red, green, blue, alpha).tex(maZU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i2, y + offY, z + offZ).color(red, green, blue, alpha).tex(maZU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i2, y + offY, z - offZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      //Bottom
      buffer.pos(startX + sizeX * i2, y - offY, z - offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i2, y - offY, z + offZ).color(red, green, blue, alpha).tex(maZU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i1, y - offY, z + offZ).color(red, green, blue, alpha).tex(maZU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(startX + sizeX * i1, y - offY, z - offZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()

      i1 += 1
    }
    if (endSide) {
      //East
      buffer.pos(endX, y + offY, z + offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(endX, y - offY, z + offZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(endX, y - offY, z - offZ).color(red, green, blue, alpha).tex(maYU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(endX, y + offY, z - offZ).color(red, green, blue, alpha).tex(minU, maZV).lightmap(lv.l1, lv.l2).endVertex()
    }
  }
}

private class BoxY(startY: Double,
                   endY: Double, x: Double, z: Double,
                   sizeX: Double, sizeY: Double, sizeZ: Double, firstSide: Boolean, endSide: Boolean)
  extends Box(x, startY, z, x, endY, z, sizeX, sizeY, sizeZ, firstSide, endSide) {
  override val length = dy

  override def render(r: BufferBuilder, sprite: TextureAtlasSprite, alpha: Int = 255, red: Int = 255, green: Int = 255, blue: Int = 255)(implicit lv: Box.LightValue): Unit = {
    val buffer: Wrapper = new Wrapper(r)
    val count = MathHelper.floor(length / sizeY)
    val minU = sprite.getMinU
    val minV = sprite.getMinV

    val maYU = sprite.getInterpolatedU(sizeY / maxSize * 16)
    val maXV = sprite.getInterpolatedV(sizeX / maxSize * 16)
    val maZU = sprite.getInterpolatedU(sizeZ / maxSize * 16)
    val maZV = sprite.getInterpolatedV(sizeZ / maxSize * 16)
    if (firstSide) {
      //Bottom
      buffer.pos(x + offX, startY, z - offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, startY, z + offZ).color(red, green, blue, alpha).tex(maZU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, startY, z + offZ).color(red, green, blue, alpha).tex(maZU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, startY, z - offZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()
    }
    var i1 = 0
    while (i1 <= count) {
      val i2 = if (i1 == count) length / sizeY else i1 + 1d
      //West
      buffer.pos(x - offX, startY + sizeY * i2, z - offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, startY + sizeY * i1, z - offZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, startY + sizeY * i1, z + offZ).color(red, green, blue, alpha).tex(maYU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, startY + sizeY * i2, z + offZ).color(red, green, blue, alpha).tex(minU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      //East
      buffer.pos(x + offX, startY + sizeY * i2, z + offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, startY + sizeY * i1, z + offZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, startY + sizeY * i1, z - offZ).color(red, green, blue, alpha).tex(maYU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, startY + sizeY * i2, z - offZ).color(red, green, blue, alpha).tex(minU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      //North
      buffer.pos(x + offX, startY + sizeY * i2, z - offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, startY + sizeY * i1, z - offZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, startY + sizeY * i1, z - offZ).color(red, green, blue, alpha).tex(maYU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, startY + sizeY * i2, z - offZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      //South
      buffer.pos(x - offX, startY + sizeY * i2, z + offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, startY + sizeY * i1, z + offZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, startY + sizeY * i1, z + offZ).color(red, green, blue, alpha).tex(maYU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, startY + sizeY * i2, z + offZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()

      i1 += 1
    }
    if (endSide) {
      //Top
      buffer.pos(x - offX, endY, z - offZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, endY, z + offZ).color(red, green, blue, alpha).tex(maZU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, endY, z + offZ).color(red, green, blue, alpha).tex(maZU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, endY, z - offZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()
    }
  }
}

private class BoxZ(startZ: Double,
                   endZ: Double, x: Double, y: Double,
                   sizeX: Double, sizeY: Double, sizeZ: Double, firstSide: Boolean, endSide: Boolean)
  extends Box(x, y, startZ, x, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide) {
  override val length = dz

  override def render(r: BufferBuilder, sprite: TextureAtlasSprite, alpha: Int = 255, red: Int = 255, green: Int = 255, blue: Int = 255)(implicit lv: Box.LightValue): Unit = {
    val buffer: Wrapper = new Wrapper(r)
    val count = MathHelper.floor(length / sizeZ)
    val minU = sprite.getMinU
    val minV = sprite.getMinV
    val maXU = sprite.getInterpolatedU(sizeX / maxSize * 16)
    val maXV = sprite.getInterpolatedV(sizeX / maxSize * 16)
    val maYU = sprite.getInterpolatedU(sizeY / maxSize * 16)
    val maZV = sprite.getInterpolatedV(sizeZ / maxSize * 16)
    if (firstSide) {
      //North
      buffer.pos(x + offX, y + offY, startZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y - offY, startZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y - offY, startZ).color(red, green, blue, alpha).tex(maYU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y + offY, startZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()
    }
    var i1 = 0
    while (i1 <= count) {
      val i2 = if (i1 == count) length / sizeZ else i1 + 1d
      //West
      buffer.pos(x - offX, y + offY, startZ + sizeZ * i1).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y - offY, startZ + sizeZ * i1).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y - offY, startZ + sizeZ * i2).color(red, green, blue, alpha).tex(maYU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y + offY, startZ + sizeZ * i2).color(red, green, blue, alpha).tex(minU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      //East
      buffer.pos(x + offX, y + offY, startZ + sizeZ * i2).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y - offY, startZ + sizeZ * i2).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y - offY, startZ + sizeZ * i1).color(red, green, blue, alpha).tex(maYU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y + offY, startZ + sizeZ * i1).color(red, green, blue, alpha).tex(minU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      //Top
      buffer.pos(x - offX, y + offY, startZ + sizeZ * i1).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y + offY, startZ + sizeZ * i2).color(red, green, blue, alpha).tex(maXU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y + offY, startZ + sizeZ * i2).color(red, green, blue, alpha).tex(maXU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y + offY, startZ + sizeZ * i1).color(red, green, blue, alpha).tex(minU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      //Bottom
      buffer.pos(x + offX, y - offY, startZ + sizeZ * i1).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y - offY, startZ + sizeZ * i2).color(red, green, blue, alpha).tex(maXU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y - offY, startZ + sizeZ * i2).color(red, green, blue, alpha).tex(maXU, maZV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y - offY, startZ + sizeZ * i1).color(red, green, blue, alpha).tex(minU, maZV).lightmap(lv.l1, lv.l2).endVertex()

      i1 += 1
    }
    if (endSide) {
      //South
      buffer.pos(x - offX, y + offY, endZ).color(red, green, blue, alpha).tex(minU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x - offX, y - offY, endZ).color(red, green, blue, alpha).tex(maYU, minV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y - offY, endZ).color(red, green, blue, alpha).tex(maYU, maXV).lightmap(lv.l1, lv.l2).endVertex()
      buffer.pos(x + offX, y + offY, endZ).color(red, green, blue, alpha).tex(minU, maXV).lightmap(lv.l1, lv.l2).endVertex()
    }
  }
}

private class BoxXZ(startX: Double, startZ: Double, endX: Double, y: Double, endZ: Double,
                    sizeX: Double, sizeY: Double, sizeZ: Double, firstSide: Boolean, endSide: Boolean)
  extends Box(startX, y, startZ, endX, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide) {
  override val length = Math.sqrt(dx * dx + dz * dz)

  override def render(buffer: BufferBuilder, sprite: TextureAtlasSprite, alpha: Int = 255, red: Int = 255, green: Int = 255, blue: Int = 255)(implicit lv: Box.LightValue): Unit = {
    val n2Size = length
    renderInternal(buffer, sprite, 0, 0.5, 0, -dz / n2Size / 2, dx / n2Size / 2, lv, alpha, red, green, blue)
  }
}

object Box {
  def apply(axisAlignedBB: AxisAlignedBB, sizeX: Double, sizeY: Double, sizeZ: Double, firstSide: Boolean, endSide: Boolean): Box =
    apply(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ,
      axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ,
      sizeX, sizeY, sizeZ, firstSide, endSide)

  def apply(startX: Double, startY: Double, startZ: Double,
            endX: Double, endY: Double, endZ: Double,
            sizeX: Double, sizeY: Double, sizeZ: Double, firstSide: Boolean, endSide: Boolean): Box = {
    if (startY == endY) {
      if (startX == endX) {
        new BoxZ(startZ, endZ, endX, endY, sizeX, sizeY, sizeZ, firstSide, endSide)
      } else if (startZ == endZ) {
        new BoxX(startX, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide)
      } else {
        new BoxXZ(startX, startZ, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide)
      }
    } else if (startZ == endZ && startX == endX) {
      new BoxY(startY, endY, endX, endZ, sizeX, sizeY, sizeZ, firstSide, endSide)
    } else {
      new Box(startX, startY, startZ, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide)
    }
  }

  protected def normalY(x: Double, y: Double, z: Double): Double = {
    -(x * x + z * z) / y
  }

  class LightValue(brightness: Int) {
    final val l1 = brightness >> 16 & 0xFFFF
    final val l2 = brightness >> 0 & 0xFFFF
  }

  implicit val lightValue: LightValue = new LightValue(0x00f000f0)

  class Wrapper(val buffer: BufferBuilder) extends AnyVal {
    def pos(x: Double, y: Double, z: Double): Wrapper = {
      buffer.func_225582_a_(x, y, z)
      this
    }

    def color(red: Int, green: Int, blue: Int, alpha: Int): Wrapper = {
      buffer.func_225586_a_(red, green, blue, alpha)
      this
    }

    def tex(u: Float, v: Float): Wrapper = {
      buffer.func_225583_a_(u, v)
      this
    }

    //noinspection SpellCheckingInspection
    def lightmap(l1: Int, l2: Int): Wrapper = {
      buffer.func_225587_b_(l1, l2)
      this
    }

    def endVertex(): Unit = {
      buffer.endVertex()
    }
  }

}
