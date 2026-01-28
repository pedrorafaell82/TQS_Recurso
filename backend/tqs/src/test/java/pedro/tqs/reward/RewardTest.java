package pedro.tqs.reward;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RewardTest {

    @Test
    void reward_defaultsActiveTrue_andSettersWork() {
        Reward r = new Reward("Coffee", 7);

        assertEquals("Coffee", r.getName());
        assertEquals(7, r.getCost());
        assertTrue(r.isActive());

        r.setActive(false);
        r.setCost(10);
        r.setName("Coffee Voucher");

        assertFalse(r.isActive());
        assertEquals(10, r.getCost());
        assertEquals("Coffee Voucher", r.getName());
    }
}
