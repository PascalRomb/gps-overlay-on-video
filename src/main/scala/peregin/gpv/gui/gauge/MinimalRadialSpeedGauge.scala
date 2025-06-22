package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}
import peregin.gpv.util.Trigo._
import peregin.gpv.util.UnitConverter

import java.awt._
import java.awt.geom.Arc2D
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Arc2D


class MinimalRadialSpeedGauge() extends GaugePainter {

  lazy val dummy: InputValue = InputValue(Some(0), MinMax.max(100))
  override def defaultInput: InputValue = dummy
  override def sample(sonda: Sonda): Unit = { input = Option(sonda.speed).getOrElse(defaultInput) }

  //TODO make fonts and stroke width responsive?
  //TODO refactor code

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    if( w != h ) {
      throw new IllegalArgumentException("Width and Height must be equals!")
    }
    val size = w;

    // inner arc, min-max arc
    val offsetMultiplier = 1.5
    val diameter = size * offsetMultiplier
    val radius = diameter / 2

    val xyOffset = (size - radius) / 2
    val arcStartX = (-radius) + xyOffset
    val arcStartY = 0 + xyOffset

    drawArc(g, arcStartX, arcStartY, diameter, new Color(200, 200, 200, 150), currentMaxRatio = 1)
    // min max label
    g.setFont(new Font("SansSerif", Font.BOLD, 32))
    val fm = g.getFontMetrics
    g.setColor(Color.white)

    g.drawString("0", xyOffset.toInt, (xyOffset + fm.getHeight).toInt)
    val maxSpeedStringed = input.boundary.max.toInt.toString
    val sw = fm.stringWidth(maxSpeedStringed)
    g.drawString(maxSpeedStringed, (size-xyOffset-sw - 10).toInt, (size-xyOffset).toInt)

    //outer, speed arc
    val outerOffsetMultiplier = 1.65
    val outerDiameter = size * outerOffsetMultiplier
    val outerXYOffset = (outerDiameter-diameter)/2

    drawSpeedArc(g, arcStartX-outerXYOffset, arcStartY-outerXYOffset,xyOffset, size, outerDiameter)
  }

  def drawSpeedArc(g: Graphics2D, arcStartX: Double, arcStartY: Double, xyOffset:Double, size: Double, diameter: Double): Unit = {
    val currentSpeed = input.current.getOrElse(0.0)
    val currentSpeedRatio = currentSpeed / input.boundary.max

    drawArc(g, arcStartX, arcStartY, diameter, new Color(100, 100, 255), currentMaxRatio = currentSpeedRatio)

    //speed text
    g.setFont(new Font("SansSerif", Font.BOLD, 72))
    val speedStr = Integer.toString(currentSpeed.toInt)

    val fm = g.getFontMetrics
    val speedStringWidth = fm.stringWidth(speedStr)
    val speedStringHeight = fm.getHeight

    g.setColor(Color.WHITE)
    g.drawString(speedStr,  xyOffset.toInt, (size - xyOffset).toInt)

    // unit
    g.setFont(new Font("SansSerif", Font.BOLD, 24))
    val unit = "km/h"
    g.drawString(unit, (xyOffset + speedStringWidth).toInt, (size - speedStringHeight - 10).toInt)
  }


  def drawArc(g: Graphics2D, arcStartX: Double, arcStartY: Double, diameter: Double, color: Color, currentMaxRatio: Double): Unit = {
    val startAngle = 90
    val endAngle = -90 * currentMaxRatio

    g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g.setColor(color)
    val arcType = if (debug) Arc2D.PIE else Arc2D.OPEN
    val baseArc = new Arc2D.Double(arcStartX, arcStartY, diameter, diameter, startAngle, endAngle,arcType)
    g.draw(baseArc)
  }
}
