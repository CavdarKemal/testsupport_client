package de.creditreform.crefoteam.cte.tesun.gui.pattern.mediator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MediatorIntegrationTest {
   private Button button;
   private Fan fan;

   @Before
   public void setUp() {
      button = new Button();
      fan = new Fan();
      PowerSupplier powerSupplier = new PowerSupplier();
      Mediator mediator = new Mediator();

      mediator.setButton(button);
      mediator.setFan(fan);
      mediator.setPowerSupplier(powerSupplier);
   }

   @Test
   public void givenTurnedOffFan_whenPressingButtonTwice_fanShouldTurnOnAndOff() {
      Assert.assertFalse(fan.isOn());

      button.press();
      Assert.assertTrue(fan.isOn());

      button.press();
      Assert.assertFalse(fan.isOn());
   }
}
