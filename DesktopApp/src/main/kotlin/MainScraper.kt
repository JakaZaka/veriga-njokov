import scraperUtil.ARSOWeatherScraper
import scraperUtil.ZaraScraper
import scraperUtil.HMScraper

fun main() {
    println("Starting Closy data scraping...")
    
    // Weather data from ARSO
    println("\n===== ARSO WEATHER DATA =====")
    val weatherScraper = ARSOWeatherScraper()
    weatherScraper.scrapeWeatherData()
    
    // Clothing data from Zara
    println("\n===== ZARA CLOTHING DATA =====")
    val zaraScraper = ZaraScraper()
    zaraScraper.scrapeMensClothing()
    
    // Clothing data from H&M
    println("\n===== H&M CLOTHING DATA =====")
    val hmScraper = HMScraper()
    hmScraper.getWomenClothes()
    
    println("\nScraping completed!")
}