package service

import java.io.PrintWriter

object DataView {

  def generateHTML(chartData: String, year: Int): Unit = {
    val htmlContent =
      s"""
         |<!DOCTYPE html>
         |<html lang="en">
         |<head>
         |  <meta charset="UTF-8">
         |  <meta name="viewport" content="width=device-width, initial-scale=1.0">
         |  <title>Power Generation and Storage - $year</title>
         |  <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
         |  <script type="text/javascript">
         |    google.charts.load('current', {'packages':['corechart']});
         |    google.charts.setOnLoadCallback(drawChart);
         |
         |    function drawChart() {
         |      $chartData
         |
         |      var options = {
         |        title: 'Power Generation and Storage ($year)',
         |        hAxis: { title: 'Date' },
         |        vAxis: { title: 'Power (MW)' },
         |        seriesType: 'bars',
         |        series: {
         |          3: { type: 'line', color: 'green' }
         |        },
         |        legend: { position: 'bottom' }
         |      };
         |
         |      var chart = new google.visualization.ComboChart(document.getElementById('chart_div'));
         |      chart.draw(data, options);
         |    }
         |  </script>
         |</head>
         |<body>
         |  <h1>Power Generation and Storage Report - $year</h1>
         |  <div id="chart_div" style="width: 1000px; height: 600px;"></div>
         |</body>
         |</html>
       """.stripMargin

    val writer = new PrintWriter(s"html/power_generation_curve_$year.html")
    writer.write(htmlContent)
    writer.close()

    println(s"HTML report for $year generated successfully at html/power_generation_curve_$year.html")
  }
}
