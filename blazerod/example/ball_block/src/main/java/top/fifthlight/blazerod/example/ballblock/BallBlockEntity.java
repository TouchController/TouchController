package top.fifthlight.blazerod.example.ballblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// Shamelessly copied from https://docs.fabricmc.net/zh_cn/develop/blocks/block-entities
public class BallBlockEntity extends BlockEntity {
    public BallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BALL_BLOCK_ENTITY, pos, state);
    }
}
