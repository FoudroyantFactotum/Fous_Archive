/*
 * Fou's Archive - A Minecraft Mod
 * Copyright (c) 2016 Foudroyant Factotum
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foudroyantfactotum.mod.fousarchive.items;

import com.foudroyantfactotum.mod.fousarchive.library.ModBlocks;
import com.foudroyantfactotum.mod.fousarchive.TheMod;
import com.foudroyantfactotum.tool.structure.block.StructureBlock;
import com.foudroyantfactotum.tool.structure.item.StructureFormTool;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemTuningFork extends StructureFormTool
{
    private static final ImmutableList<StructureBlock> validStructures = ImmutableList.of(ModBlocks.playerPiano);

    @Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        final EnumFacing[] orientation = orientationPriority[MathHelper.floor_double(player.rotationYaw * 4.0f / 360.0f + 0.5) & 3];
        final boolean[] mirror = mirrorPriority[player.isSneaking()?1:0];

        doSearch(world, pos, orientation, mirror, validStructures);

        stack.damageItem(2, player);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public int getMaxDamage()
    {
        return 10;
    }

    @Override
    public boolean isDamageable()
    {
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving)
    {
        stack.damageItem(getMaxDamage()*2, entityLiving);
        return false;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker)
    {
        stack.damageItem(getMaxDamage()*2, attacker);
        return false;
    }

    @Override
    public String getUnlocalizedName()
    {
        final String unloc = super.getUnlocalizedName();

        return TheMod.MOD_ID + ":" + unloc.substring(unloc.indexOf('.') + 1);
    }

    public String getUnlocalizedName(ItemStack stack)
    {
        return getUnlocalizedName();
    }

    @Override
    public int getItemStackLimit()
    {
        return 1;
    }
}
