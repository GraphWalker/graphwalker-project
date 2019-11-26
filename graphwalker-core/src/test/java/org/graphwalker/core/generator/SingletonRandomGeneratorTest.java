package org.graphwalker.core.generator;

import org.junit.Test;

import java.util.Random;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SingletonRandomGeneratorTest {

  @Test
  public void instantiation() {
    assertThat(SingletonRandomGenerator.random(), is(instanceOf(Random.class)));
  }

  @Test
  public void seed() {
    SingletonRandomGenerator.setSeed(123);
    assertThat(SingletonRandomGenerator.nextInt(), is(-1188957731));
    assertThat(SingletonRandomGenerator.nextInt(), is(1018954901));
    assertThat(SingletonRandomGenerator.nextInt(), is(-39088943));
  }

  @Test
  public void un_seeded() {
    assertThat(SingletonRandomGenerator.nextInt(), is(instanceOf(int.class)));
  }

  @Test
  public void seededBoundNextInt() {
    SingletonRandomGenerator.setSeed(123);
    assertThat(SingletonRandomGenerator.nextInt(1), is(0));
    assertThat(SingletonRandomGenerator.nextInt(99), is(86));
    assertThat(SingletonRandomGenerator.nextInt(999), is(245));
  }

  @Test
  public void un_seededBoundNextInt() {
    assertThat(SingletonRandomGenerator.nextInt(999), is(instanceOf(int.class)));
  }
}
