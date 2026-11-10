package net.wurstclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.wurstclient.WurstClient;
import net.wurstclient.hacks.XRayHack;

@Pseudo
@Mixin(targets = "org.embeddedt.embeddium.impl.render.chunk.compile.tasks.ChunkBuilderMeshingTask",
	remap = false)
public class EmbeddiumMeshingMixin
{
	@Unique
	private static final ThreadLocal<BlockPos.MutableBlockPos> wurst_blockPos =
		ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

	@WrapOperation(method = "execute(Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildContext;Lorg/embeddedt/embeddium/impl/util/task/CancellationToken;)Lorg/embeddedt/embeddium/impl/render/chunk/compile/ChunkBuildOutput;",
		at = @At(value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/world/WorldSlice;getBlockState(III)Lnet/minecraft/world/level/block/state/BlockState;"))
	private BlockState wurst_filterBlock(@Coerce Object slice, int x, int y,
		int z, Operation<BlockState> original)
	{
		BlockState state = original.call(slice, x, y, z);
		WurstClient wurst = WurstClient.INSTANCE;
		if(state.isAir() || !wurst.isEnabled())
			return state;

		XRayHack xray = wurst.getHax().xRayHack;
		if(xray.isEnabled()
			&& !xray.isVisible(state.getBlock(), wurst_blockPos.get().set(x, y, z)))
			return Blocks.AIR.defaultBlockState();

		return state;
	}
}
