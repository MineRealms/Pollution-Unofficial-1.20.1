package meowmel.pollution.api.magic;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

class EssenceBatchTest {
    private EssenceBatch ready(long amount) {
        var batch = new EssenceBatch();
        batch.start(Map.of("thaumcraft:aer", amount), 1);
        batch.advance();
        return batch;
    }
    @Test void multipleReceiversShareOneOutputNotCopies() {
        var batch = ready(10);
        AtomicLong first = new AtomicLong(), second = new AtomicLong();
        batch.offer((id, amount) -> { first.set(Math.min(3, amount)); return first.get(); });
        batch.offer((id, amount) -> { second.set(amount); return amount; });
        assertEquals(3, first.get());
        assertEquals(7, second.get());
        assertFalse(batch.active());
        batch.offer((id, amount) -> fail("Completed batch must not be replayed"));
    }
    @Test void fullReceiverKeepsPendingWithoutExtraProcessing() {
        var batch = ready(144);
        assertFalse(batch.offer((id, amount) -> 0));
        batch.advance();
        assertEquals(1, batch.progress());
        assertEquals(144L, batch.remaining().get("thaumcraft:aer"));
    }
    @Test void partialMillibucketsSurviveSaveLoad() {
        var batch = ready(576);
        batch.offer((id, amount) -> 17);
        var loaded = new EssenceBatch();
        loaded.load(batch.save());
        assertTrue(loaded.ready());
        assertEquals(559L, loaded.remaining().get("thaumcraft:aer"));
        loaded.offer((id, amount) -> amount);
        assertFalse(loaded.active());
    }
    @Test void processingAndMultipleAspectsSurviveReload() {
        var batch = new EssenceBatch();
        batch.start(Map.of("thaumcraft:aer", 2L, "thaumcraft:ignis", 8L), 20);
        batch.advance();
        assertFalse(batch.offer((id, amount) -> fail("Not processed yet")));
        var loaded = new EssenceBatch();
        loaded.load(batch.save());
        assertEquals(1, loaded.progress());
        for (int i = 0; i < 19; i++) loaded.advance();
        loaded.offer((id, amount) -> id.equals("thaumcraft:aer") ? amount : 0);
        assertEquals(Map.of("thaumcraft:ignis", 8L), loaded.remaining());
    }
    @Test void cannotConsumeNextInputBeforeOutputDrains() {
        var batch = ready(1);
        assertThrows(IllegalStateException.class, () -> batch.start(Map.of("thaumcraft:aer", 2L), 1));
    }
    @Test void receiverCannotMakeDebtNegative() {
        var batch = ready(1);
        assertFalse(batch.offer((id, amount) -> -1));
        assertTrue(batch.offer((id, amount) -> Long.MAX_VALUE));
        assertFalse(batch.active());
    }
}
