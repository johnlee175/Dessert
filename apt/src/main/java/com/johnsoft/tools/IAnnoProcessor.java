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

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * annotation process unit，for {@link AnnoProcTool}'s phase
 *
 * @author John Kenrinus Lee
 * @version 2019-04-17
 */
public interface IAnnoProcessor {
    /**
     * {@link AnnoProcTool#getSupportedAnnotationTypes}
     */
    void fillSupportedAnnotationTypes(Set<String> types);

    /**
     * {@link AnnoProcTool#getSupportedOptions()}
     */
    void fillSupportedOptions(Set<String> options);

    /**
     * {@link AnnoProcTool#init(ProcessingEnvironment)}
     */
    void init(ProcessingEnvironment processingEnvironment);

    /**
     * {@link AnnoProcTool#process(Set, RoundEnvironment)}
     */
    void prepare(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment);

    /**
     * {@link AnnoProcTool#process(Set, RoundEnvironment)}
     */
    boolean canProcess(TypeElement typeElement);

    /**
     * {@link AnnoProcTool#process(Set, RoundEnvironment)}
     */
    void process(ProcessingEnvironment processingEnvironment,
                 Set<? extends TypeElement> set,
                 RoundEnvironment roundEnvironment,
                 Types typeUtils, Elements elementUtils,
                 Filer filer, Messager messager,
                 Map<String, String> options);
}
