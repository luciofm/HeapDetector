package mobi.largeming.heapdetector

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mobi.largeming.heapdetector.ui.theme.HeapDetectorTheme
import mobi.largeming.heapdetector.ui.theme.Typography

@OptIn(ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HeapDetectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.state.collectAsState(UiState())
                    if (state.largeHeapApps.isEmpty() && state.normalHeapApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        HeapDetectorContent(state)
                    }
                }
            }
        }
    }

    @Composable
    private fun HeapDetectorContent(state: UiState) {
        LazyColumn {
            stickyHeader {
                Text(
                    text = "  Large Heap Apps",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                )
            }

            items(items = state.largeHeapApps) { app ->
                AppColumn(app, "Large Heap")
            }
            stickyHeader {
                Text(
                    text = "  Normal Heap Apps",
                    style = Typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                )
            }
            items(items = state.normalHeapApps) { app ->
                AppColumn(app, "Normal Heap")
            }
        }
    }

    @Composable
    private fun AppColumn(info: AppInfo, type: String) {
        val context = LocalContext.current
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(64.dp)
                .clickable {
                    Toast
                        .makeText(context, type, Toast.LENGTH_SHORT)
                        .show()
                }
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            AsyncImage(model = info, contentDescription = "", modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = info.name)
        }
    }
}
