/**
 * HeliostatPower
 *
 * @file TileBasicIonicCompressor.java
 *
 * @author rakosmanjr
 * @License Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 *
 */
package com.rakosmanjr.heliostatpower.tileentity;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.rakosmanjr.heliostatpower.items.crafting.CraftingIonicCompressor;
import com.rakosmanjr.heliostatpower.lib.Reference;
import com.rakosmanjr.heliostatpower.lib.Strings;

public class TileBasicIonicCompressor extends TileHeliostat implements
		ISidedInventory
{
	private final ItemStack[] inventory;
	
	private boolean craftingGridChanged;
	private boolean validRecipe;
	private boolean inCycle;
	
	private final int gridWidth = CraftingIonicCompressor.GRID_WIDTH;
	private final int gridHeight = CraftingIonicCompressor.GRID_HEIGHT;
	private final int gridTotal = CraftingIonicCompressor.GRID_TOTAL;
	private final int totalSlots = gridTotal + 2;
	
	private final int FUEL_SLOT = 15;
	private final int OUTPUT_SLOT = 16;
	
	// Cycle stuff
	// Applies to the current cycle
	private int recipeId; // recipeId for info lookup
	private int totalTickCount; // total ticks passed during cycle
	private int maxTickCount; // max ticks needed for cycle
	private int fuelConsumptionRate; // number of ticks to pass before consuming
										// fuel
	private int fuelConsumed; // fuel used every time fuel is consumed
	private ItemStack result; // result of the recipe
	private boolean gotFuel; // set to true when there is FIRST fuel in the fuel
								// slot
	private Status status; // status of the machine
	
	public TileBasicIonicCompressor()
	{
		inventory = new ItemStack[totalSlots];
		
		craftingGridChanged = true;
		validRecipe = false;
		inCycle = false;
		
		SetCustomName(Strings.TE_IONIC_COMPRESSOR);
	}
	
	@Override
	public void updateEntity()
	{
		if (craftingGridChanged)
		{
			validRecipe = CanProcess();
		}
		
		if (validRecipe || inCycle)
		{
			if (inCycle)
			{
				ContinueProcessingCycle();
			}
			else
			{
				StartProcessingCycle();
			}
		}
		else
		{
			status = Status.WaitingForRecipe;
		}
	}
	
	// Check if the pattern is valid for processing
	public boolean CanProcess()
	{
		recipeId = CraftingIonicCompressor.Instance().GetRecipeId(inventory);
		
		return recipeId < 0 ? false : true;
	}
	
	// Initiate a processing cycle
	public void StartProcessingCycle()
	{
		if (recipeId == -1)
		{
			validRecipe = false;
			return;
		}
		
		totalTickCount = 0;
		maxTickCount = CraftingIonicCompressor.Instance().GetMaxTick(recipeId);
		fuelConsumptionRate = CraftingIonicCompressor.Instance()
				.GetFuelConsumptionRate(recipeId);
		fuelConsumed = CraftingIonicCompressor.Instance().GetFuelConsumend(
				recipeId);
		result = CraftingIonicCompressor.Instance().GetResult(recipeId);
		
		inCycle = true;
		gotFuel = CheckFuel() > 0;
		
		// Remove components from the grid
		for (int x = 0; x < gridWidth; x++)
		{
			for (int y = 0; y < gridHeight; y++)
			{
				int slot = x * gridHeight + y;
				int count = CraftingIonicCompressor.Instance()
						.ComponentsUsedInSlot(recipeId, slot);
				
				if (count == -1)
				{
					continue;
				}
				
				decrStackSize(slot, count);
			}
		}
	}
	
	// Keep processing the current cycle
	public void ContinueProcessingCycle()
	{
		if (!gotFuel)
		{
			status = Status.WaitingForFuel;
			gotFuel = CheckFuel() > 0;
			return;
		}
		
		if (totalTickCount >= maxTickCount)
		{
			EndProcessingCycle();
			return;
		}
		else if (totalTickCount % fuelConsumptionRate == 0)
		{
			if (!UseFuel())
			{
				ForceEndProcessingCycle();
			}
		}
		
		status = Status.Processing;
		totalTickCount++;
	}
	
	// End the current processing cycle
	public void EndProcessingCycle()
	{
		if (inventory[OUTPUT_SLOT] == null)
		{
			inventory[OUTPUT_SLOT] = result.copy();
		}
		else if (inventory[OUTPUT_SLOT].isItemEqual(result)
				&& inventory[OUTPUT_SLOT].stackSize + result.stackSize <= inventory[OUTPUT_SLOT]
						.getMaxStackSize())
		{
			inventory[OUTPUT_SLOT].stackSize += result.stackSize;
		}
		else
		{
			// Hold the stack and wait...
			status = Status.OutputFull;
			return;
		}
		
		ForceEndProcessingCycle();
	}
	
	// Forcefully end the current processing cycle, destroying the current
	// processing items
	public void ForceEndProcessingCycle()
	{
		inCycle = false;
		totalTickCount = -1;
		maxTickCount = -1;
		fuelConsumptionRate = -1;
		fuelConsumed = -1;
		result = null;
	}
	
	// Removes one fuel from the fuel slot
	// Returns true if fuel was consumed
	public boolean UseFuel()
	{
		if (decrStackSize(FUEL_SLOT, fuelConsumed) == null)
		{
			return false;
		}
		
		return true;
	}
	
	// Returns the amount of fuel in the fuel slot
	public int CheckFuel()
	{
		return inventory[FUEL_SLOT] == null ? 0
				: inventory[FUEL_SLOT].stackSize;
	}
	
	public Status GetStatus()
	{
		return status;
	}
	
	@Override
	public int getSizeInventory()
	{
		return inventory.length;
	}
	
	@Override
	public ItemStack getStackInSlot(int i)
	{
		if (i < 0 || i >= inventory.length)
		{
			return null;
		}
		
		return inventory[i];
	}
	
	@Override
	public ItemStack decrStackSize(int i, int amount)
	{
		ItemStack itemstack = getStackInSlot(i);
		
		if (itemstack != null)
		{
			if (itemstack.stackSize <= amount)
			{
				setInventorySlotContents(i, null);
			}
			else
			{
				itemstack = itemstack.splitStack(amount);
				
				if (itemstack.stackSize == 0)
				{
					setInventorySlotContents(i, null);
				}
			}
		}
		
		return itemstack;
	}
	
	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		ItemStack itemstack = getStackInSlot(i);
		
		if (itemstack != null)
		{
			setInventorySlotContents(i, null);
		}
		
		return itemstack;
	}
	
	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		inventory[i] = itemstack;
		
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		
		if (i < gridWidth * gridHeight)
		{
			craftingGridChanged = true;
		}
	}
	
	@Override
	public String getInvName()
	{
		return GetCustomName();
	}
	
	@Override
	public boolean isInvNameLocalized()
	{
		return false;
	}
	
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer)
	{
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this
				&& entityPlayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5,
						zCoord + 0.5) < 64;
	}
	
	@Override
	public void openChest()
	{
		
	}
	
	@Override
	public void closeChest()
	{
		
	}
	
	@Override
	public boolean isStackValidForSlot(int i, ItemStack itemstack)
	{
		if (i == FUEL_SLOT)
		{
			Map<Item, Integer> fuels = CraftingIonicCompressor.Instance()
					.GetFuels();
			
			if (fuels.containsKey(itemstack.getItem()))
			{
				return true;
			}
		}
		else if (i == OUTPUT_SLOT)
		{
			if (Reference.DEBUG)
			{
				return true;
			}
		}
		else
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public int[] getSizeInventorySide(int var1)
	{
		return null;
	}
	
	@Override
	public boolean func_102007_a(int i, ItemStack itemstack, int j)
	{
		return false;
	}
	
	@Override
	public boolean func_102008_b(int i, ItemStack itemstack, int j)
	{
		return false;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
	}
}
