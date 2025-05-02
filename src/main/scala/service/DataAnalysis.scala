package service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import scala.util.{Using, Try}

object DataAnalysis {

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

  case class Stats(
                    mean: Double,
                    median: Double,
                    mode: Seq[Double],
                    range: Double,
                    midrange: Double
                  )

  object StatsCalculator {
    private def aggregate(data: Seq[Double])(op: (Double, Double) => Double): Double =
      data.foldLeft(0.0)(op)

    def mean(data: Seq[Double]): Double =
      if (data.isEmpty) 0.0
      else aggregate(data)(_ + _) / data.size

    def median(data: Seq[Double]): Double = {
      val sorted = data.sorted
      val n = sorted.size
      if (n == 0) 0.0
      else if (n % 2 == 1) sorted(n / 2)
      else (sorted(n / 2 - 1) + sorted(n / 2)) / 2.0
    }

    def mode(data: Seq[Double]): Seq[Double] =
      if (data.isEmpty) Seq.empty
      else {
        val freqs = data.groupBy(identity).view.mapValues(_.size).toMap
        val maxCount = freqs.values.max
        freqs.collect { case (v, c) if c == maxCount => v }.toSeq
      }

    def range(data: Seq[Double]): Double =
      if (data.isEmpty) 0.0
      else {
        val (minVal, maxVal) = data.foldLeft((Double.MaxValue, Double.MinValue)) {
          case ((minAcc, maxAcc), x) => (math.min(minAcc, x), math.max(maxAcc, x))
        }
        maxVal - minVal
      }

    def midrange(data: Seq[Double]): Double =
      if (data.isEmpty) 0.0
      else {
        val (minVal, maxVal) = data.foldLeft((Double.MaxValue, Double.MinValue)) {
          case ((minAcc, maxAcc), x) => (math.min(minAcc, x), math.max(maxAcc, x))
        }
        (minVal + maxVal) / 2.0
      }
  }

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  def loadData(csvPath: String): List[Record] = {
    Using(scala.io.Source.fromFile(csvPath)) { source =>
      source.getLines().drop(1).flatMap { line =>
        val cols = line.split(",").map(_.trim)
        val optRecord =
          if (cols.length >= 6 && cols(0).forall(_.isDigit)) {
            Try {
              val y   = cols(0).toInt
              val m   = cols(1).toInt
              val d   = cols(2).toInt
              val h   = cols(3).toInt
              val p   = cols(4).toDouble
              val src = cols(5).toLowerCase
              val ts  = LocalDateTime.of(y, m, d, h, 0)
              Record(ts, y, m, d, h,
                if (src == "solar") p else 0.0,
                if (src == "wind") p else 0.0,
                if (src == "hydro") p else 0.0)
            }.toOption
          } else {
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

  def getWeekOfYear(ts: LocalDateTime): Int =
    ts.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

  def filterByHour(data: List[Record], hour: Int): List[Record] =
    data.filter(_.hour == hour)

  def filterByDay(data: List[Record], year: Int, month: Int, day: Int): List[Record] =
    data.filter(r => r.year == year && r.month == month && r.day == day)

  def filterByWeek(data: List[Record], year: Int, week: Int): List[Record] =
    data.filter(r => r.year == year && getWeekOfYear(r.timestamp) == week)

  def filterByMonth(data: List[Record], month: Int): List[Record] =
    data.filter(_.month == month)

  private def periodKey(period: String, r: Record): String = period.toLowerCase match {
    case "hour"  => f"${r.year}-${r.month}%02d-${r.day}%02d ${r.hour}%02d:00"
    case "day"   => f"${r.year}-${r.month}%02d-${r.day}%02d"
    case "week"  => f"${r.year}-W${getWeekOfYear(r.timestamp)}%02d"
    case "month" => f"${r.year}-${r.month}%02d"
    case _        => f"${r.year}-${r.month}%02d-${r.day}%02d"
  }

  def aggregate(records: List[Record], period: String): Map[String, (Double, Double, Double)] =
    records.groupBy(r => periodKey(period, r)).view.mapValues { recs =>
      (recs.map(_.solar).sum, recs.map(_.wind).sum, recs.map(_.hydro).sum)
    }.toMap

  def printAggregates(agg: Map[String, (Double, Double, Double)]): Unit = {
    println(f"${"Period"}%-20s | ${"Solar"}%10s | ${"Wind"}%10s | ${"Hydro"}%10s")
    println("=" * 60)
    agg.toSeq.sortBy(_._1).foreach { case (period, (s, w, h)) =>
      println(f"$period%-20s | $s%10.2f | $w%10.2f | $h%10.2f")
    }
  }

  def printDescriptiveStats(records: List[Record], period: String): Unit = {
    val groups = records.groupBy(r => periodKey(period, r))
    groups.toSeq.sortBy(_._1).foreach { case (key, recs) =>
      println(s"\nStatistics for period: $key")
      println(f"${"Source"}%10s | ${"Mean"}%10s | ${"Median"}%10s | ${"Mode"}%15s | ${"Range"}%10s | ${"Midrange"}%10s")
      println("-" * 80)
      Seq(
        "Solar" -> recs.map(_.solar),
        "Wind"  -> recs.map(_.wind),
        "Hydro" -> recs.map(_.hydro)
      ).foreach { case (name, vals) =>
        if (vals.nonEmpty) {
          val stats = Stats(
            StatsCalculator.mean(vals),
            StatsCalculator.median(vals),
            StatsCalculator.mode(vals),
            StatsCalculator.range(vals),
            StatsCalculator.midrange(vals)
          )
          println(f"$name%10s | ${stats.mean}%10.2f | ${stats.median}%10.2f | ${stats.mode.mkString(",")}%15s | ${stats.range}%10.2f | ${stats.midrange}%10.2f")
        } else {
          println(f"$name%10s | No data")
        }
      }
    }
  }
}
