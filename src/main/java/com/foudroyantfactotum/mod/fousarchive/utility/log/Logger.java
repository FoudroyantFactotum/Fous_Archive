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
package com.foudroyantfactotum.mod.fousarchive.utility.log;

import com.foudroyantfactotum.mod.fousarchive.TheMod;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

public class Logger
{
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(TheMod.MOD_ID);

    public static void info(final UserLogger ul, final String format, final Object... args)
    {
        if (ul.canDebug())
        {
            logger.log(Level.INFO, format, args);
        }
    }

    public static void fatal(final String format, final Object... args)
    {
        logger.log(Level.FATAL, format, args);
    }
}