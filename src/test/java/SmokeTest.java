import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SmokeTest {

    @Test
    void mainClassIsLoadable() {
        assertDoesNotThrow(() -> {
            Class<?> mainClass = Class.forName("Main");
            assertNotNull(mainClass);
        });
    }
}
