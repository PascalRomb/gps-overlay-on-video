package peregin.gpv.gui.dashboard

import peregin.gpv.gui.gauge.GaugePainter
import peregin.gpv.model.Sonda

import scala.swing.Graphics2D

abstract class Dashboard extends Cloneable {

  override def clone(): Dashboard = super.clone().asInstanceOf[Dashboard]

  def gauges(): Seq[GaugePainter]

  def paintDashboard(g: Graphics2D, imageWidth: Int, imageHeight: Int, gaugeSize: Int, sonda: Sonda): Unit

  def getName(): String
}