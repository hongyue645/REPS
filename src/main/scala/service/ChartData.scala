package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import scala.io.Source
import scala.collection.mutable

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

  def generateGoogleChartData(aggregatedData: Map[LocalDate, Map[String, Double]]): String = {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dataRows = aggregatedData.map { case (date, powerMap) =>
      val dateStr = date.format(dateFormatter)
      val solar = powerMap.getOrElse("Solar", 0.0)
      val wind = powerMap.getOrElse("Wind", 0.0)
      val hydro = powerMap.getOrElse("Hydro", 0.0)
      s"['$dateStr', $solar, $wind, $hydro]"
    }.mkString(",\n")

    s"""
    var data = new google.visualization.DataTable();
    data.addColumn('string', 'Date');
    data.addColumn('number', 'Solar');
    data.addColumn('number', 'Wind');
    data.addColumn('number', 'Hydro');

    data.addRows([
      $dataRows
    ]);
    """
  }
}
