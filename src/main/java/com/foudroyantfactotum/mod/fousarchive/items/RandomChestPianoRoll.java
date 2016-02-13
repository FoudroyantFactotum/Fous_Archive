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
package com.foudroyantfactotum.mod.fousarchive.items;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandomChestContent;

import java.util.Random;

public class RandomChestPianoRoll extends WeightedRandomChestContent
{
    public RandomChestPianoRoll()
    {
        super(new ItemStack(ItemPianoRoll.INSTANCE), 1, 1, 42);
    }

    @Override
    protected ItemStack[] generateChestContent(Random random, IInventory newInventory)
    {
        final ItemStack stack = new ItemStack(ItemPianoRoll.INSTANCE, 1);
        final NBTTagCompound nbt = new NBTTagCompound();

        ItemPianoRoll.setPianoRollNBT(nbt, ItemPianoRoll.getPianoRoll((int) (ItemPianoRoll.getPianoRollCount()*random.nextDouble())).toString());
        stack.setTagCompound(nbt);

        return new ItemStack[]{stack};
    }
}