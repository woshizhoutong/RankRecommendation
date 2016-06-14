package com.tong.rankrec;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {
   public static void main(String[] args) {
      Result result = JUnitCore.runClasses(RankRecServiceTest.class);
      for (Failure failure : result.getFailures()) {
         System.out.println(failure.toString());
      }
      if (result.wasSuccessful()) {
    	  System.out.println("Junit test finished successfully.");
      } else {
    	  System.out.println("Junit test failed.");
      }
   }
}  	