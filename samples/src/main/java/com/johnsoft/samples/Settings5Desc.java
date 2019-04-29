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
package com.johnsoft.samples;

import java.util.ArrayList;
import java.util.List;

import com.johnsoft.annotations.GenBuilder;
import com.johnsoft.samples.utils.Nullable;
import com.johnsoft.samples.utils.Util;

/**
 * @author John Kenrinus Lee
 * @version 2019-04-29
 */
@GenBuilder(inheritable = true, nested = true, getter = false, elementAdder = true)
public class Settings5Desc {
    /**
     * a comment will be copied
     */
    @Nullable
    private static final String xib = "iiiii";

    @Nullable
    @GenBuilder.Getter("return $N + 33")
    public String name = "dark";

    @GenBuilder.Setter("int tmp = $N; if (tmp > 0) this.$N = tmp")
    public int age = 3;

    public long xs = Util.Inner.class.hashCode();

    public ArrayList<String> keys = new ArrayList<>();
    public List<String> values = new ArrayList<>();

    @Nullable
    public <T> T test(@Nullable String... stb) throws IllegalArgumentException {
        System.out.println("MMMMMM");
        return (T) this;
    }
}