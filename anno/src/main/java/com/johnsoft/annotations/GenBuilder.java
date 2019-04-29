/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package com.johnsoft.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate it for Builder code generated<br/>
 * NOTE:
 * 1. If class A marked it, means use A as description, outline, meta-info to generate class B and B.Builder, not modify A.<br/>
 *    If A named "SomethingDesc", "SomethingMeta", "SomethingOutline", B got name "Something", else just add "Impl" for A's name as B's name.<br/>
 * 2. The static field and function, instance method just be copyed. Interface, annotation, and enum will be ignored.<br/>
 * 3. We generate Builder class based on A's instance property and nested classes's instance property.<br/>
 *
 * @author John Kenrinus Lee
 * @version 2019-04-17
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface GenBuilder {
    /**
     * @return if true, means class can be inherited, the Builder also can be inherited, then the generic will be applied for Builder.
     *         default false.
     */
    boolean inheritable() default false;

    /**
     * @return if true, means the nested classes should be using Builder.
     *         default false, ignore nested classes.
     */
    boolean nested() default false;

    /**
     * @return if true, will make Builder bridge to other class, with private field + setXXX + intoXXX method.
     *         default false.
     */
    Class[] join() default {};

    /**
     * Note: custom getter is not support, will be conflict with the getter generated, 
     * but you can delegate to other method with {@link Getter}.
     *
     * @return if true, will use private final field + public method.
     *         default false, use public final field.
     */
    boolean getter() default false;

    /**
     * Note: add -> adder, just like set -> setter.
     * @return if true, will add method named addToXX for the Set, List, Map type of field, to add single element.
     *         default false.
     */
    boolean elementAdder() default false;

    /**
     * The value as pattern will replace field's getter's body which generated.<br/>
     * Note: <br/>
     * 1. just one statement supported.<br/>
     * 2. $N is the field's ref mapping the getter (only one, default: "return $N").<br/>
     */
    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.CLASS)
    @interface Getter {
        String value() default "";
    }

    /**
     * Builder's setter body pattern.<br/>
     * Note: <br/>
     * 1. just one statement supported.<br/>
     * 2. $N is the field's ref and param's ref mapping the setter (only two, default: "this.$N = $N").<br/>
     */
    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.CLASS)
    @interface Setter {
        String value() default "";
    }

    /**
     * Builder's setter is constant(int, string) of enum. <br/>
     * Note: <br/>
     * 1. You can't use this with {@link Setter} mark for the same field.<br/>
     * 2. You need provide names, and one of xxxValues (if both, int > string), and names.length = values.length.<br/>
     */
    @Documented
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.CLASS)
    @interface Enums {
        String[] names();
        int[] intValues() default {};
        String[] stringValues() default {};
    }
}
