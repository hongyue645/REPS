package service

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.io.Source
import scala.collection.mutable
import com.github.tototoshi.csv._

case class PowerData(year: Int, month: Int, day: Int, hour: Int, power: Double, energySource: String)

object ChartData {

  def parseDate(year: Int, month: Int, day: Int): LocalDate = {
    LocalDate.of(year, month, day)
  }

  def loadData(filePath: String): Seq[PowerData] = {
    val source = Source.fromFile(filePath)
    val data = source.getLines().drop(1).map { line =>
      val Array(year, month, day, hour, power, energySource) = line.split(",")
      PowerData(year.toInt, month.toInt, day.toInt, hour.toInt, power.toDouble, energySource)
    }.toSeq
    source.close()
    data
  }

  def aggregatePowerData(data: Seq[PowerData]): Map[LocalDate, Map[String, Double]] = {
    val groupedData = mutable.Map[LocalDate, mutable.Map[String, Double]]()

    data.foreach { entry =>
      val date = parseDate(entry.year, entry.month, entry.day)
      val energySource = entry.energySource

      val dailyData = groupedData.getOrElseUpdate(date, mutable.Map("Solar" -> 0.0, "Wind" -> 0.0, "Hydro" -> 0.0))
      dailyData(energySource) += entry.power
    }

    groupedData.mapValues(_.toMap).toMap
  }

  def generateGoogleChartDataFromCSV(filePath: String): String = {
    val data = loadData(filePath)
    val aggregatedData = aggregatePowerData(data)
    val storageMap = estimateStorage(data)

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val sortedData = aggregatedData.toSeq.sortBy(_._1)

    val dataRows = sortedData.map { case (date, powerMap) =>
      val dateStr = date.format(dateFormatter)
      val solar = powerMap.getOrElse("Solar", 0.0)
      val wind = powerMap.getOrElse("Wind", 0.0)
      val hydro = powerMap.getOrElse("Hydro", 0.0)
      val storage = storageMap.getOrElse(date, 0.0)
      s"['$dateStr', $solar, $wind, $hydro, $storage]"
    }.mkString(",\n")

    s"""
      var data = google.visualization.arrayToDataTable([
        ['Date', 'Solar', 'Wind', 'Hydro', 'StorageEstimate'],
        $dataRows
      ]);
    """
  }

  def estimateStorage(data: Seq[PowerData], dailyConsumption: Double = 10000.0, maxStorage: Double = 2000000): Map[LocalDate, Double] = {
    var currentStorage = 0.0
    val storageMap = mutable.LinkedHashMap[LocalDate, Double]()

    val dailyGeneration = aggregatePowerData(data)

    dailyGeneration.toSeq.sortBy(_._1).foreach { case (date, powerMap) =>
      val dailyTotal = powerMap.values.sum
      currentStorage += (dailyTotal - dailyConsumption)
      currentStorage = math.max(0.0, math.min(currentStorage, maxStorage)) // 限制在0~max
      storageMap(date) = currentStorage
    }

    storageMap.toMap
  }

}
