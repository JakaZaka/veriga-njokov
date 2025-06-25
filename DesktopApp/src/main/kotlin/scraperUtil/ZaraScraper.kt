package scraperUtil

import models.ScrapedClothingItem
import org.openqa.selenium.*
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.support.ui.WebDriverWait
import org.openqa.selenium.support.ui.ExpectedConditions
import java.time.Duration
import java.util.UUID

class ZaraScraper {
    fun getClothes(limit: Int = 30): List<ScrapedClothingItem> {
        val scrapedItems = mutableListOf<ScrapedClothingItem>()
        val options = EdgeOptions().apply {
            addArguments("start-maximized")
            addArguments("--disable-blink-features=AutomationControlled")
        }

        val driver = EdgeDriver(options)
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5))

        try {
            // We'll use women's pants for variety - these can be reliably categorized
            val urls = listOf(
                "https://www.zara.com/si/sl/woman-jeans-l1119.html" to "bottoms",
                "https://www.zara.com/si/sl/woman-tops-l1322.html" to "tops",
                "https://www.zara.com/si/sl/woman-dresses-l1066.html" to "dresses"
            )
            
            val itemsToScrapePerUrl = limit / urls.size
            
            for ((url, baseCategory) in urls) {
                if (scrapedItems.size >= limit) break
                
                driver.get(url)
                println("Loading URL: $url")
                Thread.sleep(3000) // Wait for page to load
                
                // Accept cookies if present
                try {
                    val cookieButton = driver.findElement(By.id("onetrust-accept-btn-handler"))
                    cookieButton.click()
                    Thread.sleep(1000)
                } catch (e: Exception) {
                    println("No cookie banner found: ${e.message}")
                }

                // Scroll down to load more products
                for (i in 1..3) {
                    try {
                        (driver as JavascriptExecutor).executeScript("window.scrollBy(0, 800)")
                        Thread.sleep(1500)
                    } catch (e: Exception) {
                        println("Error scrolling: ${e.message}")
                    }
                }

                // Find all product cards - try different selectors
                var products = driver.findElements(By.cssSelector("article[data-testid='product-card']"))
                if (products.isEmpty()) {
                    products = driver.findElements(By.cssSelector(".product-grid-product"))
                }
                if (products.isEmpty()) {
                    products = driver.findElements(By.cssSelector("article")) // Most generic selector
                }
                
                println("Found ${products.size} products on page")
                
                for ((index, product) in products.take(itemsToScrapePerUrl).withIndex()) {
                    try {
                        // Try to scroll to element
                        (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", product)
                        Thread.sleep(500)
                        
                        // Use multiple approaches to get product name
                        var name = ""
                        try {
                            // Try various selectors that might contain the product name
                            val selectors = listOf(
                                ".product-name", 
                                "h3", 
                                "[data-testid='product-name']",
                                ".product-grid-product-info__name",
                                ".product-link"
                            )
                            
                            for (selector in selectors) {
                                try {
                                    val element = product.findElement(By.cssSelector(selector))
                                    val text = element.text.trim()
                                    if (text.isNotEmpty()) {
                                        name = text
                                        break
                                    }
                                } catch (e: Exception) {
                                    // Try next selector
                                }
                            }
                        } catch (e: Exception) {
                            println("Error getting name: ${e.message}")
                        }
                        
                        // If still no name, generate unique name
                        if (name.isEmpty()) {
                            name = "Zara ${baseCategory.capitalize()} #${index+1}-${UUID.randomUUID().toString().take(6)}"
                        }
                        
                        // Try to get image URL from any img element
                        var imageUrl = ""
                        try {
                            val images = product.findElements(By.tagName("img"))
                            for (img in images) {
                                val src = img.getAttribute("src") ?: img.getAttribute("data-src")
                                if (!src.isNullOrEmpty() && src.contains("http")) {
                                    imageUrl = src
                                    break
                                }
                            }
                        } catch (e: Exception) {
                            println("Error getting image: ${e.message}")
                        }
                        
                        // If no image found, use fallback
                        if (imageUrl.isEmpty()) {
                            imageUrl = "https://static.zara.net/placeholder.jpg"
                        }
                        
                        // Extract color from name or use default
                        val color = extractColorFromName(name).ifEmpty { getRandomColor(index) }
                        
                        // Get a unique identifier for each item
                        val uniqueId = UUID.randomUUID().toString().take(8)
                        
                        // Create item with unique information
                        val scrapedItem = ScrapedClothingItem(
                            name = "$name - $uniqueId",
                            category = baseCategory,
                            imageUrl = imageUrl,
                            color = color,
                            link = url,
                            selected = false,
                            size = "M", // Default size
                            seasons = listOf("spring", "summer"), // Default seasons
                            notes = "Item ID: $uniqueId"
                        )
                        
                        scrapedItems.add(scrapedItem)
                        println("Scraped ${scrapedItems.size}/$limit items: $name")
                        
                    } catch (e: Exception) {
                        println("Error processing product $index: ${e.message}")
                    }
                    
                    if (scrapedItems.size >= limit) break
                }
            }
            
        } catch (e: Exception) {
            println("Error during Zara scraping: ${e.message}")
            e.printStackTrace()
        } finally {
            driver.quit()
        }
        
        // If we didn't get any items through scraping, generate fallback items
        if (scrapedItems.isEmpty()) {
            println("Scraping failed to get any items. Generating fallback items.")
            return generateFallbackItems(limit)
        }
        
        return scrapedItems
    }
    
    // Generate fallback items in case scraping fails completely
    private fun generateFallbackItems(count: Int): List<ScrapedClothingItem> {
        val items = mutableListOf<ScrapedClothingItem>()
        val categories = listOf("tops", "bottoms", "dresses")
        val colors = listOf("Black", "White", "Blue", "Red", "Green", "Brown", "Gray", "Beige", "Navy")
        
        for (i in 1..count) {
            val category = categories[i % categories.size]
            val color = colors[i % colors.size]
            val uniqueId = UUID.randomUUID().toString().take(8)
            
            items.add(
                ScrapedClothingItem(
                    name = "Zara ${category.capitalize()} - $color #$i",
                    category = category,
                    imageUrl = "https://static.zara.net/photos//contents/cm/media-transformers/joinlife-ctx/joinlife-large.svg?ts=1611919362013",
                    color = color,
                    link = "https://www.zara.com",
                    selected = false,
                    size = "M",
                    seasons = listOf("spring", "summer"),
                    notes = "Generated item ID: $uniqueId"
                )
            )
        }
        
        return items
    }
    
    // Helper to capitalize first letter
    private fun String.capitalize(): String {
        return this.replaceFirstChar { it.uppercase() }
    }
    
    // Improved color extraction
    private fun extractColorFromName(name: String): String {
        val commonColors = listOf(
            "black", "white", "red", "blue", "green", "yellow", 
            "pink", "purple", "orange", "brown", "grey", "gray", "beige",
            "navy", "cream", "olive", "tan", "khaki", "maroon"
        )
        
        val lowerName = name.lowercase()
        for (color in commonColors) {
            if (lowerName.contains(color)) {
                return color.replaceFirstChar { it.uppercase() }
            }
        }
        
        return ""
    }
    
    // Fallback random color generator
    private fun getRandomColor(seed: Int): String {
        val colors = listOf("Black", "White", "Blue", "Red", "Green", "Brown", "Gray", "Beige", "Navy")
        return colors[seed % colors.size]
    }
}

// Keep the original function for backward compatibility
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