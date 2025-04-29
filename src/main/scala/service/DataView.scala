import java.io.PrintWriter
import utils.ChartData

object DataView {

  def generateHTML(chartData: String): Unit = {
    val htmlContent =
      s"""
         |<!DOCTYPE html>
         |<html lang="en">
         |<head>
         |  <meta charset="UTF-8">
         |  <meta name="viewport" content="width=device-width, initial-scale=1.0">
         |  <title>Power Generation Curve</title>
         |  <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
         |  <script type="text/javascript">
         |    google.charts.load('current', {'packages':['corechart', 'line']});
         |    google.charts.setOnLoadCallback(drawChart);
         |
         |    function drawChart() {
         |      $chartData
         |
         |      var options = {
         |        title: 'Power Generation Curve (2022)',
         |        curveType: 'function',
         |        legend: { position: 'bottom' },
         |        hAxis: { title: 'Date' },
         |        vAxis: { title: 'Power (kW)' }
         |      };
         |
         |      var chart = new google.visualization.LineChart(document.getElementById('curve_chart'));
         |      chart.draw(data, options);
         |    }
         |  </script>
         |</head>
         |<body>
         |  <h1>Power Generation Curve for 2022</h1>
         |  <div id="curve_chart" style="width: 900px; height: 500px"></div>
         |</body>
         |</html>
       """.stripMargin

    val writer = new PrintWriter("html/power_generation_curve.html")
    writer.write(htmlContent)
    writer.close()

    println("HTML file generated successfully!")
  }
  def main(arg: Array[String]):Unit={
    val data = ChartData.loadData("data/Combined_Power_Data_2022.csv")
    val aggregatedData = ChartData.aggregatePowerData(data)

    val chartData = ChartData.generateGoogleChartData(aggregatedData)

    generateHTML(chartData)
  }
}
