package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;

@Mixin(SectionCompiler.class)
public class SectionCompilerMixin
{
	@WrapOperation(
		method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState wurst_filterBlock(RenderChunkRegion region, BlockPos pos,
		Operation<BlockState> original)
	{
		BlockState state = original.call(region, pos);
		WurstClient wurst = WurstClient.INSTANCE;
		XRayHack xray = wurst.getHax().xRayHack;
		if(!state.isAir() && wurst.isEnabled() && xray.isEnabled()
			&& !xray.isOpacityMode() && !xray.isVisible(state.getBlock(), pos))
			return Blocks.AIR.defaultBlockState();

		return state;
	}

	@WrapOperation(
		method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
		at = @At(value = "INVOKE",
		target = "Lnet/minecraft/client/resources/model/BakedModel;getRenderTypes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;)Lnet/neoforged/neoforge/client/ChunkRenderTypeSet;"))
	private ChunkRenderTypeSet wurst_renderTypes(BakedModel model,
		BlockState state, RandomSource random, ModelData data,
		Operation<ChunkRenderTypeSet> original)
	{
		if(WurstClient.INSTANCE.getHax().xRayHack.isOpacityMode())
			return ChunkRenderTypeSet.of(RenderType.translucent());

		return original.call(model, state, random, data);
	}
}
