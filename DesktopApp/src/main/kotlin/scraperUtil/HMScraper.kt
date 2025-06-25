package scraperUtil

import models.ScrapedClothingItem
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Point
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration




class HMScraper {
    // Update the getWomenClothes method to return results instead of just printing
    fun getWomenClothes(limit: Int = 30): List<ScrapedClothingItem> {
        val scrapedItems = mutableListOf<ScrapedClothingItem>()

        val options = EdgeOptions().apply {
            addArguments("start-maximized")
            addArguments("disable-blink-features=AutomationControlled")
        }


        System.setProperty("webdriver.edge.driver", "C:\\jakazaka\\FERI\\edgedriver_win64\\msedgedriver.exe")

        val driver = EdgeDriver(options)
        driver.manage().window().setSize(Dimension(800, 600))
        driver.manage().window().setPosition(Point(-2000, 0))
        val wait = WebDriverWait(driver, Duration.ofSeconds(60))

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))
            driver.get("https://www2.hm.com/en_us/women/new-arrivals/view-all.html")

            val pages = driver.findElements(By.cssSelector("nav[aria-labelledby='pagination-accessibility-label'] ol > li"))
            val secondToLastLi = pages[pages.size - 2]
            val anchor = secondToLastLi.findElement(By.tagName("a"))
            val pageNumber = anchor.text
            val pageRange = pageNumber.toInt()
            outerLoop@ for (i in 1..pageRange) {  // Label the outer loop
                driver.get("https://www2.hm.com/en_us/women/new-arrivals/view-all.html?page=$i")
                val products = driver.findElements(By.cssSelector("ul[data-elid='product-grid'] > li"))
                val productLinks = mutableMapOf<String, Pair<String, String>>()


                for (product in products) {
                    val title = product.findElement(By.cssSelector("h2")).text
                    val articleElement = product.findElement(By.cssSelector("article"))
                    val newPage = product.findElement(By.cssSelector("a"))
                    val link = newPage.getAttribute("href")
                    val category = articleElement.getAttribute("data-category")
                    
                    // Store both link and category
                    productLinks[title] = Pair(link, category ?: "unknown")
                    

                }
                for ((title, linkCategoryPair) in productLinks) {
                    // Process only what we need
                    if (scrapedItems.size >= limit) {
                        break@outerLoop  // Break out of both loops
                    }
                    
                    val link = linkCategoryPair.first
                    val category = linkCategoryPair.second
                    
                    driver.get(link)
                    val colorSection = driver.findElement(
                        By.cssSelector("section[data-testid='color-selector']")
                    )

                    val color = colorSection.findElement(By.cssSelector("p")).text

                    val img = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("section[data-testid='color-selector'] img")
                        )
                    )

                    (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", img)

                    val photoLink = wait.until {
                        val src = img.getAttribute("src")
                        if (src != null && !src.startsWith("data:image")) {
                            src.substringBefore("?")
                        } else {
                            null
                        }
                    }

                    // Instead of just printing, add to the list:
                    scrapedItems.add(
                        ScrapedClothingItem(
                            name = title,
                            category = category,
                            imageUrl = photoLink ?: "",
                            color = color ?: "unknown", // Handle nullable color
                            link = link
                        )
                    )
                    
                    println("Scraped ${scrapedItems.size}/${limit} items")
                }
            }

        } finally {
            driver.quit()
        }

        return scrapedItems.take(limit)
    }

    fun getMenClothes() {
        val options = EdgeOptions().apply {
            addArguments("start-maximized")
            addArguments("disable-blink-features=AutomationControlled")
            //addArguments("--headless=new")
            //addArguments("window-size=1920,1080")
        }


        System.setProperty("webdriver.edge.driver", "C:\\Users\\djela\\tools\\edgedriver_win64\\msedgedriver.exe")

        val driver = EdgeDriver(options)
        driver.manage().window().setSize(Dimension(800, 600))
        driver.manage().window().setPosition(Point(-2000, 0))
        val wait = WebDriverWait(driver, Duration.ofSeconds(60))

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))
            driver.get("https://www2.hm.com/en_us/men/new-arrivals/view-all.html")

            val pages = driver.findElements(By.cssSelector("nav[aria-labelledby='pagination-accessibility-label'] ol > li"))
            val secondToLastLi = pages[pages.size - 2]
            val anchor = secondToLastLi.findElement(By.tagName("a"))
            val pageNumber = anchor.text
            val pageRange = pageNumber.toInt()
            for (i in 1..pageRange) {
                driver.get("https://www2.hm.com/en_us/men/new-arrivals/view-all.html?page=$i")
                val products = driver.findElements(By.cssSelector("ul[data-elid='product-grid'] > li"))
                val productLinks = mutableMapOf<String, String>()


                for (product in products) {
                    val title = product.findElement(By.cssSelector("h2")).text
                    val articleElement = product.findElement(By.cssSelector("article"))
                    val newPage = product.findElement(By.cssSelector("a"))
                    val link = newPage.getAttribute("href")
                    //val code = articleElement.getAttribute("data-articlecode")
                    productLinks[title] = link

                    val category = articleElement.getAttribute("data-category")


                    println("$title - $category")

                }
                for ((title, link) in productLinks) {
                    driver.get(link)
                    println("Title: $title Link: $link")
                    val colorSection = driver.findElement(
                        By.cssSelector("section[data-testid='color-selector']")
                    )

                    val color = colorSection.findElement(By.cssSelector("p")).text

                    val img = wait.until(
                        ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("section[data-testid='color-selector'] img")
                        )
                    )

                    (driver as JavascriptExecutor).executeScript("arguments[0].scrollIntoView(true);", img)

                    val photoLink = wait.until {
                        val src = img.getAttribute("src")
                        if (src != null && !src.startsWith("data:image")) {
                            src.substringBefore("?")
                        } else {
                            null
                        }
                    }
                    println("Image URL: $photoLink Color: $color")

                }
            }

        } finally {
            driver.quit()
        }
    }
}

