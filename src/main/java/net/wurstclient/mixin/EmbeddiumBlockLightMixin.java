package net.wurstclient.mixin;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.LightTexture;
import net.wurstclient.WurstClient;

@Pseudo
@Mixin(targets = "org.embeddedt.embeddium.impl.render.chunk.compile.pipeline.BlockRenderer",
	remap = false)
public class EmbeddiumBlockLightMixin
{
	@Inject(method = "getVertexLight", at = @At("RETURN"))
	private void wurst_lightXRayBlocks(
		CallbackInfoReturnable<EmbeddiumQuadLightAccessor> cir)
	{
		// Embeddium's X-Ray meshing hook already removes unselected blocks.
		if(!WurstClient.INSTANCE.getHax().xRayHack.isRenderingFullbright())
			return;

		EmbeddiumQuadLightAccessor light = cir.getReturnValue();
		Arrays.fill(light.wurst_getBrightness(), 1F);
		Arrays.fill(light.wurst_getLightmap(), LightTexture.FULL_BRIGHT);
	}
}
