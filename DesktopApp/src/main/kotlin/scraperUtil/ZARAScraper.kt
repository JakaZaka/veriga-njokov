import org.openqa.selenium.*
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import java.time.Duration

fun scrapeZara(url: String) {
    val options = EdgeOptions().apply {
        addArguments("start-maximized")
        addArguments("--disable-blink-features=AutomationControlled")
    }

    val driver = EdgeDriver(options)
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5))

    try {
        driver.get(url)

        val wait = WebDriverWait(driver, Duration.ofSeconds(10))
        var previousUrl = driver.currentUrl
        var currentUrl: String
        var page = 1

        while (true) {
            println("Processing page: $page")

            // Switch to view 3
            try {
                val viewButton = wait.until(
                    ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//span[contains(@class, 'view-option-selector-button__option') and text()='3']")
                    )
                )
                (driver as JavascriptExecutor).executeScript("arguments[0].click();", viewButton)
                Thread.sleep(3000)
            } catch (e: Exception) {
                println("Error switching to view 3: ${e.message}")
                break
            }

            // Scroll down incrementally
            try {
                (driver as JavascriptExecutor).executeScript("window.scrollBy(0, 500);")
                Thread.sleep(3000)
            } catch (e: Exception) {
                println("Error during scrolling: ${e.message}")
            }

            try {
                val products = driver.findElements(By.cssSelector("img.media-image__image.media__wrapper--media"))
                for (product in products) {

                    (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", product)
                    Thread.sleep(500)

                    val imageUrl = product.getAttribute("data-src") ?: product.getAttribute("src")
                    val productName = product.getAttribute("alt") ?: "Unknown product name"

                    println("Product Name: $productName")
                    println("Image URL: $imageUrl")
                    println("--------------------------")
                }
            } catch (e: Exception) {
                println("Error reading images and names from the grid: ${e.message}")
            }

            currentUrl = driver.currentUrl
            if (currentUrl == previousUrl) {
                println("No more pages to load.")
                break
            }
            previousUrl = currentUrl
            page++
        }

    } finally {
        driver.quit()
    }
}

fun main() {
    val baseUrls = listOf(
        // WOMEN
        "https://www.zara.com/si/sl/woman-trousers-l1335.html?v1=2420795", // BOTTOMS (TROUSERS)
        "https://www.zara.com/si/sl/woman-tops-l1322.html?v1=2419940", // TOPS
        "https://www.zara.com/si/sl/woman-tshirts-l1362.html?v1=2420417", // TOPS (T-SHIRTS)
        "https://www.zara.com/si/sl/woman-shirts-l1217.html?v1=2420369", // TOPS (SHIRTS)
        "https://www.zara.com/si/sl/woman-blazers-l1055.html?v1=2420942", // TOPS (BLAZERS)
        "https://www.zara.com/si/sl/woman-jeans-l1119.html?v1=2419185", // BOTTOMS (JEANS)
        "https://www.zara.com/si/sl/woman-trousers-shorts-l1355.html?v1=2420480", // BOTTOMS (SHORTS)
        "https://www.zara.com/si/sl/woman-skirts-l1299.html?v1=2420454", // BOTTOMS (SKIRTS)
        "https://www.zara.com/si/sl/woman-cardigans-sweaters-l8322.html?v1=2419844", // TOPS (SWEATERS)
        "https://www.zara.com/si/sl/woman-jackets-l1114.html?v1=2417772", // OUTERWEAR (JACKETS)
        "https://www.zara.com/si/sl/woman-outerwear-l1184.html?v1=2419032", // OUTERWEAR (COATS)
        "https://www.zara.com/si/sl/woman-shoes-l1251.html?v1=2419160", // SHOES
        "https://www.zara.com/si/sl/woman-accessories-l1003.html?v1=2418989", // ACCESSORIES
        "https://www.zara.com/si/sl/woman-bags-l1024.html?v1=2417728", // ACCESSORIES (BAGS)

        // MEN
        "https://www.zara.com/si/sl/man-tshirts-l855.html?v1=2432042", // TOPS (T-SHIRTS)
        "https://www.zara.com/si/sl/man-shirts-l737.html?v1=2431994", // TOPS (SHIRTS)
        "https://www.zara.com/si/sl/man-bermudas-l592.html?v1=2432164", // BOTTOMS (BERMUDAS)
        "https://www.zara.com/si/sl/man-trousers-l838.html?v1=2432096", // BOTTOMS (TROUSERS)
        "https://www.zara.com/si/sl/man-jeans-l659.html?v1=2432131", // BOTTOMS (JEANS)
        "https://www.zara.com/si/sl/man-knitwear-l681.html?v1=2432265", // TOPS (KNITWEAR)
        "https://www.zara.com/si/sl/man-sweatshirts-l821.html?v1=2432232", // TOPS (SWEATSHIRTS)
        "https://www.zara.com/si/sl/man-jackets-l640.html?v1=2467336", // OUTERWEAR (JACKETS)
        "https://www.zara.com/si/sl/man-outerwear-l715.html?v1=2432200", // OUTERWEAR (COATS)
        "https://www.zara.com/si/sl/man-shoes-sneakers-l797.html?v1=2436325", // SHOES (SNEAKERS)
        "https://www.zara.com/si/sl/man-shoes-l769.html?v1=2436382", // SHOES (OTHER)
        "https://www.zara.com/si/sl/man-bags-l563.html?v1=2436405", // ACCESSORIES (BAGS)
        "https://www.zara.com/si/sl/man-accessories-l537.html?v1=2436444" // ACCESSORIES
    )

    for (url in baseUrls) {
        println("Scraping URL: $url")
        scrapeZara(url)
    }
}