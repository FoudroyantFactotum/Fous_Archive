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
package com.foudroyantfactotum.tool.structure.IStructure;

import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

public interface IStructureTE
{
    IBlockState getTransmutedBlock();

    int getRegHash();
    StructureBlock getMasterBlockInstance();
    BlockPos getMasterBlockLocation();

    void configureBlock(BlockPos local, int patternHash);
    BlockPos getLocal();
}
