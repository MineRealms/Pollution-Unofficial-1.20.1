package meowmel.pollution.gametest;

import meowmel.pollution.Pollution;
import meowmel.pollution.common.item.PollutionItems;
import meowmel.pollution.common.item.bauble.GogglesItem;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.*;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

@GameTestHolder(Pollution.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EquipmentGameTests {
    private EquipmentGameTests() {}
    @GameTest(template = "platform", batch = "equipment")
    public static void gogglesUseHelmetSlotAndPreserveExternalNightVision(GameTestHelper helper) {
        var stack = PollutionItems.NANO_GOGGLES.asStack();
        helper.assertTrue(((ArmorItem) stack.getItem()).getEquipmentSlot() == EquipmentSlot.HEAD, "goggles cannot be equipped as a helmet");
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "PollutionArmorTest"));
        player.getFoodData().setFoodLevel(20);
        var battery = GTCapabilityHelper.getElectricItem(stack);
        helper.assertTrue(battery != null, "goggles lost their electrical capability");
        battery.charge(100, 1, true, false);
        stack.getOrCreateTag().putBoolean(GogglesItem.TAG_NIGHT_VISION, true);
        stack.getOrCreateTag().putByte(GogglesItem.TAG_TOGGLE_TIMER, (byte) 5);
        stack.getItem().onArmorTick(stack, helper.getLevel(), player);
        helper.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION) && battery.getCharge() == 98,
                "helmet tick did not supply night vision at its declared cost");
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 2400));
        GogglesItem.disableNightVision(helper.getLevel(), player, false);
        helper.assertTrue(player.hasEffect(MobEffects.NIGHT_VISION) && player.getEffect(MobEffects.NIGHT_VISION).getDuration() == 2400,
                "unequipping removed potion night vision");
        helper.succeed();
    }
}
