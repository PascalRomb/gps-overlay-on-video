package peregin.gpv.gui.gauge

import peregin.gpv.model.{InputValue, MinMax, Sonda}

class SvgHeartRateGauge extends SvgGauge {

  lazy val dummy = InputValue(Some(0), MinMax(30, 230))
  override def defaultInput = dummy

  override def sample(sonda: Sonda): Unit = { input = sonda.heartRate }

  override def imagePath = "images/heart.svg"

  override def valueText = {
    val inputValue = input.current.getOrElse(defaultInput.current.get)
    f"${inputValue}%2.0f"
  }

  override def unitText = "bpm"
}
