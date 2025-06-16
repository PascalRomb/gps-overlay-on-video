package peregin.gpv.gui.dashboard

import peregin.gpv.gui.gauge.{CadenceGauge, ElevationChart, GaugePainter, GradeGauge, RadialAzimuthGauge, RadialSpeedGauge, SvgHeartRateGauge, SvgPowerGauge, TemperatureGauge}
import peregin.gpv.model.{InputValue, MinMax, Sonda}

import scala.swing.Graphics2D

abstract class Dashboard extends Cloneable {

  override def clone(): Dashboard = super.clone().asInstanceOf[Dashboard]

  def gauges(): Seq[GaugePainter]

  def paintDashboard(g: Graphics2D, imageWidth: Int, imageHeight: Int, gaugeSize: Int, sonda: Sonda): Unit

  def getName(): String
}

trait CyclingDashboard extends Dashboard {

  private val speedGauge = new RadialSpeedGauge {}
  private val cadenceGauge = new CadenceGauge {}
  private val elevationChart = new ElevationChart {}
  private val heartRateGauge = new SvgHeartRateGauge {}
  private val powerGauge = new SvgPowerGauge {}

  override def gauges(): Seq[GaugePainter] = Seq(speedGauge, cadenceGauge, elevationChart, heartRateGauge, powerGauge)

  override def getName(): String = "Cycling"

  override def paintDashboard(g: Graphics2D, imageWidth: Int, imageHeight: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    // paint elevation to the right
    g.translate(0, imageHeight - gaugeSize)
    val stashBottom = g.getTransform
    g.translate(imageWidth - gaugeSize * 3, gaugeSize / 4)
    elevationChart.paint(g, imageHeight, gaugeSize * 3, gaugeSize * 3 / 4, sonda)
    g.setTransform(stashBottom)

    // paint gauges and charts
    speedGauge.paint(g, imageHeight, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)
    if (sonda.cadence.current.isDefined) {
      cadenceGauge.paint(g, imageHeight, gaugeSize, gaugeSize, sonda)
      g.translate(gaugeSize, 0)
    }

    val gaugeSize2 = gaugeSize / 2
    if (sonda.heartRate.current.isDefined) {
      g.translate(0, gaugeSize2)
      heartRateGauge.paint(g, imageHeight, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
    if (sonda.power.current.isDefined) {
      if (sonda.heartRate.current.isDefined) g.translate(-gaugeSize2, -gaugeSize2)
      powerGauge.paint(g, imageHeight, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
  }
}

trait SkiingDashboard extends Dashboard {

  private val speedGauge = new RadialSpeedGauge {}
  private val elevationChart = new ElevationChart {}
  private val heartRateGauge = new SvgHeartRateGauge {}

  override def getName(): String = "Skiing"
  override def gauges(): Seq[GaugePainter] = Seq(speedGauge, elevationChart, heartRateGauge)

  override def paintDashboard(g: Graphics2D, imageWidth: Int, imageHeight: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    g.translate(0, imageHeight - gaugeSize)
    // paint elevation to the right
    val stashBottom = g.getTransform
    g.translate(imageWidth - gaugeSize * 3, gaugeSize / 4)
    elevationChart.paint(g, imageHeight, gaugeSize * 3, gaugeSize * 3 / 4, sonda)
    g.setTransform(stashBottom)

    // paint gauges and charts
    speedGauge.paint(g, imageHeight, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)

    val gaugeSize2 = gaugeSize / 2
    if (sonda.heartRate.current.isDefined) {
      g.translate(0, gaugeSize2)
      heartRateGauge.paint(g, imageHeight, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
  }
}

trait MotorBikingDashboard extends Dashboard {
  private val speedGauge = new RadialSpeedGauge {
    override lazy val dummy: InputValue = InputValue(Some(181), MinMax.max(230))
  }
  private val elevationChart = new ElevationChart {}
  private val heartRateGauge = new SvgHeartRateGauge {}

  override def getName(): String = "MotorBiking"
  override def gauges(): Seq[GaugePainter] = Seq(speedGauge, elevationChart, heartRateGauge)

  override def paintDashboard(g: Graphics2D, imageWidth: Int, imageHeight: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    g.translate(0, imageHeight - gaugeSize)
    // paint elevation to the right
    val stashBottom = g.getTransform
    g.translate(imageWidth - gaugeSize * 3, gaugeSize / 4)
    elevationChart.paint(g, imageHeight, gaugeSize * 3, gaugeSize * 3 / 4, sonda)
    g.setTransform(stashBottom)

    // paint gauges and charts
    speedGauge.paint(g, imageHeight, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)

    val gaugeSize2 = gaugeSize / 2
    if (sonda.heartRate.current.isDefined) {
      g.translate(0, gaugeSize2)
      heartRateGauge.paint(g, imageHeight, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
  }
}

trait SailingDashboard extends Dashboard {

  private val speedGauge = new RadialSpeedGauge {}
  private val azimuthGauge = new RadialAzimuthGauge {}
  private val heartRateGauge = new SvgHeartRateGauge {}

  override def getName(): String = "Sailing"
  override def gauges(): Seq[GaugePainter] = Seq(speedGauge, azimuthGauge, heartRateGauge)

  override def paintDashboard(g: Graphics2D, imageWidth: Int, imageHeight: Int, gaugeSize: Int, sonda: Sonda): Unit = {
    g.translate(0, imageHeight - gaugeSize)
    // paint elevation to the right
    val stashBottom = g.getTransform
    g.translate(imageWidth - gaugeSize * 3, gaugeSize / 4)
    g.setTransform(stashBottom)

    // paint gauges and charts
    speedGauge.paint(g, imageHeight, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)
    azimuthGauge.paint(g, imageHeight, gaugeSize, gaugeSize, sonda)
    g.translate(gaugeSize, 0)


    val gaugeSize2 = gaugeSize / 2
    if (sonda.heartRate.current.isDefined) {
      g.translate(0, gaugeSize2)
      heartRateGauge.paint(g, imageHeight, gaugeSize2, gaugeSize2, sonda)
      g.translate(gaugeSize2, 0)
    }
  }
}
