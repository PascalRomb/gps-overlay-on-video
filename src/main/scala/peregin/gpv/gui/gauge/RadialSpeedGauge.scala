package peregin.gpv.gui.gauge

import java.awt._
import java.awt.geom.Arc2D
import peregin.gpv.model.{Sonda, MinMax, InputValue}
import peregin.gpv.util.Trigo._
import peregin.gpv.util.UnitConverter


class RadialSpeedGauge() extends GaugePainter {

  lazy val dummy: InputValue = InputValue(Some(0), MinMax.max(100))
  override def defaultInput: InputValue = dummy
  override def sample(sonda: Sonda): Unit = { input = Option(sonda.speed).getOrElse(defaultInput) }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)

    val box = math.min(w, h)
    val strokeWidth = box / 5
    var dia = box - strokeWidth * 1.5

    // draw a thick open arc
    var x = (w - dia) / 2
    var y = (h - dia) / 2
    val start = -45
    val extent = 270
    var arc = new Arc2D.Double(x, y, dia, dia, start, extent, Arc2D.OPEN)
    g.setStroke(new BasicStroke(strokeWidth.toFloat, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f))
    g.setColor(Color.black)
    g.draw(arc)

    // draw the border
    dia = box - strokeWidth / 2
    x = (w - dia) / 2
    y = (h - dia) / 2
    arc = new Arc2D.Double(x, y, dia, dia, start, extent, Arc2D.OPEN)
    g.setColor(Color.white)
    val borderStroke = new BasicStroke(math.max(2, strokeWidth / 10).toFloat)
    g.setStroke(borderStroke)
    g.draw(arc)

    // draw the the ticks and units
    g.setColor(Color.white)
    val r = dia / 2 // the radius of the circle
    val cx = w / 2
    val cy = h / 2
    val ticks = input.boundary.tenths
    val longTickLength = math.max(2, r / 10)
    val smallTickLength = math.max(1, longTickLength / 2)
    val tickStroke = new BasicStroke(math.max(1, strokeWidth / 20).toFloat)
    g.setFont(gaugeFont.deriveFont((longTickLength + 2).toFloat))
    val ticksWithNumber = if (input.boundary.max > 180) 30 else if (input.boundary.max > 100) 20 else 10
    for (t <- 0 to ticks) {
      val angle = -start - t * extent / ticks
      val tickLength = if (t % ticksWithNumber == 0) {
        g.setStroke(borderStroke)

        val text = f"${UnitConverter.speed(ticks - t, units)}%2.0f"
        val tb = g.getFontMetrics.getStringBounds(text, g)
        val tw = tb.getWidth / 2
        val th = tb.getHeight / 2
        val tp = r - longTickLength - tw - 2
        g.drawString(text, polarX(cx, tp, angle) - tw.toInt, polarY(cy, tp, angle) + th.toInt)
        longTickLength
      } else {
        g.setStroke(tickStroke)
        smallTickLength
      }
      g.drawLine(polarX(cx, r, angle), polarY(cy, r, angle), polarX(cx, r - tickLength, angle), polarY(cy, r - tickLength, angle))
    }

    // draw colored sections with color
    dia = box - strokeWidth * 2.5
    x = (w - dia) / 2
    y = (h - dia) / 2
    g.setColor(Color.red)
    arc = new Arc2D.Double(x, y, dia, dia, start, 50, Arc2D.OPEN)
    g.draw(arc)
    g.setColor(Color.yellow)
    arc = new Arc2D.Double(x, y, dia, dia, start + 50, 50, Arc2D.OPEN)
    g.draw(arc)
    g.setColor(Color.green)
    arc = new Arc2D.Double(x, y, dia, dia, start + 100, 100, Arc2D.OPEN)
    g.draw(arc)
    g.setColor(Color.gray)
    arc = new Arc2D.Double(x, y, dia, dia, start + 200, extent - 200, Arc2D.OPEN)
    g.draw(arc)

    // draw pointer
    g.setColor(Color.black)
    var cr = (longTickLength / 2).toInt + 1
    g.fillOval(cx - cr, cy - cr, 2 * cr, 2 * cr)
    g.setColor(Color.yellow)
    cr -= 1
    g.fillOval(cx - cr, cy - cr, 2 * cr, 2 * cr)

    if (input.current.isDefined) {
      val pointerAngle = -extent - start + input.current.get * extent / input.boundary.tenths
      val pointer = r - strokeWidth / 1.2
      val pointerStroke = new BasicStroke(math.max(2, strokeWidth / 5).toFloat, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f)
      g.setStroke(pointerStroke)
      val px = polarX(cx, pointer, pointerAngle)
      val py = polarY(cy, pointer, pointerAngle)
      g.setColor(Color.black)
      g.drawLine(cx + 1, cy + 1, px + 1, py + 1)
      g.setColor(Color.yellow)
      g.drawLine(cx, cy, px, py)

      // draw current speed
      g.setFont(gaugeFont.deriveFont(Font.BOLD, (longTickLength * 4.7).toFloat))
      val text = f"${UnitConverter.speed(input.current.get, units)}%2.1f"
      val tb = g.getFontMetrics.getStringBounds(text, g)
      textWidthShadow(g, text, (w - tb.getWidth) / 2, cy + box / 2 - tb.getHeight * 1.2)
    }

    // draw unit
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (longTickLength * 2).toFloat))
    val utext = UnitConverter.speedUnits(units)
    val utb = g.getFontMetrics.getStringBounds(utext, g)
    textWidthShadow(g, utext, (w - utb.getWidth) / 2, cy + box / 2 - utb.getHeight * 1.5)
  }
}
