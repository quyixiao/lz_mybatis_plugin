package com.test;

import java.lang.reflect.Method;

public class BBTest  extends  AATest{


    public void b(){

    }


    public static void main(String[] args) {
        Method method[] = BBTest.class.getMethods();
        for(Method method1 : method){
            System.out.println(method1.getName());
            System.out.println(method1.getDeclaringClass());
            if(method1.getDeclaringClass() == BBTest.class){
                System.out.println("=========" + method1.getName());
            }
        }
    }

}
