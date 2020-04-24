package epicsquid.roots.spell;

import epicsquid.mysticallib.network.PacketHandler;
import epicsquid.mysticallib.util.Util;
import epicsquid.roots.Roots;
import epicsquid.roots.config.SpellConfig;
import epicsquid.roots.init.ModItems;
import epicsquid.roots.modifiers.instance.ModifierInstanceList;
import epicsquid.roots.network.fx.MessageSanctuaryBurstFX;
import epicsquid.roots.network.fx.MessageSanctuaryRingFX;
import epicsquid.roots.util.EntityUtil;
import epicsquid.roots.util.types.Property;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.oredict.OreIngredient;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpellSanctuary extends SpellBase {
  public static Property.PropertyCooldown PROP_COOLDOWN = new Property.PropertyCooldown(0);
  public static Property.PropertyCastType PROP_CAST_TYPE = new Property.PropertyCastType(EnumCastType.CONTINUOUS);
  public static Property.PropertyCost PROP_COST_1 = new Property.PropertyCost(0, new SpellCost("pereskia", 0.125));
  public static Property.PropertyCost PROP_COST_2 = new Property.PropertyCost(1, new SpellCost("wildroot", 0.125));
  public static Property<Float> PROP_VELOCITY = new Property<>("push_velocity", 0.125f).setDescription("the knockback speed at which entities are pushed away by the spell");
  public static Property<Integer> PROP_RADIUS_X = new Property<>("radius_x", 4).setDescription("the radius on the X axis of the area the spell in which the spell takes effect");
  public static Property<Integer> PROP_RADIUS_Y = new Property<>("radius_y", 5).setDescription("the radius on the Y axis of the area the spell in which the spell takes effect");
  public static Property<Integer> PROP_RADIUS_Z = new Property<>("radius_z", 4).setDescription("the radius on the Z axis of the area the spell in which the spell takes effect");

  public static ResourceLocation spellName = new ResourceLocation(Roots.MODID, "spell_sanctuary");
  public static SpellSanctuary instance = new SpellSanctuary(spellName);

  private float velocity;
  private int radius_x, radius_y, radius_z;

  private static Set<String> entitiesBlackList = new HashSet<>();

  public SpellSanctuary(ResourceLocation name) {
    super(name, TextFormatting.DARK_PURPLE, 208f / 255f, 16f / 255f, 80f / 255f, 224f / 255f, 32f / 255f, 144f / 255f);
    properties.addProperties(PROP_COOLDOWN, PROP_CAST_TYPE, PROP_COST_1, PROP_COST_2, PROP_VELOCITY, PROP_RADIUS_X, PROP_RADIUS_Y, PROP_RADIUS_Z);
  }

  @Override
  public void init() {
    addIngredients(
        new ItemStack(Items.ARMOR_STAND),
        new ItemStack(ModItems.pereskia),
        new ItemStack(Items.MILK_BUCKET),
        new ItemStack(ModItems.bark_spruce),
        new OreIngredient("wildroot")
    );
  }

  static {
    entitiesBlackList.addAll(Arrays.asList(SpellConfig.spellFeaturesCategory.sanctuaryEntitiesBlacklist));
  }

  @Override
  public boolean cast(EntityPlayer player, ModifierInstanceList modifiers, int ticks) {

    List<Entity> entities = Util.getEntitiesWithinRadius(player.world, Entity.class, player.getPosition(), radius_x, radius_y, radius_z);

    if (entities.size() > 0) {
      for (Entity e : entities) {
        if (e.getUniqueID().compareTo(player.getUniqueID()) != 0) {
          if ((e instanceof IProjectile || EntityUtil.isHostile(e)) && (!entitiesBlackList.contains(EntityList.getKey(e).toString()))) {
            if (Math.pow((e.posX - player.posX), 2) + Math.pow((e.posY - player.posY), 2) + Math.pow((e.posZ - player.posZ), 2) < 9.0f) {
              e.motionX = velocity * (e.posX - player.posX);
              e.motionY = velocity * (e.posY - player.posY);
              e.motionZ = velocity * (e.posZ - player.posZ);
              e.velocityChanged = true;
              if (!e.isInvisible()) {
                PacketHandler.sendToAllTracking(new MessageSanctuaryBurstFX(e.posX, e.posY + 0.6f * e.getEyeHeight(), e.posZ), e);
              }
            }
          }
        }
      }
    }
    if (player.ticksExisted % 2 == 0) {
      PacketHandler.sendToAllTracking(new MessageSanctuaryRingFX(player.posX, player.posY + 0.875f, player.posZ), player);
    }
    return true;
  }

  @Override
  public void doFinalise() {
    this.castType = properties.get(PROP_CAST_TYPE);
    this.cooldown = properties.get(PROP_COOLDOWN);
    this.radius_x = properties.get(PROP_RADIUS_X);
    this.radius_y = properties.get(PROP_RADIUS_Y);
    this.radius_z = properties.get(PROP_RADIUS_Z);
    this.velocity = properties.get(PROP_VELOCITY);
  }
}

