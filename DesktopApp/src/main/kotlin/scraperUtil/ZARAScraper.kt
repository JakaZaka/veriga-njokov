import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions

fun main() {
    val options = EdgeOptions().apply {
        addArguments("start-maximized")
        addArguments("--disable-blink-features=AutomationControlled")
        addArguments("--remote-allow-origins=*")
    }

    val driver = EdgeDriver(options)

    try {
        driver.get("https://www.zara.com/si/sl/woman-tshirts-l1362.html")

        // Example: wait and print product names
        Thread.sleep(5000)

        val products = driver.findElements(org.openqa.selenium.By.cssSelector(".product-grid-product-info__name"))
        for (product in products) {
            println(product.text)
        }

    } finally {
        driver.quit()
    }
}
