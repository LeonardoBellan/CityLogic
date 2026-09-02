package kfclash.citylogic.domain;

import org.junit.jupiter.api.Test;

import kfclash.citylogic.domain.map.Dimension;

import static org.junit.jupiter.api.Assertions.*;

public class DimensionTest {

    @Test
    public void testDimensionCreationWithValidValues() {
        Dimension dimension = new Dimension(10, 20);
        assertEquals(10, dimension.getWidth());
        assertEquals(20, dimension.getHeight());
    }

    @Test
    public void testDimensionCreationWithMinimumValues() {
        Dimension dimension = new Dimension(1, 1);
        assertEquals(1, dimension.getWidth());
        assertEquals(1, dimension.getHeight());
    }

    @Test
    public void testDimensionCreationWithZeroWidth() {
        assertThrows(IllegalArgumentException.class, () -> new Dimension(0, 10));
    }

    @Test
    public void testDimensionCreationWithZeroHeight() {
        assertThrows(IllegalArgumentException.class, () -> new Dimension(10, 0));
    }

    @Test
    public void testDimensionCreationWithNegativeWidth() {
        assertThrows(IllegalArgumentException.class, () -> new Dimension(-5, 10));
    }

    @Test
    public void testDimensionCreationWithNegativeHeight() {
        assertThrows(IllegalArgumentException.class, () -> new Dimension(10, -5));
    }

    @Test
    public void testDimensionCreationWithBothZero() {
        assertThrows(IllegalArgumentException.class, () -> new Dimension(0, 0));
    }

    @Test
    public void testDimensionCreationWithBothNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Dimension(-1, -1));
    }

    @Test
    public void testDimensionWithLargeValues() {
        Dimension dimension = new Dimension(1000, 2000);
        assertEquals(1000, dimension.getWidth());
        assertEquals(2000, dimension.getHeight());
    }
}
