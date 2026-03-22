package top.fifthlight.blazerod.render.common.runtime.load

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.future
import top.fifthlight.blazerod.render.api.loader.ModelLoader
import top.fifthlight.blazerod.model.Model
import top.fifthlight.blazerod.render.common.util.dispatchers.BlazeRod
import top.fifthlight.blazerod.render.version_1_21_8.runtime.RenderSceneImpl
import top.fifthlight.mergetools.api.ActualConstructor
import top.fifthlight.mergetools.api.ActualImpl

@ActualImpl(ModelLoader::class)
object ModelLoaderImpl : ModelLoader {
    @JvmStatic
    @ActualConstructor("create")
    fun create() = this

    override suspend fun loadModel(model: Model): RenderSceneImpl? = coroutineScope {
        val loadInfo = ModelPreprocessor.preprocess(
            scope = this,
            loadDispatcher = Dispatchers.Default,
            model = model,
        ) ?: return@coroutineScope null
        val gpuInfo = ModelResourceLoader.load(
            scope = this,
            gpuDispatcher = Dispatchers.BlazeRod.Main,
            info = loadInfo,
        )
        SceneReconstructor.reconstruct(info = gpuInfo)
    }

    override fun loadModelAsFuture(model: Model) = CoroutineScope(Dispatchers.Default).future {
        loadModel(model)
    }
}
