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
  private val maxSpeed = 100

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

//    val box = math.min(w, h)
//    val strokeWidth = box / 5
//    var dia = box - strokeWidth * 1.5
//
//    // draw a thick open arc
//    var x = (w - dia) / 2
//    var y = (h - dia) / 2
//    val start = 135 // Inizia in basso a sinistra
//    val extent = -200
//    var arc = new Arc2D.Double(x, y, dia, dia, start, extent, Arc2D.OPEN)
//    g.setStroke(new BasicStroke(strokeWidth.toFloat, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f))
//    g.setColor(Color.black)
//    g.draw(arc)

    val size = Math.min(w, h)
    val centerX = w / 2
    val centerY = h / 2 + 30
    val radius = size / 2 - 20
    val arcSize = radius * 3
    val arcSizeWidth = arcSize - 20
    val arcX = centerX - radius
    val arcY = centerY - radius

    // inner arc
    g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g.setColor(new Color(200, 200, 200, 150))
    val baseArc = new Arc2D.Double(arcX, arcY, arcSize, arcSizeWidth, 105, -105, Arc2D.OPEN)
    g.draw(baseArc)

    // min max label //FIXME
    g.setFont(new Font("SansSerif", Font.PLAIN, 12))
    g.drawString("0", centerX - radius + 5, centerY)
    g.drawString(Integer.toString(maxSpeed), centerX + radius - 20, centerY)

    val offset = 20
    val outerArcX = arcX - offset
    val outerArcY = arcY - offset
    val outerArcSize = arcSize + 2 * offset
    val outerArcSizeWidth = arcSizeWidth + 2 * offset

    g.setColor(new Color(100, 100, 255)) // o altro colore per il bordo
    g.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    val angle = 105.0 * currentSpeed / maxSpeed
    val outerArc = new Arc2D.Double(outerArcX, outerArcY, outerArcSize, outerArcSizeWidth, 105, -angle, Arc2D.OPEN)
    g.draw(outerArc)


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
