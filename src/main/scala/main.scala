import java.io.{BufferedWriter, FileWriter, PrintWriter}
import scala.util.{Try, Random}

import scala.concurrent.Await
import scala.concurrent.duration._

import controller.PowerPlantController
import controller.DataCollection




object MainApp {
  def main(args: Array[String]): Unit = {
    val solarData = utils.CSVReader.readSolarData("data/Cleaned_Solar_Data.csv")
    val windData = utils.CSVReader.readWindData("data/Cleaned_Wind_Data.csv")
    val hydroData = utils.CSVReader.readHydroData("data/Cleaned_Hydro_Data.csv")

    val solarPanel = model.SolarPanel("SP-001", solarData)
    val windTurbine = model.WindTurbine("WT-001", windData)
    val hydroPlant = model.HydroPower("HP-001", hydroData)
    var running = true
    while (running) {
      println("1. Monitor the System")
      println("2. Store the collected data in a file")
      println("3. View the data stored in a file")
      println("4. Analyse the data collected")
      println("0. Exit")
      print("Enter your choice:")
      try {
        val choice = scala.io.StdIn.readInt()
        choice match {
          case 1 =>
            PowerPlantController.displayDeviceStatus(solarPanel)
            PowerPlantController.displayDeviceStatus(windTurbine)
            PowerPlantController.displayDeviceStatus(hydroPlant)

          case 2 =>

            print("Enter the date that needs check (yyyy-mm-dd)")
            val date = scala.io.StdIn.readLine()

            Await.result(DataCollection.collectAndStoreData(date), 10.seconds)
          case 3 =>

          case 4 =>

          case 0 =>
            println("Exiting program.")
            running = false
          case _ =>
            println("Invalid choice, please try again.")
        }
      } catch {
        case e: NumberFormatException =>
          println("Invalid input. Please enter a valid number.")
      }
    }
  }
}
