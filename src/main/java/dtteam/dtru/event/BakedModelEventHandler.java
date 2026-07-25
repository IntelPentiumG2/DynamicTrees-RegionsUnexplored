package dtteam.dtru.event;

import dtteam.dtru.DynamicTreesRU;
import dtteam.dtru.model.UnbakedBambooBranchModel;
import dtteam.dtru.model.UnbakedEucalyptusBranchModel;
import dtteam.dtru.model.UnbakedEucalyptusSurfaceRootModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

@EventBusSubscriber(modid = DynamicTreesRU.MOD_ID, value = Dist.CLIENT)
public final class BakedModelEventHandler {

    @SubscribeEvent
    public static void onModelRegistryEvent(RegisterBlockStateModels event) {
        event.registerModel(DynamicTreesRU.BAMBOO, UnbakedBambooBranchModel.CODEC);
        event.registerModel(DynamicTreesRU.EUCALYPTUS, UnbakedEucalyptusBranchModel.CODEC);
        event.registerModel(DynamicTreesRU.EUCALYPTUS_SURFACE_ROOT, UnbakedEucalyptusSurfaceRootModel.CODEC);
    }

}
