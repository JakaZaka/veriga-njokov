package scraperUtil

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

/**
 * Scraper for ARSO weather data
 */
class ARSOWeatherScraper {
    private val arsoUrl = "https://meteo.arso.gov.si/uploads/probase/www/observ/surface/text/sl/observationAms_si_latest.html"

    fun scrapeWeatherData() {
        println("Fetching weather data from ARSO...")
        
        try {
            // Connect to the ARSO website and get the HTML document
            val doc: Document = Jsoup.connect(arsoUrl).get()
            
            // Extract the weather data table
            val table: Elements = doc.select("table.meteoSI-table")
            val rows: Elements = table.select("tr")
            
            // Skip the header row
            for (i in 1 until rows.size) {
                val row = rows[i]
                val cells = row.select("td")
                
                if (cells.size >= 9) {
                    // Extract data from cells using correct indices (based on debugging)
                    val location = cells[0].text()
                    val temperature = cells[2].text().trim() // Temperature is in cell[2]
                    val humidity = cells[3].text().trim() // Humidity is in cell[3]
                    val windSpeed = if (cells[5].text().isNotBlank()) cells[5].text().trim() else ""
                    val precipitation = if (cells[8].text().isNotBlank()) cells[8].text().trim() else "0"
                    
                    // Convert to appropriate data types
                    val temperatureValue = temperature.toDoubleOrNull() ?: Double.NaN
                    val isRaining = precipitation.toDoubleOrNull()?.let { it > 0.0 } ?: false
                    
                    // Print parsed data
                    println("Location: $location")
                    println("Temperature: ${temperatureValue}°C")
                    println("Is raining: $isRaining")
                    println("Humidity: $humidity%")
                    println("Wind speed: $windSpeed km/h")
                    println("-----------------------")
                    
                    // In future: Save to MongoDB using the Weather model
                    // Example code to save (not executed now):
                    /*
                    val weatherData = Weather(
                        location = location,
                        temperature = temperatureValue,
                        isRaining = isRaining
                    )
                    // Save to database
                    */
                }
            }
        } catch (e: Exception) {
            println("Error scraping weather data: ${e.message}")
            e.printStackTrace()
        }
    }
}