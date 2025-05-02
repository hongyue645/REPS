package utils

import model.{SolarPanel, WindTurbine, HydroPower}
import java.io._
import scala.util.Try

object DataCollection {

  def writeToCSV(filePath: String, data: List[String]): Unit = {
    val writer = new BufferedWriter(new FileWriter(filePath))
    try {
      data.foreach(line => writer.write(line + "\n"))
    } finally {
      writer.close()
    }
  }

  def collectDataForYear(year: Int, solarData: List[String], windData: List[String], hydroData: List[String]): Unit = {
    val combinedData = solarData ++ windData ++ hydroData
    val header = "Year,Month,Day,Hour,Power,EnergySource"

    val dataWithHeader = combinedData match {
      case Nil => List(header)
      case _ => header +: combinedData
    }

    val outputFilePath = s"data/Combined_Power_Data_$year.csv"
    writeToCSV(outputFilePath, dataWithHeader)
    println(s"Data for $year collected and written to $outputFilePath")
  }
  def getYearData(source: Any, year: Int): List[String] = {

    (1 to 12).flatMap { month =>

      val daysInMonth = getDaysInMonth(year, month)

      (1 to daysInMonth).flatMap { day =>
        source match {
          case solar: SolarPanel =>
            solar.getDataByDate(year, month, day).flatMap(record => formatRecord(record, "Solar"))
          case wind: WindTurbine =>
            wind.getDataByDate(year, month, day).flatMap(record => formatRecord(record, "Wind"))
          case hydro: HydroPower =>
            hydro.getDataByDate(year, month, day).flatMap(record => formatRecord(record, "Hydro"))
          case _ => Nil
        }
      }
    }.toList
  }
  def getDaysInMonth(year: Int, month: Int): Int = month match {
    case 1 | 3 | 5 | 7 | 8 | 10 | 12 => 31
    case 4 | 6 | 9 | 11 => 30
    case 2 => if (isLeapYear(year)) 29 else 28
    case _ => 0
  }
  def isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)


  def formatRecord(record: model.EnergyData, source: String): List[String] = {

    val formattedData = s"${record.year},${record.month},${record.day},${record.hour},${record.power},$source"
    List(formattedData)
  }

  def main(args: Array[String]): Unit = {
    val solarData = MyCSVReader.readSolarData("data/Cleaned_Solar_Data.csv")
    val windData = MyCSVReader.readWindData("data/Cleaned_Wind_Data.csv")
    val hydroData = MyCSVReader.readHydroData("data/Cleaned_Hydro_Data.csv")

    val solarPanel = SolarPanel("SP-001", solarData)
    val windTurbine = WindTurbine("WT-001", windData)
    val hydroPlant = HydroPower("HP-001", hydroData)

    val year = 2022
    val solarYearData = getYearData(solarPanel, year)
    val windYearData = getYearData(windTurbine, year)
    val hydroYearData = getYearData(hydroPlant, year)

    collectDataForYear(year, solarYearData, windYearData, hydroYearData)
  }
}
