package mod.akrivus.kagic.entity.gem;

import java.util.HashMap;
import java.util.List;

import mod.akrivus.kagic.entity.EntityGem;
import mod.akrivus.kagic.entity.ai.EntityAIFollowDiamond;
import mod.akrivus.kagic.entity.ai.EntityAIStandGuard;
import mod.akrivus.kagic.entity.ai.EntityAIStay;
import mod.akrivus.kagic.init.KAGIC;
import mod.akrivus.kagic.init.ModAchievements;
import mod.akrivus.kagic.init.ModEnchantments;
import mod.akrivus.kagic.init.ModItems;
import mod.akrivus.kagic.init.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsTarget;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityBismuth extends EntityGem {
	public static final HashMap<Block, Double> BISMUTH_YIELDS = new HashMap<Block, Double>();
	public static final HashMap<Integer, ResourceLocation> BISMUTH_HAIR_STYLES = new HashMap<Integer, ResourceLocation>();
	public EntityBismuth(World worldIn) {
		super(worldIn);
		this.isImmuneToFire = true;
		this.setSize(0.9F, 2.3F);
		
		//Define valid gem cuts and placements
		this.setCutPlacement(GemCuts.BISMUTH, GemPlacements.BACK_OF_HEAD);
		this.setCutPlacement(GemCuts.BISMUTH, GemPlacements.BACK);
		this.setCutPlacement(GemCuts.BISMUTH, GemPlacements.CHEST);
		this.setCutPlacement(GemCuts.BISMUTH, GemPlacements.BELLY);

		// Apply entity AI.
		this.stayAI = new EntityAIStay(this);
        this.tasks.addTask(1, new EntityAIFollowDiamond(this, 1.0D));
        this.tasks.addTask(2, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(3, new EntityAIMoveTowardsTarget(this, 0.414D, 32.0F));
        this.tasks.addTask(3, new EntityAIAttackMelee(this, 1.0d, true));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 16.0F));
        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityMob.class, 16.0F));
        this.tasks.addTask(5, new EntityAIStandGuard(this, 0.6D));
        this.tasks.addTask(6, new EntityAILookIdle(this));
        
        // Apply target AI.
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false, new Class[0]));
        
        // Apply entity attributes.
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(18.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.4D);
        this.droppedGemItem = ModItems.BISMUTH_GEM;
		this.droppedCrackedGemItem = ModItems.CRACKED_BISMUTH_GEM;
	}


	public void convertGems(int placement) {
    	this.setGemCut(GemCuts.BISMUTH.id);
    	switch (placement) {
    	case 0:
    		this.setGemPlacement(GemPlacements.CHEST.id);
    		break;
    	}
    }
	
	/*********************************************************
     * Methods related to entity interaction.                *
     *********************************************************/
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (!this.world.isRemote) {
			if (hand == EnumHand.MAIN_HAND) {
				ItemStack stack = player.getHeldItemMainhand();
				if (this.isTamed()) {
					if (this.isOwner(player)) {
						if (this.isCoreItem(stack)) {
							return super.processInteract(player, hand);
						}
						else if (stack.isItemStackDamageable()) {
							if (stack.isItemDamaged()) {
								int damage = stack.getItemDamage() - stack.getMaxDamage();
								stack.damageItem(damage, this);
								if (!stack.isItemEnchanted() && stack.isItemEnchantable() && this.rand.nextInt(300) == 0) {
									List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(this.rand, stack, damage / 10, true);
									for (int i = 0; i < list.size(); ++i) {
				                        EnchantmentData data = (EnchantmentData) list.get(i);
				                        stack.addEnchantment(data.enchantmentobj, data.enchantmentLevel);
				                    }
									if (list.size() > 1) {
										if (this.rand.nextInt(30) == 0) {
											if (this.rand.nextInt(90) == 0) {
												stack.addEnchantment(ModEnchantments.BREAKING_POINT, 1);
											}
											else {
												stack.addEnchantment(ModEnchantments.FAIR_FIGHT, 1);
											}
										}
									}
								}
							}
						}
						else {
							ItemStack result = smeltItem(stack);
							if (!result.isEmpty()) {
								if (player.inventory.getFirstEmptyStack() > -1) {
									player.inventory.addItemStackToInventory(result);
								}
								else {
									this.entityDropItem(result, 0.0F);
								}
								if (result.getItem() == ModItems.ACTIVATED_GEM_BASE) {
									this.getOwner().addStat(ModAchievements.GEM_FORGER);
								}
								if (!player.capabilities.isCreativeMode) {
									stack.shrink(1);
								}
								this.getOwner().addStat(ModAchievements.TO_THE_FORGE);
								return true;
							}
						}
					}
				}
			}
		}
		return super.processInteract(player, hand);
    }
	public ItemStack smeltItem(ItemStack stack) {
		ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack).copy();
		return result;
    }
	
	/*********************************************************
     * Methods related to entity death.                      *
     *********************************************************/
	public void onDeath(DamageSource cause) {
		if (!this.world.isRemote) {
			if (cause.getEntity() instanceof EntityLivingBase) {
				ItemStack heldItem = ((EntityLivingBase)cause.getEntity()).getHeldItemMainhand();
				if (heldItem.isItemEnchanted()) {
					NBTTagList enchantments = heldItem.getEnchantmentTagList();
					for (int i = 0; i < enchantments.tagCount(); i++) {
						if (enchantments.getCompoundTagAt(i).getInteger("id") == Enchantment.getEnchantmentID(ModEnchantments.BREAKING_POINT)) {
							this.dropItem(ModItems.RECORD_THE_BREAKING_POINT, 1);
						}
					}
				}
			}
		}
		super.onDeath(cause);
    }
	
	/*********************************************************
     * Methods related to entity sounds.                     *
     *********************************************************/
	public SoundEvent getHurtSound() {
		return ModSounds.BISMUTH_HURT;
	}
	public SoundEvent getObeySound() {
		return ModSounds.BISMUTH_OBEY;
	}
	public SoundEvent getDeathSound() {
		return ModSounds.BISMUTH_DEATH;
	}
}
