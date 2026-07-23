package kfclash.citylogic.domain;

import org.junit.Test;

import static org.junit.Assert.*;

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

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionCreationWithZeroWidth() {
        new Dimension(0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionCreationWithZeroHeight() {
        new Dimension(10, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionCreationWithNegativeWidth() {
        new Dimension(-5, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionCreationWithNegativeHeight() {
        new Dimension(10, -5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionCreationWithBothZero() {
        new Dimension(0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimensionCreationWithBothNegative() {
        new Dimension(-1, -1);
    }

    @Test
    public void testDimensionWithLargeValues() {
        Dimension dimension = new Dimension(1000, 2000);
        assertEquals(1000, dimension.getWidth());
        assertEquals(2000, dimension.getHeight());
    }
}
