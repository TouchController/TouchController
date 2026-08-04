package top.fifthlight.combine.backend.minecraft.render.v26_3.extension;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;

public interface SubmittableGuiGraphics {
    void combine$addGuiElement(GuiElementRenderState guiElementRenderState);

    ScreenRectangle combine$peekScissorStack();
}
