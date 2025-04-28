import controller.PowerPlantController
import model.{SolarPanel, WindTurbine, HydroPower}
import utils.CSVReader

object MainApp {
  def main(args: Array[String]): Unit = {
    val solarData = CSVReader.readSolarData("data/Cleaned_Solar_Data.csv")
    val windData = CSVReader.readWindData("data/Cleaned_Wind_Data.csv")
    val hydroData = CSVReader.readHydroData("data/Cleaned_Hydro_Data.csv")

    val solarPanel = SolarPanel("SP-001", solarData)
    val windTurbine = WindTurbine("WT-001", windData)
    val hydroPlant = HydroPower("HP-001", hydroData)

    var running = true
    while (running) {
      println("\n=== Renewable Energy Plant System ===")
      println("1. Monitor the System")
      println("2. Store the collected data in a file (TODO)")
      println("3. View the data stored in a file (TODO)")
      println("4. Analyse the data collected (TODO)")
      println("0. Exit")
      print("Enter your choice: ")
      try {
        val choice = scala.io.StdIn.readInt()
        choice match {
          case 1 =>
            println("Enter date (yyyy-mm-dd): ")
            val inputDate = scala.io.StdIn.readLine()

            val parts = inputDate.split("-")
            val year = parts(0).toInt
            val month = parts(1).toInt
            val day = parts(2).toInt

            println("Do you want to monitor a specific hour? (y/n): ")
            val hourChoice = scala.io.StdIn.readLine()

            val hourOpt: Option[Int] = if (hourChoice.toLowerCase == "y") {
              println("Enter the hour (0-23): ")
              Some(scala.io.StdIn.readInt())
            } else {
              None
            }

            // 调用 monitor
            println("\nSolar Panel Monitoring:")
            PowerPlantController.monitor(solarPanel, year, month, day, hourOpt)

            println("\nWind Turbine Monitoring:")
            PowerPlantController.monitor(windTurbine, year, month, day, hourOpt)

            println("\nHydro Power Monitoring:")
            PowerPlantController.monitor(hydroPlant, year, month, day, hourOpt)


          case 2 =>
            println("功能开发中 (Store data)")

          case 3 =>
            println("功能开发中 (View stored file)")

          case 4 =>
            println("功能开发中 (Analyze data)")

          case 0 =>
            println("Exiting program.")
            running = false

          case _ =>
            println("Invalid choice, please try again.")
        }
      } catch {
        case e: Exception =>
          println("Invalid input. Please enter a valid number.")
      }
    }
  }
}
