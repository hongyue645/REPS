package service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import scala.util.{Using, Try}

object DataAnalysis {

  /**
   * Represents a power generation record with timestamp components.
   */
  case class Record(
                     timestamp: LocalDateTime,
                     year:      Int,
                     month:     Int,
                     day:       Int,
                     hour:      Int,
                     solar:     Double,
                     wind:      Double,
                     hydro:     Double
                   )

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  /**
   * Loads CSV data and parses into a list of Record instances.
   * Skips header and logs any malformed lines.
   */
  def loadData(csvPath: String): List[Record] = {
    Using(scala.io.Source.fromFile(csvPath)) { source =>
      source.getLines().drop(1).flatMap { line =>
        val cols = line.split(",").map(_.trim)
        val optRecord =
          if (cols.length >= 6 && cols(0).forall(_.isDigit)) {
            // combined format: year,month,day,hour,power,source
            Try {
              val y     = cols(0).toInt
              val m     = cols(1).toInt
              val d     = cols(2).toInt
              val h     = cols(3).toInt
              val p     = cols(4).toDouble
              val src   = cols(5).toLowerCase
              val ts    = LocalDateTime.of(y, m, d, h, 0)
              Record(ts, y, m, d, h,
                if (src=="solar") p else 0.0,
                if (src=="wind")  p else 0.0,
                if (src=="hydro") p else 0.0)
            }.toOption
          } else {
            // timestamp format: yyyy-MM-dd HH:mm:ss,solar,wind,hydro
            Try {
              val ts    = LocalDateTime.parse(cols(0), formatter)
              val sol   = cols.lift(1).flatMap(s => Try(s.toDouble).toOption).getOrElse(0.0)
              val wind  = cols.lift(2).flatMap(s => Try(s.toDouble).toOption).getOrElse(0.0)
              val hydro = cols.lift(3).flatMap(s => Try(s.toDouble).toOption).getOrElse(0.0)
              Record(ts, ts.getYear, ts.getMonthValue, ts.getDayOfMonth, ts.getHour, sol, wind, hydro)
            }.toOption
          }

        if (optRecord.isEmpty) println(s"Warning: skipped malformed line: $line")
        optRecord
      }.toList
    }.getOrElse {
      println(s"Error: failed to load data from $csvPath")
      Nil
    }
  }

  /** Returns the ISO week‐number for a LocalDateTime. */
  def getWeekOfYear(ts: LocalDateTime): Int =
    ts.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

  /** Filter functions **/
  def filterByHour(data: List[Record], hour: Int): List[Record] =
    data.filter(_.hour == hour)

  def filterByDay(data: List[Record], year: Int, month: Int, day: Int): List[Record] =
    data.filter(r => r.year == year && r.month == month && r.day == day)

  def filterByWeek(data: List[Record], year: Int, week: Int): List[Record] =
    data.filter(r => r.year == year && getWeekOfYear(r.timestamp) == week)

  def filterByMonth(data: List[Record], month: Int): List[Record] =
    data.filter(_.month == month)

  /**
   * Aggregates records by the given period (hour, day, week, month).
   */
  def aggregate(records: List[Record], period: String): Map[String, (Double, Double, Double)] =
    records
      .groupBy { r =>
        period.toLowerCase match {
          case "hour"  => f"${r.year}-${r.month}%02d-${r.day}%02d ${r.hour}%02d:00"
          case "day"   => f"${r.year}-${r.month}%02d-${r.day}%02d"
          case "week"  => f"${r.year}-W${getWeekOfYear(r.timestamp)}%02d"
          case "month" => f"${r.year}-${r.month}%02d"
          case other   =>
            println(s"Unknown period '$other', defaulting to day")
            f"${r.year}-${r.month}%02d-${r.day}%02d"
        }
      }
      .view
      .mapValues { recs =>
        (recs.map(_.solar).sum, recs.map(_.wind).sum, recs.map(_.hydro).sum)
      }
      .toMap

  /**
   * Prints the aggregated results in a table.
   */
  def printAggregates(agg: Map[String, (Double, Double, Double)]): Unit = {
    println(f"${"Period"}%-20s | ${"Solar"}%10s | ${"Wind"}%10s | ${"Hydro"}%10s")
    println("=" * 60)
    agg.toSeq.sortBy(_._1).foreach { case (period, (s, w, h)) =>
      println(f"$period%-20s | $s%10.2f | $w%10.2f | $h%10.2f")
    }
  }

  // (Optional) a main method to demo usage:
  def main(args: Array[String]): Unit = {
    val data = loadData("data/power.csv")
    val daily = aggregate(filterByDay(data, 2025, 5, 1), "day")
    printAggregates(daily)
  }

}
