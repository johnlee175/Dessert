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
package com.johnsoft.samples.utils;

import java.awt.Color;
import java.awt.Font;

import com.johnsoft.samples.Settings3;
import com.johnsoft.samples.Settings3Impl;
import com.johnsoft.samples.Settings4;

/**
 * @author John Kenrinus Lee
 * @version 2019-04-29
 */
public enum SettingManager {
    self;

    private Settings4.Colors colors;
    private Settings4.Fonts fonts;

    public Settings4.Colors getColors() {
        return colors;
    }

    public SettingManager setColors(final Settings4.Colors colors) {
        this.colors = colors;
        return this;
    }

    public Settings4.Fonts getFonts() {
        return fonts;
    }

    public SettingManager setFonts(final Settings4.Fonts fonts) {
        this.fonts = fonts;
        return this;
    }

    public Settings4.Fonts.Builder newFonts() {
        return Settings4.Fonts.builder().setSettingManager(this);
    }

    public Settings4.Colors.Builder newColors() {
        return Settings4.Colors.builder().setSettingManager(this);
    }

    public static void main(String[] args) {
        // A
        SettingManager.self.newColors()
                           .setForeground(Color.BLACK)
                           .intoSettingManager()
                           .newFonts()
                           .setNormal(Font.decode("Monaco"))
                           .intoSettingManager();

        // B
        SettingManager.self.setColors(new Settings4.Colors.Builder()
                .setForeground(Color.BLACK).build());
        SettingManager.self.setFonts(new Settings4.Fonts.Builder()
                .setNormal(Font.decode("Monaco")).build());
    }
}
