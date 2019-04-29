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
package com.johnsoft.tools;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.google.auto.service.AutoService;

/**
 * The annotation processor<br/>
 *
 * @author John Kenrinus Lee
 * @version 2019-04-17
 */
@SuppressWarnings("unused")
@AutoService(Processor.class)
public class AnnoProcTool extends AbstractProcessor {
    private final IAnnoProcessor[] processors = new IAnnoProcessor[] {
        new GenBuilderProcessor()
    };

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (IAnnoProcessor processor : processors) {
            processor.fillSupportedAnnotationTypes(types);
        }
        return types;
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> options = new LinkedHashSet<>();
        for (IAnnoProcessor processor : processors) {
            processor.fillSupportedOptions(options);
        }
        return options;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        for (IAnnoProcessor processor : processors) {
            processor.init(processingEnvironment);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Types typeUtils = processingEnv.getTypeUtils();
        Elements elementUtils = processingEnv.getElementUtils();
        Filer filer = processingEnv.getFiler();
        Messager messager = processingEnv.getMessager();
        Map<String, String> options = processingEnv.getOptions();

        // do somethings for all round here
        for (IAnnoProcessor processor : processors) {
            processor.prepare(set, roundEnvironment);
        }

        if (set.isEmpty()) {
            return false;
        }

        boolean claimed = false;
        for (TypeElement typeElement : set) {
            for (IAnnoProcessor processor : processors) {
                if (processor.canProcess(typeElement)) {
                    try {
                        processor.process(processingEnv, set, roundEnvironment, typeUtils, elementUtils,
                                filer, messager, options);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    claimed = true;
                }
            }
        }
        return claimed;
    }
}
