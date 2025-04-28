package model

case class HydroPower(id: String, forecastData: List[EnergyData]) extends EnergySource {

  override def getCurrentOutput: Double = {
    getLatestData.map(_._2).getOrElse(0.0)
  }

  override def adjustSettings(): String = {
    "Hydro plant flow rate adjusted for optimal power generation."
  }

  override def getLatestData: Option[(String, Double)] = {
    forecastData.lastOption.map(data => (data.dateString + " " + data.hourString, data.power))
  }

  override def getDataByDate(year: Int, month: Int, day: Int, hour: Option[Int] = None): List[EnergyData] = {
    hour match {
      case Some(h) => forecastData.filter(e => e.year == year && e.month == month && e.day == day && e.hour == h)
      case None => forecastData.filter(e => e.year == year && e.month == month && e.day == day)
    }
  }

  override def date: String = forecastData.lastOption.map(_.dateString).getOrElse("N/A")

  override def powerOfDate: String = forecastData.lastOption.map(_.power.toString).getOrElse("N/A")
}
