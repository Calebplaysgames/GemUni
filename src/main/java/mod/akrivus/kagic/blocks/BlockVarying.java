package mod.akrivus.kagic.blocks;

import mod.akrivus.kagic.init.ModCreativeTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class BlockVarying extends Block {
	public BlockVarying(String unlocalizedName, int resistance, int hardness, int level) {
		super(Material.ROCK, MapColor.PURPLE);
		this.setUnlocalizedName(unlocalizedName);
		this.setCreativeTab(ModCreativeTabs.CREATIVE_TAB_OTHER);
		this.setHardness(hardness);
		this.setResistance(resistance);
		this.setHarvestLevel("pickaxe", level);
	}
}
