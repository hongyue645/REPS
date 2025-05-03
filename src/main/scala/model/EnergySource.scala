package model

import model.EnergyData

trait EnergySource {
  def id: String
  def date: String
  def powerOfDate: String
  def getCurrentOutput: Double
  def getLatestData: Option[(String, Double)]
  def adjustSettings(): String

  def getDataByDate(year: Int, month: Int, day: Int, hour: Option[Int] = None): List[EnergyData]
}



