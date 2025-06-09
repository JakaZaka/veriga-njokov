import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.MainScreen  // Change the import

@Composable
@Preview
fun App() {
    MaterialTheme {
        MainScreen()  // Change this line to use MainScreen instead
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Closy - Desktop Manager"
    ) {
        App()
    }
}
