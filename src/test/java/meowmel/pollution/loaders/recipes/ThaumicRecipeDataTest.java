package meowmel.pollution.loaders.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThaumicRecipeDataTest {
    private JsonObject json(String value) { return JsonParser.parseString(value).getAsJsonObject(); }

    @Test void repeatedShapedSymbolsAreNotCollapsed() {
        var inputs = ThaumicRecipeData.inputs("thaumcraft:arcane_shaped", json("""
                {"pattern":["TQT"," B "],"key":{"T":{"item":"test:metal"},
                "Q":{"item":"test:mercury"},"B":{"item":"test:board"}}}
                """));
        assertEquals(4, inputs.size());
        assertEquals(2, inputs.stream().filter(i -> i.get("item").getAsString().equals("test:metal")).count());
    }

    @Test void centralItemRemainsAnInput() {
        var inputs = ThaumicRecipeData.inputs("thaumcraft:infusion", json("""
                {"central":{"item":"test:previous_template"},"components":[
                {"item":"test:crystal"},{"item":"test:crystal"}]}
                """));
        assertEquals(3, inputs.size());
        assertEquals("test:previous_template", inputs.get(0).get("item").getAsString());
    }

    @Test void essentiaAndVisAreConvertedOnlyOnce() {
        assertEquals(144, ThaumicRecipeData.fluidAmount(1, false));
        assertEquals(2304, ThaumicRecipeData.fluidAmount(16, false));
        assertEquals(576, ThaumicRecipeData.fluidAmount(16, true));
        assertEquals(3600, ThaumicRecipeData.fluidAmount(100, true));
        assertThrows(IllegalArgumentException.class, () -> ThaumicRecipeData.fluidAmount(-1, false));
        assertThrows(ArithmeticException.class, () -> ThaumicRecipeData.fluidAmount(Integer.MAX_VALUE, false));
    }

    @Test void customOrNbtIngredientsCannotTurnIntoFreeInputs() {
        for (String ingredient : new String[]{"{}", "{\"type\":\"test:custom\"}",
                "{\"item\":\"test:thing\",\"nbt\":\"{}\"}", "{\"item\":\"test:thing\",\"count\":0}"}) {
            assertThrows(IllegalArgumentException.class, () -> ThaumicRecipeData.inputs("thaumcraft:crucible",
                    json("{\"catalyst\":" + ingredient + "}")));
        }
    }

    @Test void unsupportedTypesAndMissingSymbolsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ThaumicRecipeData.inputs("thaumcraft:enchantment", json("{}")));
        assertThrows(IllegalArgumentException.class, () -> ThaumicRecipeData.inputs("thaumcraft:arcane_shaped",
                json("{\"pattern\":[\"X\"],\"key\":{}}")));
    }
}
