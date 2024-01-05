package mobi.largeming.heapdetector

import android.app.Application
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val state: Flow<UiState> = flow {
        val apps = withContext(Dispatchers.IO) {
            listUserVisibleApps(application.packageManager)
        }
        emit(apps)
    }

    private fun listUserVisibleApps(
        packageManager: PackageManager
    ): UiState {
        val pm = packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val resolvedInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            pm.queryIntentActivities(mainIntent, 0)
        }

        val largeHeapApps = mutableListOf<AppInfo>()
        val normalApps = mutableListOf<AppInfo>()
        val installedApps = resolvedInfos
            .map { it.activityInfo.packageName }
            .toSet()
            .map { pm.getApplicationInfo(it, 0) }

        installedApps.forEach { app ->
            val largeHeap =
                app.flags and ApplicationInfo.FLAG_LARGE_HEAP == ApplicationInfo.FLAG_LARGE_HEAP
            if (largeHeap) {
                largeHeapApps.add(mapApp(app, packageManager))
            } else {
                normalApps.add(mapApp(app, packageManager))
            }
        }

        Log.d(
            "HeapDetector",
            "Total apps: ${installedApps.size}, largeHeap: ${largeHeapApps.size}, normal: ${normalApps.size}"
        )
        largeHeapApps.forEach { app ->
            Log.d("HeapDetector", "LargeHeap App: ${app.name}")
        }
        normalApps.forEach { app ->
            Log.d("HeapDetector", "NormalHeap App: ${app.name}")
        }

        return UiState(largeHeapApps.sortedBy { it.name }, normalApps.sortedBy { it.name })
    }

    private fun mapApp(
        info: ApplicationInfo,
        packageManager: PackageManager
    ): AppInfo {
        val name = info.loadLabel(packageManager).toString()
        return AppInfo(name, info)
    }
}

data class AppInfo(
    val name: String,
    val appInfo: ApplicationInfo
)

data class UiState(
    val largeHeapApps: List<AppInfo> = emptyList(),
    val normalHeapApps: List<AppInfo> = emptyList()
)