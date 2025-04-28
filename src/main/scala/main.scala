import java.io.{BufferedWriter, FileWriter, PrintWriter}
import scala.util.{Try, Random}

case class EnergyRecord(timestamp: String, source: String, value: Double)

object DataCollection {
  val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")

  def collectData(numRecords: Int): Seq[EnergyRecord] = {
    val energySources = Seq("Solar", "Wind", "Hydro")
    val now = java.time.LocalDateTime.now()

    (0 until numRecords).map { i =>
      val timestamp = now.minusMinutes(i.toLong).format(dateFormatter)
      val source = energySources(Random.nextInt(energySources.length))
      val value = BigDecimal(Random.nextDouble() * 1000).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      EnergyRecord(timestamp, source, value)
    }
  }
}

object DataStorage {
  def writeDataToCsv(records: Seq[EnergyRecord], filePath: String): Unit = {
    val writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))
    try {
      writer.println("timestamp,source,value")
      records.foreach { record =>
        writer.println(s"${record.timestamp},${record.source},${record.value}")
      }
      println(s"数据已保存到文件: $filePath")
    } finally {
      writer.close()
    }
  }
}

object MainApp {
  def main(args: Array[String]): Unit = {

    println("开始收集数据...")
    val data = DataCollection.collectData(100)

    // 2. 保存数据到 CSV 文件
    println("将数据保存到文件...")
    DataStorage.writeDataToCsv(data, "energy_data.csv")

    println("模拟错误输入: 无效的日期格式")
    val invalidDateInput = "2024-04-31" // 无效日期

    Try(java.time.LocalDate.parse(invalidDateInput, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))) match {
      case scala.util.Success(_) => println("日期格式有效")
      case scala.util.Failure(exception) => println(s"错误: 日期格式无效, 请输入正确的日期格式")
    }

    // 5. 提示用户
    println("Exiting..")
  }
}
