package dtteam.dtru;

import com.dtteam.dynamictrees.block.leaves.LeavesProperties;
import com.dtteam.dynamictrees.block.soil.SoilProperties;
import com.dtteam.dynamictrees.data.GatherDataHelper;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.tree.family.Family;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.treepack.Resources;
import dtteam.dtru.data.DTRUDataGenerators;
import dtteam.dtru.data.DTRULoaderBuilders;
import dtteam.dtru.init.DTRUPlusRegistries;
import dtteam.dtru.init.DTRURegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DynamicTreesRU.MOD_ID)

public class DynamicTreesRU {
    public static final String MOD_ID = "dtru";

    /**
     * Block state model types. Since 26.1 these are named directly by block state definitions rather
     * than by a model loader, so they are declared here for both the client registration and the
     * families that reference them during data generation.
     */
    public static final Identifier BAMBOO = location("bamboo");
    public static final Identifier EUCALYPTUS = location("eucalyptus");
    public static final Identifier EUCALYPTUS_SURFACE_ROOT = location("eucalyptus_surface_root");

    public DynamicTreesRU(IEventBus bus, ModContainer modContainer) {

        bus.addListener(this::commonSetup);
        bus.addListener(this::gatherClientData);

        if (isDynamicTreesPlusLoaded()){
            bus.register(new DTRUPlusRegistries());
        }

        NeoForgeRegistryHandler.setup(MOD_ID, bus);
        DTRURegistries.setup();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        if (isDynamicTreesPlusLoaded()){
            DTRUPlusRegistries.setup();
        }
    }

    private void gatherClientData(final GatherDataEvent.Client event) {
        // Both of these name client-only data generation types, so they stay out of the mod
        // constructor: the JVM verifies a whole class when it links it, which would drag those types
        // onto a dedicated server.
        DTRUDataGenerators.register();
        DTRULoaderBuilders.register();

        // Dynamic Trees only fires its own data gathering for the mods named by --mod, so the tree
        // pack has to be loaded here for an add-on's run to see any of its own trees.
        Resources.MANAGER.gatherData();

        if (isDynamicTreesPlusLoaded()){
            DTRUPlusRegistries.gatherClientData(event);
        } else {
            GatherDataHelper.gatherClientData(MOD_ID, event,
                    SoilProperties.REGISTRY,
                    Family.REGISTRY,
                    Species.REGISTRY,
                    LeavesProperties.REGISTRY
            );
        }
    }

    public static Identifier location (final String name){
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static boolean isDynamicTreesPlusLoaded(){
        return ModList.get().isLoaded("dynamictreesplus");
    }
}
