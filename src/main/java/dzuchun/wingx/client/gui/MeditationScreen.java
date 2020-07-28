package dzuchun.wingx.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import dzuchun.wingx.Wingx;
import dzuchun.wingx.client.render.gui.SeparateRenderers;
import dzuchun.wingx.client.render.overlay.FadingScreenOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(value = Dist.CLIENT)
public class MeditationScreen extends Screen {

	private static final ResourceLocation HUD = new ResourceLocation(Wingx.MOD_ID,
			"textures/gui/meditation/meditation_hud.png");
	private static final Vector4f BACKGROUND_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

	public MeditationScreen(ITextComponent titleIn) {
		super(titleIn);
		this.minecraft = Minecraft.getInstance();
	}

	@Override
	public void render(MatrixStack matrixStackIn, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
		this.renderBackground(matrixStackIn, 0);
		this.minecraft.getTextureManager().bindTexture(HUD);
		SeparateRenderers.myBlit(matrixStackIn, 0, 0, this.width, this.height, 0.0f, 0.0f, 1.0f, 1.0f, this.minecraft,
				HUD);
		String nodeName = I18n.format("wingx.gui.node.name");
		StringTextComponent nodeNameComponent = new StringTextComponent(nodeName);
		nodeNameComponent
				.func_230530_a_(Style.EMPTY.setBold(true).setColor(Color.func_240744_a_(TextFormatting.GREEN)));
		matrixStackIn.push();
		float scale = 1f;
		matrixStackIn.scale(scale, scale, scale);
		this.font.func_238407_a_(matrixStackIn, nodeNameComponent,
				this.width / scale - this.font.func_238414_a_(nodeNameComponent), 0.0f / scale, 0xFFFFFFFF);
		matrixStackIn.pop();
		super.render(matrixStackIn, p_230430_2_, p_230430_3_, p_230430_4_);
	}

	@Override
	public void renderBackground(MatrixStack matrixStackIn, int p_238651_2_) {
		if (this.minecraft.world != null) {
			SeparateRenderers.renderColorScreen(matrixStackIn, BACKGROUND_COLOR);
			net.minecraftforge.common.MinecraftForge.EVENT_BUS
					.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, matrixStackIn));
		} else {
			this.renderDirtBackground(p_238651_2_);
		}

	}

	@SuppressWarnings("unused")
	private Button goIntoButton;

	@Override
	protected void init() {
		super.init();
		this.addButton(goIntoButton = new Button(this.width - 100 - 10, this.height - 20 - 2, 100, 20,
				new TranslationTextComponent("wingx.gui.gointo"), (Button button) -> {
					// TODO go into :)
				}));
	}

	@Override
	public void onClose() {
		new FadingScreenOverlay(FadingScreenOverlay.Color.BLACK, FadingScreenOverlay.Color.ZERO, 10).activate();
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
