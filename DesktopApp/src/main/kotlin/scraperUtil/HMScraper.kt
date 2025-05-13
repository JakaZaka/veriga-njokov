package scraperUtil

import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.By
import java.time.Duration




class HMScraper {
    fun getWomenClothes() {

        val options = EdgeOptions().apply {
            addArguments("start-maximized")
            addArguments("disable-blink-features=AutomationControlled")
        }


        System.setProperty("webdriver.edge.driver", "C:\\Users\\djela\\tools\\edgedriver_win64\\msedgedriver.exe")

        val driver = EdgeDriver(options)

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))
            driver.get("https://www2.hm.com/en_eur/ladies/shop-by-product/h-m-edition.html")

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
                val colourElements = articleElement.findElements(By.cssSelector("div.f84092.c75e9a.d7c999 ul > li"))
                val colours = mutableListOf<String>()
                for (colourElement in colourElements) {
                    colours.add(colourElement.findElement(By.tagName("a")).text)
                }


                println("$title - $category")
                println("Colours:")
                colours.forEach { println("$it") }
            }
            for ((title,link) in productLinks){
                driver.get(link)
                println("Title: $title Link: $link")
                val imageElements = driver.findElements(
                    By.cssSelector("ul.d36894 li img")
                )

                val imageUrls = imageElements.map { it.getAttribute("src") }

                imageUrls.forEach { println("Image URL: $it") }

            }

        } finally {
            driver.quit()
        }
    }
}

