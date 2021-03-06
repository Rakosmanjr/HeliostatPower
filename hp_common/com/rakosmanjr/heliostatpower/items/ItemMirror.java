/**
 * HeliostatPower
 *
 * @file ItemMirror.java
 *
 * @author rakosmanjr
 * @License Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 *
 */
package com.rakosmanjr.heliostatpower.items;

import com.rakosmanjr.heliostatpower.lib.Strings;

public class ItemMirror extends ItemHeliostat
{
	public ItemMirror(int id)
	{
		super(id);
		setUnlocalizedName(Strings.MIRROR_NAME);
	}
}
