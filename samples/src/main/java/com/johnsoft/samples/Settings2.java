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

import java.awt.Color;
import java.awt.Font;

import com.johnsoft.annotations.GenBuilder;
import com.johnsoft.samples.utils.Util;

/**
 * @author John Kenrinus Lee
 * @version 2019-04-29
 */
@GenBuilder(getter = true, nested = true)
public class Settings2 {
    public static class Fonts {
        public Font normal = new Font("Arial", Font.BOLD, 32);
    }

    public static class Colors {
        public Color foreground = Util.getDefaultColor();
    }
}