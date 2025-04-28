package model

case class EnergyData(
                       year: Int,
                       month: Int,
                       day: Int,
                       hour: Int,
                       power: Double
                     ) {
  def dateString: String = f"$year-$month%02d-$day%02d"
  def hourString: String = f"$hour%02d:00"
}
