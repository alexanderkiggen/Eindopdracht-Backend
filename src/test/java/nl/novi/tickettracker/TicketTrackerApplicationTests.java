package nl.novi.tickettracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TicketTrackerApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testMain() {
        TicketTrackerApplication.main(new String[]{"--spring.profiles.active=test"});
    }
}