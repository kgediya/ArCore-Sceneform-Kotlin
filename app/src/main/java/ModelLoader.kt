import android.net.Uri
import android.util.Log
import com.google.ar.core.Anchor
import com.google.ar.sceneform.rendering.ModelRenderable
import com.krunalraj.arcoreplayground.MainActivity
import java.lang.ref.WeakReference

class ModelLoader internal constructor(owner:WeakReference<MainActivity>) {
    private val owner: WeakReference<MainActivity>
    init{
        this.owner = owner
    }
    internal fun loadModel(anchor: Anchor, uri: Uri) {
        if (owner.get() == null)
        {
            Log.d(TAG, "Activity is null. Cannot load model.")
            return
        }
        ModelRenderable.builder()
            .setSource(owner.get(), uri)
            .build()
            .handle({ renderable, throwable->
                val activity = owner.get()
                if (activity == null)
                {
null
                }
                else if (throwable != null)
                {
                    activity.onException(throwable)
                }
                else
                {
                    activity.addNodeToScene(anchor, renderable)
                }
                null })
        return
    }
    companion object {
        private val TAG = "ModelLoader"
    }
}