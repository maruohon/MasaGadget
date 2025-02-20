package com.plusls.MasaGadget.mixin.tweakeroo.renderTradeEnchantedBook;

import com.plusls.MasaGadget.config.Configs;
import fi.dy.masa.malilib.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity> extends EntityRenderer<T> {
    protected MixinLivingEntityRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    // from entityRenderer
    @Inject(method = "render", at = @At(value = "RETURN"))
    private void postRenderEntity(T livingEntity, float yaw, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, CallbackInfo ci) {
        if (!(livingEntity instanceof VillagerEntity) || !Configs.Tweakeroo.RENDER_TRADE_ENCHANTED_BOOK.getBooleanValue()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        World world = WorldUtils.getBestWorld(client);
        if (world == null) {
            return;
        }

        VillagerEntity villagerEntity = (VillagerEntity) world.getEntityById(livingEntity.getId());
        if (villagerEntity == null) {
            return;
        }
        Text text = null;
        Text price = null;

        for (TradeOffer tradeOffer : villagerEntity.getOffers()) {
            ItemStack sellItem = tradeOffer.getSellItem();
            if (sellItem.isOf(Items.ENCHANTED_BOOK)) {
                Map<Enchantment, Integer> enchantmentData = EnchantmentHelper.get(sellItem);
                for (Map.Entry<Enchantment, Integer> entry : enchantmentData.entrySet()) {
                    int level = entry.getValue();
                    int cost = tradeOffer.getOriginalFirstBuyItem().getCount();
                    int minCost = 2 + 3 * level;
                    int maxCost = minCost + 4 + level * 10;
                    if (entry.getKey().isTreasure()) {
                        minCost *= 2;
                        maxCost *= 2;
                    }
                    Formatting color;
                    if (cost <= (maxCost - minCost) / 3 + minCost) {
                        color = Formatting.GREEN;
                    } else if (cost <= (maxCost - minCost) / 3 * 2 + minCost) {
                        color = Formatting.WHITE;
                    } else {
                        color = Formatting.RED;
                    }
                    price = new LiteralText(String.format("%d(%d-%d)", cost, minCost, maxCost)).formatted(color);

                    if (level == entry.getKey().getMaxLevel()) {
                        text = ((MutableText) entry.getKey().getName(entry.getValue())).formatted(Formatting.GOLD);
                    } else {
                        text = ((MutableText) entry.getKey().getName(entry.getValue())).formatted(Formatting.WHITE);
                    }
                }
            }
            if (text != null) {
                break;
            }
        }
        if (text == null) {
            return;
        }
        double d = this.dispatcher.getSquaredDistanceToCamera(livingEntity);
        if (!(d > 4096.0D)) {
            float f = livingEntity.getHeight() / 8 * 7;
            matrixStack.push();
            matrixStack.translate(0, f, 0);
            matrixStack.multiply(this.dispatcher.getRotation());
            matrixStack.scale(-0.018F, -0.018F, 0.018F);
            matrixStack.translate(0, 0, -33);
            Matrix4f lv = matrixStack.peek().getModel();
            float g = client.options.getTextBackgroundOpacity(0.25F);
            int k = (int) (g * 255.0F) << 24;
            TextRenderer lv2 = this.getTextRenderer();
            float h = (float) (-lv2.getWidth(text) / 2);
            lv2.draw(text, h, 0, 553648127, false, lv, vertexConsumerProvider, false, k, light);
            lv2.draw(text, h, 0, -1, false, lv, vertexConsumerProvider, false, 0, light);
            h = (float) (-lv2.getWidth(price) / 2);
            lv2.draw(price, h, 11, 553648127, false, lv, vertexConsumerProvider, false, k, light);
            lv2.draw(price, h, 11, -1, false, lv, vertexConsumerProvider, false, 0, light);
            matrixStack.pop();
        }
    }
}
