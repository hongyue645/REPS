package utils

import scala.io.Source

object CSVReader {
  def readSolarData(filePath: String): List[(String, Double)] = {
    val buffered = Source.fromFile(filePath)
    val data = buffered.getLines().drop(1).toList.flatMap { line =>
      val cols = line.split(",")
      if (cols.length >= 3) {
        try {
          Some((cols(0), cols(2).toDouble))
        } catch {
          case _: NumberFormatException => None
        }
      } else None
    }
    buffered.close()
    data
  }

  def readWindData(filePath: String): List[(String, Double)] = {
    val buffered = Source.fromFile(filePath)
    val data = buffered.getLines().drop(1).toList.flatMap { line =>
      val cols = line.split(",")
      if (cols.length >= 3) {
        try Some((cols(0), cols(2).toDouble))
        catch {
          case _: NumberFormatException => None
        }
      } else None
    }
    buffered.close()

    data
  }

  def readHydroData(filePath: String): List[(String, Double)] = {
    val buffered = Source.fromFile(filePath)
    val data = buffered.getLines().drop(1).toList.flatMap { line =>
      val cols = line.split(",")
      if (cols.length >= 3) {
        try Some((cols(0), cols(2).toDouble))
        catch {
          case _: NumberFormatException => None
        }
      } else None
    }
    buffered.close()
    data
  }


}