package utils

import model.EnergyData
import scala.io.Source

object CSVReader {

  def readSolarData(path: String): List[EnergyData] = readCSV(path)
  def readWindData(path: String): List[EnergyData] = readCSV(path)
  def readHydroData(path: String): List[EnergyData] = readCSV(path)

  private def readCSV(filePath: String): List[EnergyData] = {
    val bufferedSource = Source.fromFile(filePath)
    val data = bufferedSource.getLines().drop(1).map { line =>
      val cols = line.split(",").map(_.trim)
      val startTime = cols(0) // 取startTime这一列
      val power = cols(2).toDouble // hydroGeneration这一列

      val dateAndTime = startTime.split(" ")
      val datePart = dateAndTime(0) // "2016-04-22"
      val timePart = dateAndTime(1) // "21:00:00+00:00"

      val dateFields = datePart.split("-")
      val year = dateFields(0).toInt
      val month = dateFields(1).toInt
      val day = dateFields(2).toInt

      val hour = timePart.split(":")(0).toInt // 取"21"小时部分

      EnergyData(year, month, day, hour, power)
    }.toList
    bufferedSource.close()
    data
    
  }
}
