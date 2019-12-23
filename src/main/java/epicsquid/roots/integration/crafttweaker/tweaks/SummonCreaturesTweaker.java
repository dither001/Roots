package epicsquid.roots.integration.crafttweaker.tweaks;

import crafttweaker.CraftTweakerAPI;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.entity.IEntityDefinition;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.CraftTweaker;
import epicsquid.roots.Roots;
import epicsquid.roots.init.ModRecipes;
import epicsquid.roots.integration.crafttweaker.Action;
import epicsquid.roots.util.zen.ZenDocAppend;
import epicsquid.roots.util.zen.ZenDocArg;
import epicsquid.roots.util.zen.ZenDocClass;
import epicsquid.roots.util.zen.ZenDocMethod;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.registry.EntityEntry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ZenDocClass("mods.roots.SummonCreatures")
@ZenDocAppend({"docs/include/summon_creatures.example.md"})
@ZenRegister
@ZenClass("mods." + Roots.MODID + ".SummonCreatures")
public class SummonCreaturesTweaker {

  @ZenDocMethod(
      order = 1,
      args = {
          @ZenDocArg(arg = "entity", info = "the entity to be summoned"),
          @ZenDocArg(arg = "ingredients", info = "a list of ingredients used for the summoning")
      }
  )
  @ZenMethod
  public static void addEntity(IEntityDefinition entity, IIngredient[] ingredients) {
    CraftTweaker.LATE_ACTIONS.add(new Add((EntityEntry) entity.getInternal(), Stream.of(ingredients).map(CraftTweakerMC::getIngredient).collect(Collectors.toList())));
  }

  @ZenDocMethod(
      order = 2,
      args = {
          @ZenDocArg(arg = "entity", info = "the entity to remove from summoning via recipe")
      }
  )
  @ZenMethod
  public static void removeEntity(IEntityDefinition entity) {
    CraftTweaker.LATE_ACTIONS.add(new Remove((EntityEntry) entity.getInternal()));
  }

  @ZenDocMethod(
      order = 3,
      args = {
          @ZenDocArg(arg = "entity", info = "the entity to remove life essence for")
      }
  )
  @ZenMethod
  public static void removeLifeEssence(IEntityDefinition entity) {
    CraftTweaker.LATE_ACTIONS.add(new RemoveLifeEssence((EntityEntry) entity.getInternal()));
  }

  @ZenDocMethod(
      order = 4,
      args = {
          @ZenDocArg(arg = "entity", info = "the entity to add life essence for")
      }
  )
  @ZenMethod
  public static void addLifeEssence(IEntityDefinition entity) {
    CraftTweaker.LATE_ACTIONS.add(new AddLifeEssence((EntityEntry) entity.getInternal()));
  }

  @ZenDocMethod(
      order = 5
  )
  @ZenMethod
  public static void clearLifeEssence() {
    CraftTweaker.LATE_ACTIONS.add(new ClearLifeEssence());
  }

  private static class Remove extends Action {
    private final EntityEntry entry;

    public Remove(EntityEntry entry) {
      super("remove_summon_creature");
      this.entry = entry;
      if (!EntityLivingBase.class.isAssignableFrom(this.entry.getEntityClass())) {
        CraftTweakerAPI.logError("Invalid Summon Creature entity class to remove: " + this.entry.getEntityClass().getSimpleName());
      }
    }

    @Override
    public void apply() {
      Class<? extends EntityLivingBase> clz = (Class<? extends EntityLivingBase>) entry.getEntityClass();
      ModRecipes.removeSummonCreatureEntry(clz);
    }

    @Override
    public String describe() {
      return String.format("Recipe to remove %s from SummonCreatures", entry.getName());
    }
  }

  private static class RemoveLifeEssence extends Action {
    private final EntityEntry entry;

    public RemoveLifeEssence(EntityEntry entry) {
      super("remove_life_essence");
      this.entry = entry;
      if (!EntityLivingBase.class.isAssignableFrom(this.entry.getEntityClass())) {
        CraftTweakerAPI.logError("Invalid Summon Creature entity class to remove: " + this.entry.getEntityClass().getSimpleName());
      }
    }

    @Override
    public void apply() {
      Class<? extends EntityLivingBase> clz = (Class<? extends EntityLivingBase>) entry.getEntityClass();
      ModRecipes.removeLifeEssence(clz);
    }

    @Override
    public String describe() {
      return String.format("Recipe to blacklist %s from SummonCreatures and Life Essence", entry.getName());
    }
  }

  private static class ClearLifeEssence extends Action {
    public ClearLifeEssence() {
      super("clear_life_essences");
    }

    @Override
    public void apply() {
      ModRecipes.clearLifeEssence();
    }

    @Override
    public String describe() {
      return String.format("Recipe to clear all life essences");
    }
  }

  private static class Add extends Action {
    private final EntityEntry entry;
    private List<Ingredient> ingredients;

    public Add(EntityEntry entry, List<Ingredient> ingredients) {
      super("add_summon_creature");
      this.entry = entry;
      this.ingredients = ingredients;
      if (!EntityLivingBase.class.isAssignableFrom(this.entry.getEntityClass())) {
        CraftTweakerAPI.logError("Invalid Summon Creature entity class to add: " + this.entry.getEntityClass().getSimpleName());
      }
    }

    @Override
    public void apply() {
      ModRecipes.addSummonCreatureEntry(entry.getName(), (Class<? extends EntityLivingBase>) entry.getEntityClass(), ingredients);
    }

    @Override
    public String describe() {
      return String.format("Recipe to add %s to Summon Creatures", entry.getName());
    }
  }

  private static class AddLifeEssence extends Action {
    private final EntityEntry entry;

    public AddLifeEssence(EntityEntry entry) {
      super("add_life_essence");
      this.entry = entry;
      if (!EntityLivingBase.class.isAssignableFrom(this.entry.getEntityClass())) {
        CraftTweakerAPI.logError("Invalid Summon Creature life essence entity class to add: " + this.entry.getEntityClass().getSimpleName());
      }
    }

    @Override
    public void apply() {
      ModRecipes.addLifeEssence((Class<? extends EntityLivingBase>) entry.getEntityClass());
    }

    @Override
    public String describe() {
      return String.format("Recipe to add %s to Life Essence", entry.getName());
    }
  }
}
