package meowmel.pollution.loaders.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/** Registry-free validation and arithmetic for the industrial recipe converter. */
final class ThaumicRecipeData {
    private ThaumicRecipeData() {}

    static List<JsonObject> inputs(String type, JsonObject json) {
        List<JsonObject> inputs = new ArrayList<>();
        switch (type) {
            case "thaumcraft:infusion" -> {
                inputs.add(ingredient(json.get("central")));
                for (JsonElement component : json.getAsJsonArray("components")) {
                    inputs.add(ingredient(component));
                }
            }
            case "thaumcraft:crucible" -> inputs.add(ingredient(json.get("catalyst")));
            case "thaumcraft:arcane_shapeless" -> {
                for (JsonElement component : json.getAsJsonArray("ingredients")) {
                    inputs.add(ingredient(component));
                }
            }
            case "thaumcraft:arcane_shaped" -> {
                JsonObject key = json.getAsJsonObject("key");
                JsonArray pattern = json.getAsJsonArray("pattern");
                for (JsonElement row : pattern) {
                    for (char symbol : row.getAsString().toCharArray()) {
                        if (symbol != ' ') inputs.add(ingredient(key.get(String.valueOf(symbol))));
                    }
                }
            }
            default -> throw new IllegalArgumentException("unsupported-type");
        }
        if (inputs.isEmpty()) throw new IllegalArgumentException("empty-inputs");
        return inputs;
    }

    private static JsonObject ingredient(JsonElement value) {
        if (value == null || !value.isJsonObject()) throw new IllegalArgumentException("unsupported-ingredient");
        JsonObject input = value.getAsJsonObject();
        if (input.has("nbt") || input.has("type") || input.has("components")
                || input.has("item") == input.has("tag")) {
            throw new IllegalArgumentException("unsupported-ingredient");
        }
        if (input.has("count") && input.get("count").getAsInt() <= 0) {
            throw new IllegalArgumentException("invalid-count");
        }
        return input;
    }

    static int fluidAmount(int points, boolean arcane) {
        if (points < 0) throw new IllegalArgumentException("negative-aspect");
        return Math.multiplyExact(points, arcane ? 36 : 144);
    }
}
