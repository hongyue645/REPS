package model

import model.EnergyData

trait EnergySource {
  def id: String
  def date: String
  def powerOfDate: String// 每个设备有唯一 id
  def getCurrentOutput: Double        // 当前发电量
  def getLatestData: Option[(String, Double)]  // 获取最新数据
  def adjustSettings(): String       // 控制行为（如调整角度）

  def getDataByDate(year: Int, month: Int, day: Int, hour: Option[Int] = None): List[EnergyData]
}

