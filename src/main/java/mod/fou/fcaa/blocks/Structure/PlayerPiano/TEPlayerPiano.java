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
package mod.fou.fcaa.blocks.Structure.PlayerPiano;

import mod.fou.fcaa.blocks.Structure.TEStructureTemplate;
import mod.fou.fcaa.structure.registry.StructureDefinition;
import net.minecraft.util.EnumFacing;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TEPlayerPiano extends TEStructureTemplate
{
    public static final Executor e = Executors.newSingleThreadExecutor();

    public volatile float[] keyOffset = new float[88];
    public volatile boolean[] keyIsDown = new boolean[88];
    public volatile double songPos;

    public TEPlayerPiano()
    {
        //noop
    }

    public TEPlayerPiano(StructureDefinition sd, EnumFacing orientation, boolean mirror)
    {
        super(sd, orientation, mirror);
    }
}
