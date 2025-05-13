package scraperUtil

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

/**
 * Scraper for ARSO weather data
 */
class ARSOWeatherScraper {
    private val arsoUrl = "https://meteo.arso.gov.si/uploads/probase/www/observ/surface/text/sl/observationAms_si_latest.html"
    
    /**
     * Main function to scrape ARSO weather data
     */
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
                
                if (cells.size >= 8) {
                    // Extract data from cells
                    val location = cells[0].text()
                    val temperature = cells[1].text().replace("°C", "").trim()
                    val relativeHumidity = cells[2].text().replace("%", "").trim()
                    val windSpeed = cells[4].text().split(" ")[0]
                    val precipitation = cells[7].text().replace("mm", "").trim()
                    
                    // Convert to appropriate data types where possible
                    val temperatureValue = temperature.toDoubleOrNull() ?: Double.NaN
                    val isRaining = precipitation.toDoubleOrNull()?.let { it > 0.0 } ?: false
                    
                    // Print parsed data
                    println("Location: $location")
                    println("Temperature: $temperatureValue°C")
                    println("Is raining: $isRaining")
                    println("Humidity: $relativeHumidity%")
                    println("Wind speed: $windSpeed km/h")
                    println("-----------------------")
                    
                    // In future: Save to MongoDB using the Weather model
                    // Currently our model can accept location, temperature, isRaining
                }
            }
        } catch (e: Exception) {
            println("Error scraping weather data: ${e.message}")
            e.printStackTrace()
        }
    }
}