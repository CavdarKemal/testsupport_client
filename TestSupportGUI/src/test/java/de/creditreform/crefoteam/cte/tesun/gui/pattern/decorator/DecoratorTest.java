package de.creditreform.crefoteam.cte.tesun.gui.pattern.decorator;

import org.junit.Assert;
import org.junit.Test;

public class DecoratorTest {
   @Test
   public void whenDecoratorsInjectedAtRuntime_thenConfigSuccess() {
      ChristmasTree tree1 = new Garland(new ChristmasTreeImpl());
      Assert.assertEquals("Christmas tree with Garland", tree1.decorate());

      ChristmasTree tree2 = new BubbleLights( new Garland(new Garland(new ChristmasTreeImpl())));
      Assert.assertEquals("Christmas tree with Garland with Garland with Bubble Lights", tree2.decorate());
   }
}
