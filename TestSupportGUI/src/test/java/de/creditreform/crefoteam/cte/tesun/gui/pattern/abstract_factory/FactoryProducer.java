package de.creditreform.crefoteam.cte.tesun.gui.pattern.abstract_factory;

public class FactoryProducer {
   public static AbstractFactory getFactory(boolean rounded){
      if(rounded){
         return new RoundedShapeFactory();
      }else{
         return new ShapeFactory();
      }
   }

}
