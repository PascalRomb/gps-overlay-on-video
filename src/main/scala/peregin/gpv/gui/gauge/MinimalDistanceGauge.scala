package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

import java.awt._


class MinimalDistanceGauge extends GaugePainter {
  val padding = 75

  lazy val dummy: InputValue = InputValue(Some(0), MinMax.max(80))

  override def defaultInput: InputValue = dummy


  override def sample(sonda: Sonda): Unit = {
    input = Option(sonda.distance).getOrElse(defaultInput)
  }

  override def paint(g: Graphics2D, devHeight: Int, w: Int, h: Int): Unit = {
    super.paint(g, devHeight, w, h)
    g.setFont(gaugeFont.deriveFont(Font.BOLD, (h * 0.2).toFloat))

    //distance
    val y = (0.5*h ).toInt
    val strokeWidth = (0.1*h).toFloat
    drawLine(g, y, strokeWidth,new Color(200, 200, 200, 150), w-padding)
    val currentDistance = truncate(input.current.getOrElse(0.0))
    val currentDistanceX = (padding + ((w-padding-padding) * (currentDistance/input.boundary.max))).toInt
    drawLine(g, y, strokeWidth, Color.yellow, currentDistanceX)

    //min max current
    val fm = g.getFontMetrics
    val labelPadding = fm.getHeight + (0.1*h).toInt
    g.setColor(Color.white)
    val zeroKmString = "0.0 Km"
    val zeroKmStringWidth = fm.stringWidth(zeroKmString) / 2
    g.drawString(zeroKmString, padding - zeroKmStringWidth, y - labelPadding)
    val maxDistanceString = f"${input.boundary.max}%.1f Km"
    val offsetString = fm.stringWidth(maxDistanceString) / 2
    g.drawString(maxDistanceString, w-padding-offsetString, y - labelPadding)
    if(currentDistance > 0.0) {
      g.drawString(s"${currentDistance} Km", currentDistanceX, y + labelPadding)
    }



  }

  def drawLine(g: Graphics2D, y: Int,strokeWidth: Float, color: Color, distance: Double): Unit = {
    g.setColor(color)
    g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g.drawLine(padding, y, distance.toInt, y)
  }

  def truncate(d: Double): Double = BigDecimal(d).setScale(1, BigDecimal.RoundingMode.DOWN).toDouble

}
