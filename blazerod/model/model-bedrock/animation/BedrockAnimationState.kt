package top.fifthlight.blazerod.model.bedrock.animation

import org.slf4j.LoggerFactory
import team.unnamed.mocha.MochaEngine
import team.unnamed.mocha.parser.ast.Expression
import team.unnamed.mocha.runtime.binding.JavaObjectBinding
import team.unnamed.mocha.runtime.standard.MochaMath
import team.unnamed.mocha.runtime.value.MutableObjectBinding
import top.fifthlight.blazerod.model.animation.AnimationContext
import top.fifthlight.blazerod.model.animation.AnimationState
import top.fifthlight.blazerod.model.bedrock.molang.context.QueryContext
import top.fifthlight.blazerod.model.bedrock.molang.context.YsmContext
import top.fifthlight.blazerod.model.bedrock.molang.value.MolangValue

class BedrockAnimationState(
    val context: AnimationContext,
    override val duration: Float,
    val startDelay: MolangValue,
    val loopDelay: MolangValue,
    val loopMode: AnimationLoopMode,
    animTimeUpdate: BedrockAnimation.AnimationTimeUpdate,
) : AnimationState {
    companion object {
        @Suppress("UnstableApiUsage")
        private val mathBinding = JavaObjectBinding.of(MochaMath::class.java, null, MochaMath())

        private val logger = LoggerFactory.getLogger(BedrockAnimationState::class.java)
    }

    private val engine = MochaEngine.create<AnimationContext?>(null) { builder ->
        builder.set("math", mathBinding)
        val variableBinding = MutableObjectBinding()
        builder.set("variable", variableBinding)
        builder.set("v", variableBinding)
        builder.set("query", QueryContext)
        builder.set("ysm", YsmContext)
    }

    fun evalExpressions(context: AnimationContext, expressions: List<Expression>): Double {
        try {
            engine.setEntity(context)
            return engine.eval(expressions)
        } catch (ex: Throwable) {
            logger.error("Error evaluating expressions", ex)
            return 0.0
        } finally {
            engine.setEntity(null)
        }
    }

    fun evalValue(context: AnimationContext, value: MolangValue) = when (value) {
        is MolangValue.Molang -> evalExpressions(context, value.molang).toFloat()
        is MolangValue.Plain -> value.value
    }

    // Animation state machine
    private sealed class State {
        abstract val startGameTick: Long
        abstract val startDeltaTick: Float

        abstract fun update(
            context: AnimationContext,
            state: BedrockAnimationState,
        ): State

        abstract fun getTime(
            context: AnimationContext,
            state: BedrockAnimationState,
        ): Float

        protected fun getDeltaTime(context: AnimationContext): Float {
            val deltaGameTick = context.getGameTick() - startGameTick
            val deltaDeltaTick = context.getDeltaTick() - startDeltaTick
            return (deltaGameTick + deltaDeltaTick) * AnimationContext.SECONDS_PER_TICK
        }

        protected fun addDeltaTime(time: Float): Pair<Long, Float> {
            val totalTicks = time / AnimationContext.SECONDS_PER_TICK
            val deltaGameTick = totalTicks.toLong()
            val deltaDeltaTick = totalTicks - deltaGameTick
            var finalGameTick = startGameTick + deltaGameTick
            var finalDeltaTick = startDeltaTick + deltaDeltaTick
            if (finalDeltaTick > 1f) {
                finalDeltaTick -= 1f
                finalGameTick++
            }
            return Pair(finalGameTick, finalDeltaTick)
        }

        data class WaitingStartDelay(
            override val startGameTick: Long,
            override val startDeltaTick: Float,
            val startDelay: Float,
        ) : State() {
            override fun update(
                context: AnimationContext,
                state: BedrockAnimationState,
            ): State {
                val deltaTime = getDeltaTime(context)
                return if (deltaTime >= startDelay) {
                    val (newGameTick, newDeltaTick) = addDeltaTime(startDelay)
                    Playing(
                        startGameTick = newGameTick,
                        startDeltaTick = newDeltaTick,
                    )
                } else {
                    this
                }
            }

            override fun getTime(
                context: AnimationContext,
                state: BedrockAnimationState,
            ) = 0f
        }

        data class Playing(
            override val startGameTick: Long,
            override val startDeltaTick: Float,
        ) : State() {
            override fun update(
                context: AnimationContext,
                state: BedrockAnimationState,
            ): State {
                val deltaTime = getDeltaTime(context)
                return if (deltaTime >= state.duration) {
                    val (newGameTick, newDeltaTick) = addDeltaTime(state.duration)
                    WaitingLoopDelay(
                        startGameTick = newGameTick,
                        startDeltaTick = newDeltaTick,
                        loopDelay = state.evalValue(context, state.loopDelay),
                    )
                } else {
                    this
                }
            }

            override fun getTime(
                context: AnimationContext,
                state: BedrockAnimationState,
            ) = getDeltaTime(context)
        }

        data class WaitingLoopDelay(
            override val startGameTick: Long,
            override val startDeltaTick: Float,
            val loopDelay: Float,
        ) : State() {
            override fun update(
                context: AnimationContext,
                state: BedrockAnimationState,
            ): State {
                val deltaTime = getDeltaTime(context)
                if (deltaTime < loopDelay) {
                    return this
                }
                val (newGameTick, newDeltaTick) = addDeltaTime(loopDelay)
                return when (state.loopMode) {
                    AnimationLoopMode.NO_LOOP -> EndOnTime(
                        startGameTick = newGameTick,
                        startDeltaTick = newDeltaTick,
                        time = 0f,
                    )

                    AnimationLoopMode.LOOP -> if (state.duration < 1e-2) {
                        EndOnTime(
                            newGameTick,
                            newDeltaTick,
                            time = state.duration,
                        )
                    } else {
                        Playing(
                            newGameTick,
                            newDeltaTick,
                        )
                    }

                    AnimationLoopMode.HOLD_ON_LAST_FRAME -> EndOnTime(
                        newGameTick,
                        newDeltaTick,
                        time = state.duration,
                    )
                }
            }

            override fun getTime(
                context: AnimationContext,
                state: BedrockAnimationState,
            ) = state.duration
        }

        data class EndOnTime(
            override val startGameTick: Long,
            override val startDeltaTick: Float,
            val time: Float,
        ) : State() {
            override fun update(
                context: AnimationContext,
                state: BedrockAnimationState,
            ) = this

            override fun getTime(
                context: AnimationContext,
                state: BedrockAnimationState,
            ) = time
        }
    }

    private var state: State = State.WaitingStartDelay(
        startGameTick = context.getGameTick(),
        startDeltaTick = context.getDeltaTick(),
        startDelay = evalValue(context, startDelay),
    )

    override val playing: Boolean
        get() = state is State.Playing

    private var time = 0f
    override fun updateTime(context: AnimationContext) {
        while (true) {
            val newState = state.update(context, this)
            if (newState == state) {
                break
            }
            state = newState
        }
        time = state.getTime(context, this)
    }

    override fun getTime() = time
}
