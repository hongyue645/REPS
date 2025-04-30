package service

import com.github.tototoshi.csv._
import java.io.File
import scala.collection.mutable.ListBuffer

object DataAnalysis {
  def analyzeEnergyData(year: Int, startDate: String, endDate: String, energyType: String): Option[Map[String, Double]] = {
    val filePath = s"data/Combined_Power_Data_$year.csv"
    val file = new File(filePath)
    
    if (!file.exists()) {
      println(s"No data exists for year $year")
      return None
    }

    try {
      val reader = CSVReader.open(file)
      val data = reader.allWithHeaders()
      reader.close()

      // Parse start and end dates
      val (startMonth, startDay) = parseDate(startDate)
      val (endMonth, endDay) = parseDate(endDate)

      // Filter data by date range and energy type
      val filteredData = data.filter { row =>
        val month = row("Month").toInt
        val day = row("Day").toInt
        val source = row("EnergySource")

        isDateInRange(month, day, startMonth, startDay, endMonth, endDay) && 
        source == energyType
      }

      // Group by date and calculate daily totals
      val dailyTotals = filteredData.groupBy(row => s"${row("Month")}-${row("Day")}")
        .map { case (date, rows) =>
          date -> rows.map(_("Power").toDouble).sum
        }

      if (dailyTotals.isEmpty) {
        println("No data found for the specified criteria")
        return None
      }

      val values = dailyTotals.values.toList
      val average = values.sum / values.length
      val median = calculateMedian(values)
      val range = values.max - values.min
      val midrange = (values.max + values.min) / 2.0

      // Calculate mode
      val mode = calculateMode(values)

      Some(Map(
        "average" -> average,
        "median" -> median,
        "range" -> range,
        "midrange" -> midrange,
        "mode" -> mode
      ))
    } catch {
      case e: Exception =>
        println(s"Error analyzing data: ${e.getMessage}")
        None
    }
  }

  private def parseDate(date: String): (Int, Int) = {
    val parts = date.split("-")
    (parts(0).toInt, parts(1).toInt)
  }

  private def isDateInRange(month: Int, day: Int, startMonth: Int, startDay: Int, endMonth: Int, endDay: Int): Boolean = {
    val date = month * 100 + day
    val start = startMonth * 100 + startDay
    val end = endMonth * 100 + endDay
    date >= start && date <= end
  }

  private def calculateMedian(values: List[Double]): Double = {
    val sorted = values.sorted
    if (sorted.length % 2 == 0) {
      val mid = sorted.length / 2
      (sorted(mid - 1) + sorted(mid)) / 2.0
    } else {
      sorted(sorted.length / 2)
    }
  }

  private def calculatePercentile(values: List[Double], percentile: Int): Double = {
    val sorted = values.sorted
    val index = (percentile * sorted.length) / 100
    sorted(index)
  }

  private def calculateMode(values: List[Double]): Double = {
    // Count frequency of each value
    val frequencyMap = values.groupBy(identity).mapValues(_.size)
    // Find the value with maximum frequency
    frequencyMap.maxBy(_._2)._1
  }
}