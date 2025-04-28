package model

case class HydroPower(id: String, forecastData: List[(String, Double)]) extends EnergySource {

  // 获取最新一条水力数据
  def getLatestData: Option[(String, Double)] = {
    forecastData.lastOption
  }

  override def getCurrentOutput: Double = {
    getLatestData.map(_._2).getOrElse(0.0)
  }

  override def adjustSettings(): String = {
    "Hydro plant flow rate adjusted for optimal power generation."
  }
}
