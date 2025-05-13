import scraperUtil.ARSOWeatherScraper

fun main() {
    println("Starting Closy data scraping...")
    
    // Weather data from ARSO
    println("\n===== ARSO WEATHER DATA =====")
    val weatherScraper = ARSOWeatherScraper()
    weatherScraper.scrapeWeatherData()
    
    // Clothing data from Zara
    
    // Clothing data from H&M
    
    println("\nScraping completed!")
}