/**
 * HeliostatPower
 *
 * @file BlockBasicIonicCompressor.java
 *
 * @author rakosmanjr
 * @License Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 *
 */
package com.rakosmanjr.heliostatpower.block;

import com.rakosmanjr.heliostatpower.HeliostatPower;
import com.rakosmanjr.heliostatpower.lib.GuiIds;
import com.rakosmanjr.heliostatpower.lib.Strings;
import com.rakosmanjr.heliostatpower.tileentity.TileBasicIonicCompressor;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockBasicIonicCompressor extends BlockHeliostat
{
	public BlockBasicIonicCompressor(int id)
	{
		super(id, Material.iron);
		
		setUnlocalizedName(Strings.IONIC_COMPRESSOR_NAME);
		setCreativeTab(HeliostatPower.tabsHP);
		setHardness(5F);
		setBlockBounds(0, 0, 0, 1, 1, 1);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileBasicIonicCompressor();
	}
	
	@Override
	public boolean renderAsNormalBlock()
	{
		return true;
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta)
	{
		super.breakBlock(world, x, y, z, id, meta);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int par6, float par7, float par8, float par9)
	{
		if (!world.isRemote && !player.isSneaking())
		{
			TileBasicIonicCompressor tileCompressor = (TileBasicIonicCompressor)world
					.getBlockTileEntity(x, y, z);
			
			if (tileCompressor != null)
			{
				player.openGui(HeliostatPower.instance,
						GuiIds.IONIC_COMPRESSOR_ID, world, x, y, z);
			}
		}
		
		return true;
	}
}
