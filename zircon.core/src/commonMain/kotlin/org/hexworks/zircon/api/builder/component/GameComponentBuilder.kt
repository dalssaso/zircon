package org.hexworks.zircon.api.builder.component

import org.hexworks.cobalt.datatypes.Maybe
import org.hexworks.zircon.api.component.builder.base.BaseComponentBuilder
import org.hexworks.zircon.api.component.data.ComponentMetadata
import org.hexworks.zircon.api.component.renderer.ComponentRenderer
import org.hexworks.zircon.api.data.Block
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.game.GameArea
import org.hexworks.zircon.api.game.GameComponent
import org.hexworks.zircon.internal.component.renderer.DefaultComponentRenderingStrategy
import org.hexworks.zircon.internal.component.renderer.NoOpComponentRenderer
import org.hexworks.zircon.internal.game.InternalGameArea
import org.hexworks.zircon.internal.game.impl.DefaultGameComponent
import kotlin.jvm.JvmStatic

@Suppress("UNCHECKED_CAST")
class GameComponentBuilder<T : Tile, B : Block<T>>(
        private var gameArea: Maybe<InternalGameArea<T, B>> = Maybe.empty())
    : BaseComponentBuilder<GameComponent<T, B>, GameComponentBuilder<T, B>>(NoOpComponentRenderer()) {

    fun withGameArea(gameArea: GameArea<T, B>) = also {
        require(gameArea is InternalGameArea<T, B>) {
            "The supplied game area does not implement the internal game area api."
        }
        this.gameArea = Maybe.of(gameArea)
        this.contentSize = gameArea.visibleSize.to2DSize()
    }

    override fun build(): DefaultGameComponent<T, B> {
        require(gameArea.isPresent) {
            "Can't build a game component without a game area."
        }
        val gameAreaSize = gameArea.get().visibleSize.to2DSize()
        require(contentSize == gameAreaSize) {
            "Can't build a game component with a size ($size) different from its game area's visible size ($gameAreaSize)."
        }
        return DefaultGameComponent(
                componentMetadata = ComponentMetadata(
                        relativePosition = position,
                        size = size,
                        componentStyleSet = componentStyleSet,
                        tileset = tileset),
                initialTitle = title,
                renderer = DefaultComponentRenderingStrategy(
                        decorationRenderers = decorationRenderers,
                        componentRenderer = componentRenderer as ComponentRenderer<GameComponent<T, B>>),
                gameArea = gameArea.get()).apply {
            colorTheme.map {
                theme = it
            }
        }
    }

    override fun createCopy() = newBuilder<T, B>().withProps(props.copy()).apply {
        gameArea.map {
            withGameArea(it)
        }
    }

    companion object {

        @JvmStatic
        fun <T : Tile, B : Block<T>> newBuilder(): GameComponentBuilder<T, B> {
            return GameComponentBuilder()
        }
    }
}
