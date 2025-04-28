package model

trait EnergySource {
  def id: String                     // 每个设备有唯一 id
  def getCurrentOutput: Double        // 当前发电量
  def getLatestData: Option[(String, Double)]  // 获取最新数据
  def adjustSettings(): String       // 控制行为（如调整角度）
}

case class SolarPanel(id: String, data: List[(String, Double)]) extends EnergySource {
  // 实现 getCurrentOutput
  def getCurrentOutput: Double = data.lastOption.map(_._2).getOrElse(0.0)

  def getLatestData: Option[(String, Double)] = data.lastOption
  def adjustSettings(): String = "Adjusting solar panel settings..."
}

case class WindTurbine(id: String, data: List[(String, Double)]) extends EnergySource {
  // 实现 getCurrentOutput
  def getCurrentOutput: Double = data.lastOption.map(_._2).getOrElse(0.0)

  def getLatestData: Option[(String, Double)] = data.lastOption
  def adjustSettings(): String = "Adjusting wind turbine settings..."
}

case class HydroPower(id: String, data: List[(String, Double)]) extends EnergySource {
  // 实现 getCurrentOutput
  def getCurrentOutput: Double = data.lastOption.map(_._2).getOrElse(0.0)

  def getLatestData: Option[(String, Double)] = data.lastOption
  def adjustSettings(): String = "Adjusting hydro power settings..."
}
