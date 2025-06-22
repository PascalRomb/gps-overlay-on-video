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

  private val currentSpeed = 43
  private val maxSpeed = 100 //already in input from sonda??

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    if( w != h ) {
      throw new IllegalArgumentException("Width and Height must be equals!")
    }

    val drawTick = true
    // inner arc, min-max arc
    val offsetMultiplier = 1.5
    val diameter = w * offsetMultiplier
    val radius = diameter / 2

    val xyOffset = (w - radius) / 2
    val arcStartX = (-radius) + xyOffset
    val arcStartY = 0 + xyOffset

    if(drawTick) {
      g.setColor(Color.red)
      g.fillOval(xyOffset.toInt, xyOffset.toInt, 20, 20)
      g.fillOval((w-xyOffset).toInt, (w-xyOffset).toInt, 20, 20)
    }

    g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g.setColor(new Color(200, 200, 200, 150))
    val baseArc = new Arc2D.Double(arcStartX, arcStartY, diameter, diameter, 90, -90, Arc2D.PIE)
    g.draw(baseArc)

    //outer, speed arc
    val offsetMultiplier2 = 1.7
    val diameter2 = w * offsetMultiplier2
    val radius2 = diameter2 / 2

    val xyOffset2 = (w - radius2) / 2
    val arcStartX2 = (-radius2) + xyOffset2
    val arcStartY2 = 0 + xyOffset2

    g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g.setColor(new Color(100, 100, 255))
    val startAngle = 90
    val angle = startAngle * currentSpeed / maxSpeed
    val baseArc2 = new Arc2D.Double(arcStartX2, arcStartY2, diameter2, diameter2, startAngle, -angle, Arc2D.PIE)
    g.draw(baseArc2)


    //    // min max label //FIXME
    //    g.setFont(new Font("SansSerif", Font.PLAIN, 12))
    //    g.drawString("0", centerX - radius + 5, centerY)
    //    g.drawString(Integer.toString(maxSpeed), centerX + radius - 20, centerY)
    //
    //    val offset = 20
    //    val outerArcX = arcX - offset
    //    val outerArcY = arcY - offset
    //    val outerArcSize = arcSize + 2 * offset
    //    val outerArcSizeWidth = arcSizeWidth + 2 * offset
    //
    //    g.setColor(new Color(100, 100, 255)) // o altro colore per il bordo
    //    g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    //    val angle = 105.0 * currentSpeed / maxSpeed
    //    val outerArc = new Arc2D.Double(outerArcX, outerArcY, outerArcSize, outerArcSizeWidth, 105, -angle, Arc2D.OPEN)
    //    g.draw(outerArc)


    // //FIXME
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
}
