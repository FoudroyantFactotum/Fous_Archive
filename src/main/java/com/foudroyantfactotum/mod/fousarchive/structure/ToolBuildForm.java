/*
 * Copyright (c) 2016 Foudroyant Factotum
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.foudroyantfactotum.mod.fousarchive.structure;

import com.foudroyantfactotum.mod.fousarchive.blocks.Structure.BlockStructure;
import com.foudroyantfactotum.mod.fousarchive.structure.IStructure.IPartBlockState;
import com.foudroyantfactotum.mod.fousarchive.structure.net.StructurePacket;
import com.foudroyantfactotum.mod.fousarchive.structure.net.StructurePacketOption;
import com.foudroyantfactotum.mod.fousarchive.structure.registry.StructureDefinition;
import com.foudroyantfactotum.mod.fousarchive.structure.registry.StructureRegistry;
import com.foudroyantfactotum.mod.fousarchive.utility.Log.Logger;
import com.foudroyantfactotum.mod.fousarchive.utility.ModNetwork;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.BlockPos.MutableBlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.foudroyantfactotum.mod.fousarchive.structure.coordinates.TransformLAG.*;

public class ToolBuildForm extends Item
{
    private static final ExecutorService pool = Executors.newFixedThreadPool(5);
    private static final EnumFacing[][] orientationPriority = {
            {EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.WEST}, //south
            {EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH}, //west
            {EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST}, //north
            {EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST}, //east
    };
    private static final boolean[][] mirrorPriority = {
            {false, true},
            {true, false},
    };

    public ToolBuildForm()
    {
        setUnlocalizedName(getUnlocalizedName() + "_form");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote || player == null)
        {
            return true;
        }

        final EnumFacing[] orientation = orientationPriority[MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5) & 3];
        final boolean[] mirror = mirrorPriority[player.isSneaking()?1:0];
        final List<Future<SearchResult>> searchJobFuture = new ArrayList<>(StructureRegistry.getStructureList().size());

        //search currently ignores multiple matches and take the first match available.
        for (final BlockStructure sb : StructureRegistry.getStructureList())
        {
            searchJobFuture.add(pool.submit(new SearchJob(sb, world, pos, orientation, mirror)));
        }

        SearchResult result = null;

        for (final Future<SearchResult> res : searchJobFuture)
        {
            try
            {
                if (result == null)
                {
                    result = res.get();
                } else {
                    res.cancel(true);
                }
            }
            catch (InterruptedException | ExecutionException e)
            {
                Logger.info(e.getMessage());
            }
        }

        searchJobFuture.clear();

        if (result != null)
        {
            final IBlockState state = result.block.getDefaultState()
                    .withProperty(BlockDirectional.FACING, result.orientation)
                    .withProperty(BlockStructure.MIRROR, result.mirror);


            world.setBlockState(result.origin, state, 0x2);
            result.block.formStructure(world, result.origin, state, 0x2);

            BlockStructure.updateExternalNeighbours(world, result.origin, result.block.getPattern(), result.orientation, result.mirror, true);

            ModNetwork.network.sendToAllAround(
                    new StructurePacket(result.origin, result.block.getRegHash(), result.orientation, result.mirror, StructurePacketOption.BUILD),
                    new NetworkRegistry.TargetPoint(world.provider.getDimensionId(), result.origin.getX(), result.origin.getY(), result.origin.getZ(), 30)
            );
        }

        return true;
    }

    /**
     * Performs complete search on world at the location
     */
    private static class SearchJob implements Callable<SearchResult>
    {
        final BlockStructure ssBlock;
        final World world;
        final BlockPos pos;

        final EnumFacing[] orientationOrder;
        final boolean[] mirrorOrder;

        SearchJob(BlockStructure ssBlock, World world, BlockPos pos, EnumFacing[] orientationOrder, boolean[] mirrorOrder)
        {
            this.ssBlock = ssBlock;
            this.world = world;
            this.pos = pos;
            this.orientationOrder = orientationOrder;
            this.mirrorOrder = mirrorOrder;
        }

        @Override
        public SearchResult call() throws Exception
        {
            final StructureDefinition sd = ssBlock.getPattern();
            final BlockPos tl = sd.getToolFormLocation();

            nextOrientation:
            for (final EnumFacing o: orientationOrder)
            {
                nextMirror:
                for (final boolean mirror : mirrorOrder)
                {
                    final BlockPos origin =
                            localToGlobal(
                                    -tl.getX(), -tl.getY(), -tl.getZ(),
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    o, mirror, sd.getBlockBounds()
                            );

                    for (final MutableBlockPos local : sd.getStructureItr())
                    {
                        final IPartBlockState pb = sd.getBlock(local);
                        final IBlockState b = pb.getBlockState();

                        //alter local coord var and changes it to world coord.
                        mutLocalToGlobal(local, origin, o, mirror, sd.getBlockBounds());

                        final IBlockState ncwb = world.getBlockState(local);
                        final IBlockState wb = ncwb.getBlock().getActualState(ncwb, world, local);

                        if (b != null && (b.getBlock() != wb.getBlock() || !doBlockStatesMatch(pb, localToGlobal(b, o, mirror), wb)))
                        {
                            if (mirror == mirrorOrder[1]) //is last mirror
                            {
                                continue nextOrientation;
                            }
                            else
                            {
                                continue nextMirror;
                            }
                        }
                    }

                    //found match, eeek!
                    final SearchResult result = new SearchResult();

                    result.block = ssBlock;
                    result.origin = origin;
                    result.orientation = o;
                    result.mirror = mirror;

                    return result;
                }
            }

            //no matches for this structure
            return null;
        }
    }

    /***
     * final result struct, used to return result from the search.
     */
    private static final class SearchResult
    {
        public BlockStructure block;
        public EnumFacing orientation;
        public boolean mirror;
        public BlockPos origin;
    }
}