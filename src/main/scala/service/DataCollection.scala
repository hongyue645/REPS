package controller
import scala.concurrent.duration._
import utils.CSVReader
import model.{SolarPanel, WindTurbine, HydroPower, EnergySource}
import java.io.{BufferedWriter, FileWriter, PrintWriter}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.{Random, Try}

object DataCollection {

  implicit val ec: ExecutionContext = ExecutionContext.global

  def collectAndStoreData(date:String): Future[Unit] = Future {

    val solarData = CSVReader.readSolarData("data/Cleaned_Solar_Data.csv")
    val windData = CSVReader.readWindData("data/Cleaned_Wind_Data.csv")
    val hydroData = CSVReader.readHydroData("data/Cleaned_Hydro_Data.csv")

    val solarPanel = SolarPanel("SP-001", solarData)
    val windTurbine = WindTurbine("WT-001", windData)
    val hydroPlant = HydroPower("HP-001", hydroData)

    storeDataToFile(solarPanel, windTurbine, hydroPlant,date)
  }

  def storeDataToFile(solarPanel: SolarPanel, windTurbine: WindTurbine, hydroPlant: HydroPower, date:String): Unit = {
    val filePath = "data/collected_energy_data.csv"

    val writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))

    if (new java.io.File(filePath).length() == 0) {
      writer.println("Device,Date,EnergyGenerated(MW)")
    }

    writeDeviceData(writer, solarPanel,date)
    writeDeviceData(writer, windTurbine, date)
    writeDeviceData(writer, hydroPlant, date)

    writer.close()
    println(s"Data has been saved to $filePath")
  }

  def writeDeviceData(writer: PrintWriter, source: EnergySource, date: String): Unit = {
    val data = source.getLatestData match {
      case Some((_, energy)) =>
        s"${source.getClass.getSimpleName},$date,$energy"
      case None => s"${source.getClass.getSimpleName},$date,No data available"
    }
    writer.println(data)
  }


  def scheduleDataCollection(dateString: String): Unit = {
    val interval = 1.hour
    val scheduler = new java.util.Timer()

    scheduler.scheduleAtFixedRate(new java.util.TimerTask {
      def run(): Unit = {
        println("Collecting data...")
        collectAndStoreData(dateString)
      }
    }, 0, interval.toMillis)
  }


  def main(args: Array[String]): Unit = {
    val date = "2024-4-19"
    println("Starting data collection...")
    scheduleDataCollection(date)
  }
}
