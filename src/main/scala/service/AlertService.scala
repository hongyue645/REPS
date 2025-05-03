package service

import scala.io.Source
import scala.util.Try

object AlertService {

  val lowOutputThreshold: Double = 20.0

  def checkEnergyStatus(dateInput: String): List[String] = {
    val dateParts = dateInput.trim.split("-").map(_.toIntOption)
    if (dateParts.length != 3 || dateParts.exists(_.isEmpty)) {
      List(s"Error: Invalid date format. Use YYYY-MM-DD")
    } else {
      val year = dateParts(0).get
      val month = dateParts(1).get
      val day = dateParts(2).get
      val formattedDate = f"$year-$month%02d-$day%02d"
      val filePath = s"data/Combined_Power_Data_$year.csv"

      Try(Source.fromFile(filePath, "UTF-8")).map { source =>
        val lines = source.getLines().toList
        source.close()

        if (lines.isEmpty) {
          List(s"$formattedDate: No data available.")
        } else {
          val header = lines.head.replace("\uFEFF", "").split(",").map(_.trim)
          val dataLines = lines.tail

          val yearIdx = header.indexOf("Year")
          val monthIdx = header.indexOf("Month")
          val dayIdx = header.indexOf("Day")
          val hourIdx = header.indexOf("Hour")
          val powerIdx = header.indexOf("Power")
          val energyTypeIdx = header.indexOf("EnergySource")

          if (Seq(yearIdx, monthIdx, dayIdx, hourIdx, powerIdx, energyTypeIdx).contains(-1)) {
            List("Error: CSV header does not contain all required fields.")
          } else {
            val grouped = dataLines.flatMap { line =>
              val cols = line.split(",").map(_.trim)
              if (cols.length > Seq(yearIdx, monthIdx, dayIdx, hourIdx, powerIdx, energyTypeIdx).max) {
                val rowYear = Try(cols(yearIdx).toInt).getOrElse(-1)
                val rowMonth = Try(cols(monthIdx).toInt).getOrElse(-1)
                val rowDay = Try(cols(dayIdx).toInt).getOrElse(-1)
                val rowHour = Try(cols(hourIdx).toInt).getOrElse(-1)
                val energyType = cols(energyTypeIdx)
                val powerTry = Try(cols(powerIdx).toDouble)

                if (rowYear == year && rowMonth == month && rowDay == day)
                  Some((energyType, rowHour, powerTry.toOption))
                else None
              } else None
            }

            val groupedByType = grouped.groupBy(_._1)
            val allSources = List("Solar", "Wind", "Hydro")

            allSources.flatMap { source =>
              val entries = groupedByType.getOrElse(source, Nil)
              if (entries.isEmpty) {
                Some(s"Warning: $formattedDate $source data missing for the entire day.")
              } else {
                val alerts = entries.flatMap {
                  case (_, hour, Some(power)) if power < lowOutputThreshold =>
                    Some(s"Alert: $formattedDate $source output is low at hour $hour ($power)")
                  case (_, hour, None) =>
                    Some(s"Error: $formattedDate $source has data error at hour $hour")
                  case _ => None
                }
                if (alerts.isEmpty)
                  Some(s"$formattedDate $source output is normal.")
                else alerts
              }
            }
          }
        }
      }.getOrElse(List(s"Error: Cannot read file $filePath"))
    }
  }
}
