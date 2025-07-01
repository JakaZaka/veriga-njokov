import scraperUtil.ARSOWeatherScraper
import scraperUtil.HMScraper
import scraperUtil.scrapeZara
fun main() {
    println("Starting Closy data scraping...")

    // Weather data from ARSO (commented out)
    // println("\n===== ARSO WEATHER DATA =====")
    // val weatherScraper = ARSOWeatherScraper()
    // weatherScraper.scrapeWeatherData()

    // Clothing data from H&M (commented out)
    // println("\n===== H&M CLOTHING DATA =====")
    // val hmScraper = HMScraper()
    // hmScraper.getWomenClothes()
    // hmScraper.getMenClothes()

    // Zara data scraping
    println("\n===== ZARA CLOTHING DATA =====")
    val baseUrls = listOf(
        "https://www.zara.com/si/sl/woman-trousers-l1335.html?v1=2420795",
        "https://www.zara.com/si/sl/woman-tops-l1322.html?v1=2419940",
        "https://www.zara.com/si/sl/man-tshirts-l855.html?v1=2432042",
        "https://www.zara.com/si/sl/man-jeans-l659.html?v1=2432131"
    )
    for (url in baseUrls) {
        println("Scraping URL: $url")
        scrapeZara(url)
    }

    println("\nScraping completed!")
}
