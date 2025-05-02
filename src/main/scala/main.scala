import controller.PowerPlantController
import model.{SolarPanel, WindTurbine, HydroPower}
import service.DataAnalysis
import utils.{MyCSVReader, DataCollection}
import java.io.File
import scala.io.StdIn
import com.github.tototoshi.csv._

object MainApp {
  def main(args: Array[String]): Unit = {
    val solarData = MyCSVReader.readSolarData("data/Cleaned_Solar_Data.csv")
    val windData = MyCSVReader.readWindData("data/Cleaned_Wind_Data.csv")
    val hydroData = MyCSVReader.readHydroData("data/Cleaned_Hydro_Data.csv")

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

            // Call monitor
            println("\nSolar Panel Monitoring:")
            PowerPlantController.monitor(solarPanel, year, month, day, hourOpt)

            println("\nWind Turbine Monitoring:")
            PowerPlantController.monitor(windTurbine, year, month, day, hourOpt)

            println("\nHydro Power Monitoring:")
            PowerPlantController.monitor(hydroPlant, year, month, day, hourOpt)


          case 2 =>
            print("Enter a year that need calculate:")
            val year = scala.io.StdIn.readInt()

            val solarData = MyCSVReader.readSolarData("data/Cleaned_Solar_Data.csv")
            val windData = MyCSVReader.readWindData("data/Cleaned_Wind_Data.csv")
            val hydroData = MyCSVReader.readHydroData("data/Cleaned_Hydro_Data.csv")

            val solarPanel = SolarPanel("SP-001", solarData)
            val windTurbine = WindTurbine("WT-001", windData)
            val hydroPlant = HydroPower("HP-001", hydroData)

            val solarYearData = DataCollection.getYearData(solarPanel, year)
            val windYearData = DataCollection.getYearData(windTurbine, year)
            val hydroYearData = DataCollection.getYearData(hydroPlant, year)

            DataCollection.collectDataForYear(year, solarYearData, windYearData, hydroYearData)

            println(s"Data for the year $year has been collected and stored successfully.")


          case 3 =>
            try {
              // Scan data directory for yearly data files
              val dataDir = new File("data")
              val yearPattern = "Combined_Power_Data_(\\d{4})\\.csv".r
              val availableYears = dataDir.listFiles
                .filter(_.isFile)
                .map(_.getName)
                .flatMap(name => yearPattern.findFirstMatchIn(name).map(_.group(1).toInt))
                .sorted

              if (availableYears.isEmpty) {
                println("No data files found for any year.")
              } else {
                println("Available years:")
                availableYears.foreach(year => println(s"$year"))

                print("Enter the year to view: ")
                val selectedYear = scala.io.StdIn.readInt()
                
                if (availableYears.contains(selectedYear)) {
                  // Read and display data for the selected year
                  val filePath = s"data/Combined_Power_Data_$selectedYear.csv"
                  val reader = CSVReader.open( new File( filePath ) )
                  try {
                    val data = reader.allWithHeaders()
                    val total = data.map( _( "Power" ).toDouble ).sum
                    println( "Total: " + total.toString )
                  }
                  finally
                  {
                    reader.close()
                  }
                  val source = scala.io.Source.fromFile(filePath)
                  try {
                    val lines = source.getLines().toList
                    if (lines.nonEmpty) {
                      // Skip header line and process data
                      val data = lines.tail.map(_.split(","))
                      
                      // Calculate total power for each energy type
                      val solarPower = data.filter(_(5) == "Solar").map(_(4).toDouble).sum
                      val windPower = data.filter(_(5) == "Wind").map(_(4).toDouble).sum
                      val hydroPower = data.filter(_(5) == "Hydro").map(_(4).toDouble).sum

                      println(s"\nPower generation by source for year $selectedYear:")
                      println(f"Solar: $solarPower%.2f MW")
                      println(f"Wind: $windPower%.2f MW")
                      println(f"Hydro: $hydroPower%.2f MW")
                    }
                  } finally {
                    source.close()
                  }
                } else {
                  println(s"No data exists for year $selectedYear. Please try again.")
                }
              }
            } catch {
              case _: NumberFormatException =>
                println("Please enter a valid year number.")
              case e: Exception =>
                println(s"An error occurred: ${e.getMessage}")
            }

          case 4 =>
            try {
              val dataDir = new File("data")
              val yearPattern = "Combined_Power_Data_(\\d{4})\\.csv".r
              val availableYears = dataDir.listFiles
                .filter(_.isFile)
                .map(_.getName)
                .flatMap(name => yearPattern.findFirstMatchIn(name).map(_.group(1).toInt))
                .sorted

              if (availableYears.isEmpty) {
                println("No data files found for any year.")
                return
              }
              println("Available years:")
              availableYears.foreach(println)
              print("Enter the year to analyze: ")
              val year = scala.io.StdIn.readInt()

              val filePath = s"data/Combined_Power_Data_$year.csv"
              val records = DataAnalysis.loadData(filePath)
              println("Choose the way you want to filter(hour/day/week/month):")
              val periodType = StdIn.readLine().trim.toLowerCase
              periodType match {
                case "hour" =>
                  print("Enter hour (0-23): ")
                  val h = StdIn.readInt()
                  val filtered = DataAnalysis.filterByHour(records, h)
                  val agg = DataAnalysis.aggregate(filtered, "hour")
                  DataAnalysis.printAggregates(agg)

                case "day" =>
                  print("Enter month (1-12): ")
                  val m = StdIn.readInt()
                  print("Enter day (1-31): ")
                  val d = StdIn.readInt()
                  val filtered = DataAnalysis.filterByDay(records, year, m, d)
                  val agg = DataAnalysis.aggregate(filtered, "day")
                  DataAnalysis.printAggregates(agg)

                case "week" =>
                  print("Enter the week number of the year (1-53): ")
                  val w = StdIn.readInt()
                  val filtered = DataAnalysis.filterByWeek(records, year, w)
                  val agg = DataAnalysis.aggregate(filtered, "week")
                  DataAnalysis.printAggregates(agg)

                case "month" =>
                  print("Enter month (1-12): ")
                  val mo = StdIn.readInt()
                  val filtered = DataAnalysis.filterByMonth(records, mo)
                  val agg = DataAnalysis.aggregate(filtered, "month")
                  DataAnalysis.printAggregates(agg)

                case other =>
                  println(s"Unknown period '$other'. Please choose hour, day, week, or month.")

              }

            } catch {
              case e: NumberFormatException =>
                println("Please enter valid numbers")
              case e: IllegalArgumentException =>
                println(e.getMessage)
              case e: Exception =>
                println(s"An error occurred: ${e.getMessage}")
            }

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
