package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

@Pseudo
@Mixin(targets = "org.embeddedt.embeddium.impl.model.light.data.QuadLightData",
	remap = false)
public interface EmbeddiumQuadLightAccessor
{
	@Accessor("br")
	float[] wurst_getBrightness();

	@Accessor("lm")
	int[] wurst_getLightmap();
}
