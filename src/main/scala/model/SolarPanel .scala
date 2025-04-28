package model

import model.EnergySource

case class SolarPanel(id: String, forecastData: List[(String, Double)]) extends EnergySource {

  // 模拟获取最近一次的发电预测值
  override def getCurrentOutput: Double = {
    getLatestData.map(_._2).getOrElse(0.0)
  }


  // 模拟调整太阳能板角度（返回字符串说明）
  override def adjustSettings(): String = {
    "Solar panel adjusted to track the sun."
  }

  def getLatestData: Option[(String, Double)] = {
    forecastData.lastOption
  }

}
