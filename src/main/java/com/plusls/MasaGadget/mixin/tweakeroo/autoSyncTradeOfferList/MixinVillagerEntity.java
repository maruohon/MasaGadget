package com.plusls.MasaGadget.mixin.tweakeroo.autoSyncTradeOfferList;

import com.plusls.MasaGadget.config.Configs;
import com.plusls.MasaGadget.tweakeroo.pcaSyncProtocol.PcaSyncProtocol;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerEntity.class)
public abstract class MixinVillagerEntity extends MerchantEntity {

    private VillagerProfession oldVillagerProfession;

    public MixinVillagerEntity(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void syncVillagerData(CallbackInfo ci) {
        if (!Configs.Tweakeroo.AUTO_SYNC_TRADE_OFFER_LIST.getDefaultBooleanValue() || MinecraftClient.getInstance().isIntegratedServerRunning() || !PcaSyncProtocol.enable) {
            return;
        }
        VillagerProfession currentVillagerProfession = ((VillagerEntity) (Object) this).getVillagerData().getProfession();
        if (oldVillagerProfession != currentVillagerProfession) {
            PcaSyncProtocol.syncEntity(this.getId());
            PcaSyncProtocol.cancelSyncEntity();
            oldVillagerProfession = currentVillagerProfession;
        }
    }
}
