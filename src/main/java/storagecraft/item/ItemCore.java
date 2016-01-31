package storagecraft.item;

import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemCore extends ItemBase
{
	public static final int TYPE_CONSTRUCTION = 0;
	public static final int TYPE_DESTRUCTION = 1;

	public ItemCore()
	{
		super("core");

		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		for (int i = 0; i < 2; ++i)
		{
			list.add(new ItemStack(item, 1, i));
		}
	}
}
