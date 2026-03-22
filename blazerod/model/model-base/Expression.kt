package top.fifthlight.blazerod.model

sealed class Expression {
    abstract val name: String?
    abstract val tag: Tag?

    enum class Tag(
        val vrm0Name: String? = null,
        val vrm1Name: String? = null,
        val pmxEnglish: String? = null,
        val pmxJapanese: String? = null,
    ) {
        NEUTRAL(
            vrm0Name = "neutral",
            vrm1Name = "neutral",
        ),
        HAPPY(
            vrm0Name = "Joy",
            vrm1Name = "happy",
            pmxJapanese = "にこり",
        ),
        ANGRY(
            vrm0Name = "Angry",
            vrm1Name = "angry",
            pmxJapanese = "怒り",
        ),
        SAD(
            vrm0Name = "Sorrow",
            vrm1Name = "sad",
            pmxJapanese = "困る",
        ),
        RELAXED(
            vrm0Name = "Fun",
            vrm1Name = "relaxed",
        ),
        SURPRISED(
            vrm1Name = "surprised",
        ),
        AA(
            vrm0Name = "A",
            vrm1Name = "aa",
            pmxJapanese = "あ",
        ),
        EE(
            vrm0Name = "E",
            vrm1Name = "ee",
            pmxJapanese = "え",
        ),
        IH(
            vrm0Name = "I",
            vrm1Name = "ih",
            pmxJapanese = "い",
        ),
        OH(
            vrm0Name = "O",
            vrm1Name = "oh",
            pmxJapanese = "お",
        ),
        OU(
            vrm0Name = "U",
            vrm1Name = "ou",
            pmxJapanese = "う",
        ),
        BLINK(
            vrm0Name = "Blink",
            vrm1Name = "blink",
            pmxJapanese = "まばたき",
        ),
        BLINK_LEFT(
            vrm0Name = "Blink_L",
            vrm1Name = "blinkLeft",
        ),
        BLINK_RIGHT(
            vrm0Name = "Blink_R",
            vrm1Name = "blinkRight",
        ),
        LOOK_UP(
            vrm0Name = "LookUp",
            vrm1Name = "lookUp",
        ),
        LOOK_DOWN(
            vrm0Name = "LookDown",
            vrm1Name = "lookDown",
        ),
        LOOK_LEFT(
            vrm0Name = "LookLeft",
            vrm1Name = "lookLeft",
        ),
        LOOK_RIGHT(
            vrm0Name = "LookRight",
            vrm1Name = "lookRight",
        );

        companion object {
            private val vrm0NameMap = entries
                .mapNotNull { it.takeIf { it.vrm0Name != null } }
                .associateBy { it.vrm0Name!!.lowercase() }

            private val vrm1NameMap = entries
                .mapNotNull { it.takeIf { it.vrm1Name != null } }
                .associateBy { it.vrm1Name!!.lowercase() }

            private val pmxJapaneseMap = entries
                .mapNotNull { it.takeIf { it.pmxJapanese != null } }
                .associateBy { it.pmxJapanese!!.lowercase() }

            private val pmxEnglishMap = entries
                .mapNotNull { it.takeIf { it.pmxEnglish != null } }
                .associateBy { it.pmxEnglish!!.lowercase() }

            fun fromVrm0Name(name: String): Tag? = vrm0NameMap[name.lowercase()]
            fun fromVrm1Name(name: String): Tag? = vrm1NameMap[name.lowercase()]
            fun fromPmxJapanese(name: String): Tag? = pmxJapaneseMap[name.lowercase()]
            fun fromPmxEnglish(name: String): Tag? = pmxEnglishMap[name.lowercase()]
        }
    }

    data class Target(
        override val name: String? = null,
        override val tag: Tag? = null,
        val isBinary: Boolean = false,
        val bindings: List<Binding>,
    ) : Expression() {
        sealed class Binding {
            data class NodeMorphTarget(
                val nodeId: NodeId,
                val index: Int,
                val weight: Float,
            ) : Binding()

            data class MeshMorphTarget(
                val meshId: MeshId,
                val index: Int,
                val weight: Float,
            ) : Binding()
        }
    }

    data class Group(
        override val name: String? = null,
        override val tag: Tag? = null,
        val targets: List<TargetItem>,
    ) : Expression() {
        data class TargetItem(
            val target: Target,
            val influence: Float,
        )
    }
}
