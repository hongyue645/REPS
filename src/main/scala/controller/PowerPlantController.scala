package controller

import model.{SolarPanel, WindTurbine, HydroPower, EnergySource}

import utils.CSVReader

object PowerPlantController {

  def monitor(source: EnergySource): String = {
    val latest = source match {
      case sp: SolarPanel => sp.getLatestData
      case wt: WindTurbine => wt.getLatestData
      case hp: HydroPower => hp.getLatestData
      case _ => None
    }

    latest match {
      case Some((time, value)) =>
        f"Current output is $value%.1f MW at $time"
      case None =>
        "No data available."
    }
  }


  def control(source: EnergySource): String = {
    source.adjustSettings()
  }

  def displayDeviceStatus(source: EnergySource): Unit = {
    val title = source match {
      case _: SolarPanel => "Solar Panel"
      case _: WindTurbine => "Wind Turbine"
      case _: HydroPower => "Hydro Power"
      case _ => "Energy Device"
    }

    println("=" * 15 + s" $title [${source.id}] " + "=" * 15)
    println("Monitoring: " + monitor(source))
    println("Control:    " + control(source))
    println()
  }


  def main(args: Array[String]): Unit = {
    val solarData = CSVReader.readSolarData("data/Cleaned_Solar_Data.csv")
    val windData = CSVReader.readWindData("data/Cleaned_Wind_Data.csv")
    val hydroData = CSVReader.readHydroData("data/Cleaned_Hydro_Data.csv")

    val solarPanel = SolarPanel("SP-001", solarData)
    val windTurbine = WindTurbine("WT-001", windData)
    val hydroPlant = HydroPower("HP-001", hydroData)

    displayDeviceStatus(solarPanel)
    displayDeviceStatus(windTurbine)
    displayDeviceStatus(hydroPlant)
  }

}