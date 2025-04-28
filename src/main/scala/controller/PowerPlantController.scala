package controller

import model.{SolarPanel, WindTurbine, HydroPower, EnergySource}
import utils.CSVReader

object PowerPlantController {

  def monitor(source: EnergySource, year: Int, month: Int, day: Int, hour: Option[Int] = None): Unit = {
    val dailyData = source.getDataByDate(year, month, day, hour)

    if (dailyData.isEmpty) {
      println(s"No data available for $year-$month-$day${hour.map(h => s" $h:00").getOrElse("")}.")
    } else {
      dailyData.foreach { record =>
        println(f"[Monitor] ${record.hourString} - Output: ${record.power}%.1f MW")
        if (record.power < threshold(source)) {
          println(f"[Control] ${record.hourString} - Low output detected! Adjusting settings...")
          val controlResult = control(source)
          println(s"[Control] Adjustment result: $controlResult")
        }
      }
    }
  }

  def control(source: EnergySource): String = {
    source.adjustSettings()
  }

  def threshold(source: EnergySource): Double = source match {
    case _: SolarPanel => 50.0
    case _: WindTurbine => 30.0
    case _: HydroPower => 70.0
    case _ => 9999.0
  }

  def main(args: Array[String]): Unit = {
    val solarData = CSVReader.readSolarData("data/Cleaned_Solar_Data.csv")
    val windData = CSVReader.readWindData("data/Cleaned_Wind_Data.csv")
    val hydroData = CSVReader.readHydroData("data/Cleaned_Hydro_Data.csv")

    val solarPanel = SolarPanel("SP-001", solarData)
    val windTurbine = WindTurbine("WT-001", windData)
    val hydroPlant = HydroPower("HP-001", hydroData)

    println("Program ready. Please run MainApp.scala to interact with the system.")
  }
}
