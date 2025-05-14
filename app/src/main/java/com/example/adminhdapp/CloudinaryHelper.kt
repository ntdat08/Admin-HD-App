import android.content.Context
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import java.util.HashMap

object CloudinaryHelper {
    private const val CLOUD_NAME = "dt8rs7d8v"
    private const val API_KEY = "758659865958178"
    private const val API_SECRET = "1yjSTgFr5OtT2rI6Sf4-73kYGpM"

    fun initialize(context: Context) {
        try {
            MediaManager.get()
        }
        catch(ex : Exception) {
            val config = HashMap<String, String>()
            config["cloud_name"] = CLOUD_NAME
            config["api_key"] = API_KEY
            config["api_secret"] = API_SECRET
            "secure" to true
            MediaManager.init(context, config)
        }
    }

    fun uploadImage(
        filePath: String,
        folderName: String = "menu_images",
        callback: UploadCallback
    ) {
        MediaManager.get().upload(filePath)
            .option("folder", folderName)
            .callback(callback)
            .dispatch()
    }
}