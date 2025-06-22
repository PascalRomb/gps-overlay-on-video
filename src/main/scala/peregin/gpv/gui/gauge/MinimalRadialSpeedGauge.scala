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

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    if( w != h ) {
      throw new IllegalArgumentException("Width and Height must be equals!")
    }
    val size = w;

    // inner arc, min-max arc
    drawMinMaxArc(g, size)

    //outer, speed arc
    drawSpeedArc(g, size)

  }

  def drawMinMaxArc(g: Graphics2D, size: Double): Unit = {
    //val minValue = input.boundary.min
    //val maxValue = input.boundary.max
    val calculatedOffset = drawArc(g, size, 1.5, new Color(200, 200, 200, 150), currentMaxRatio = 1)

    g.setColor(Color.red)
    g.fillOval(calculatedOffset.toInt, calculatedOffset.toInt, 20, 20)
    g.fillOval((size-calculatedOffset).toInt, (size-calculatedOffset).toInt, 20, 20)
    //TODO draw min max value

    //    // min max label //FIXME
    //    g.setFont(new Font("SansSerif", Font.PLAIN, 12))
    //    g.drawString("0", centerX - radius + 5, centerY)
    //    g.drawString(Integer.toString(maxSpeed), centerX + radius - 20, centerY)
  }

  def drawSpeedArc(g: Graphics2D, size: Double): Unit = {
    val currentSpeed = input.current.map(el => el/ input.boundary.max).getOrElse(0.0)
    val calculatedOffset = drawArc(g, size, 1.7, new Color(100, 100, 255), currentMaxRatio = currentSpeed)

    g.setColor(Color.red)
    g.fillOval((calculatedOffset).toInt, (size-calculatedOffset).toInt, 20, 20)

    // TODO Add this
    //    // vel text
    //    g.setFont(new Font("SansSerif", Font.BOLD, 32))
    //    val speedStr = Integer.toString(currentSpeed)
    //    var fm = g.getFontMetrics
    //    var sw = fm.stringWidth(speedStr)
    //    g.setColor(Color.WHITE)
    //    g.drawString(speedStr, centerX - sw / 2, centerY - 10)

    //    // km/h
    //    g.setFont(new Font("SansSerif", Font.PLAIN, 14))
    //    val unit = "km/h"
    //    fm = g.getFontMetrics
    //    sw = fm.stringWidth(unit)
    //    g.drawString(unit, centerX - sw / 2, centerY + 10)
    //
    //
  }




  def drawArc(g: Graphics2D, size: Double, offsetMultiplier: Double, color: Color, currentMaxRatio: Double): Double = {
    val diameter = size * offsetMultiplier
    val radius = diameter / 2

    val xyOffset = (size - radius) / 2
    val arcStartX = (-radius) + xyOffset
    val arcStartY = 0 + xyOffset

    val startAngle = 90
    val endAngle = -90 * currentMaxRatio

    g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g.setColor(color)
    val arcType = if (debug) Arc2D.PIE else Arc2D.OPEN
    val baseArc = new Arc2D.Double(arcStartX, arcStartY, diameter, diameter, startAngle, endAngle,arcType)
    g.draw(baseArc)

    xyOffset
  }
}
