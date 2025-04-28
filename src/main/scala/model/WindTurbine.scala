package model

case class WindTurbine(id: String, forecastData: List[(String, Double)]) extends EnergySource {

  def getLatestData: Option[(String, Double)] = {
    forecastData.lastOption
  }

  override def getCurrentOutput: Double = {
    getLatestData.map(_._2).getOrElse(0.0)
  }

  override def adjustSettings(): String = {
    "Wind turbine aligned to optimal wind direction."
  }
}
