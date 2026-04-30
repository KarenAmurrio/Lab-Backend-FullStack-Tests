package com.g5.relpapel.msbookpayments.MsBookPayments.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ItemTest {

    @Test
    void isStockDisponible() {
        Item item = new Item("1","Quijoque", "123445", 15, true);
        boolean response = item.isStockDisponible(5);
        assertNotNull(response);
        assertEquals(true,response);
    }
    @Test
    void isStockDisponible_NoDisponible() {
        Item item = new Item("1","Quijoque", "123445", 15, true);
        boolean response = item.isStockDisponible(55);
        assertNotNull(response);
        assertEquals(false,response);
    }
    @Test
    void isStockDisponible_NoVisible() {
        Item item = new Item("1","Quijoque", "123445", 15, false);
        boolean response = item.isStockDisponible(5);
        assertNotNull(response);
        assertEquals(false,response);
    }
}