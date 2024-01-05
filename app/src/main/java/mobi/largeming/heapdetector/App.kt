package mobi.largeming.heapdetector

import android.app.Application
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.memory.MemoryCache
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class App : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val memoryCache = MemoryCache.Builder(applicationContext)
            .maxSizePercent(percent = 0.3)
            .build()
        return ImageLoader.Builder(applicationContext)
            .components {
                add(AppIconFetcher.Factory(applicationContext, memoryCache))
            }
            .memoryCache(memoryCache)
            .build()
    }
}

class AppIconFetcher(
    private val data: AppInfo,
    val context: Context,
    val memoryCache: MemoryCache
) : Fetcher {

    class Factory(private val application: Context, private val memoryCache: MemoryCache) :
        Fetcher.Factory<AppInfo> {
        override fun create(data: AppInfo, options: Options, imageLoader: ImageLoader): Fetcher {
            return AppIconFetcher(data, context = application, memoryCache = memoryCache)
        }

    }

    override suspend fun fetch(): FetchResult? {
        val pm = context.packageManager

        Log.d("HeapDetector", "Loading icon for: ${data.name}")
        return withContext(Dispatchers.IO) {
            memoryCache[MemoryCache.Key(data.appInfo.packageName)]?.let { cachedValue ->
                return@withContext DrawableResult(
                    drawable = BitmapDrawable(context.resources, cachedValue.bitmap),
                    isSampled = false,
                    dataSource = DataSource.MEMORY
                )
            }

            data.appInfo.loadIcon(pm)?.let { drawable ->
                memoryCache[MemoryCache.Key(data.appInfo.packageName)] =
                    MemoryCache.Value(drawable.toBitmap())
                DrawableResult(
                    drawable = drawable,
                    isSampled = false,
                    dataSource = DataSource.DISK
                )
            }
        }
    }
}
