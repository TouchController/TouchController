package top.fifthlight.armorstand.util

import top.fifthlight.blazerod.model.formats.ModelFileLoaders
import top.fifthlight.blazerod.model.loader.ModelFileLoader

object ModelLoaders {
    private val loaders
        get() = ModelFileLoaders.loaders

    val modelExtensions by lazy {
        loaders
            .flatMap { it.extensions.entries }
            .mapNotNull { (extension, abilities) ->
                extension.takeIf { ModelFileLoader.Ability.MODEL in abilities }
            }
    }

    val animationExtensions by lazy {
        loaders
            .flatMap { it.extensions.entries }
            .mapNotNull { (extension, abilities) ->
                extension.takeIf { ModelFileLoader.Ability.EXTERNAL_ANIMATION in abilities }
            }
    }

    val scanExtensions by lazy {
        modelExtensions + animationExtensions
    }

    val embedThumbnailExtensions by lazy {
        loaders
            .flatMap { it.extensions.entries }
            .mapNotNull { (extension, abilities) ->
                extension.takeIf { ModelFileLoader.Ability.EMBED_THUMBNAIL in abilities }
            }
    }

    val markerFileNames by lazy {
        loaders.flatMap { it.markerFiles.keys }.map { it.lowercase() }.toSet()
    }

    val markerFileLoaders by lazy {
        buildMap {
            loaders.forEach { loader ->
                loader.markerFiles.forEach { (markerFile, abilities) ->
                    put(markerFile.lowercase(), loader)
                }
            }
        }
    }
}
