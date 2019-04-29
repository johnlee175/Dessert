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

import com.johnsoft.annotations.GenBuilder;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

/**
 * APT for GenBuilder (generate Builder code)
 *
 * @author John Kenrinus Lee
 * @version 2019-04-17
 */
public class GenBuilderProcessor implements IAnnoProcessor {
    private Trees trees;

    @Override
    public void fillSupportedAnnotationTypes(final Set<String> types) {
        types.add(GenBuilder.class.getCanonicalName());
    }

    @Override
    public void fillSupportedOptions(final Set<String> options) {
    }

    @Override
    public void init(ProcessingEnvironment processingEnvironment) {
        trees = Trees.instance(processingEnvironment);
    }

    @Override
    public void prepare(final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment) {
    }

    @Override
    public boolean canProcess(final TypeElement typeElement) {
        return typeElement.getQualifiedName().contentEquals(GenBuilder.class.getCanonicalName());
    }

    @Override
    public void process(final ProcessingEnvironment processingEnvironment,
                        final Set<? extends TypeElement> set, final RoundEnvironment roundEnvironment,
                        final Types typeUtils, final Elements elementUtils,
                        final Filer filer, final Messager messager,
                        final Map<String, String> options) {
        if (trees == null) {
            return;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, "DO -> processGenBuilder");

        for (Element element : roundEnvironment.getElementsAnnotatedWith(GenBuilder.class)) {
            if (element.getKind() != ElementKind.CLASS) { // interface is not ok
                throw new IllegalArgumentException(String.format("Only class can be annotated with @%s",
                        GenBuilder.class.getSimpleName()));
            }
            TypeElement typeElement = (TypeElement) element;
            if (typeElement.getNestingKind() != NestingKind.TOP_LEVEL) {
                throw new IllegalArgumentException(String.format("Only Top-level class can be annotated with @%s",
                        GenBuilder.class.getSimpleName()));
            }
            GenBuilder annotation = typeElement.getAnnotation(GenBuilder.class);
            boolean useGetter = annotation.getter();
            boolean inheritable = annotation.inheritable();
            boolean nested = annotation.nested();
            boolean elementAdder = annotation.elementAdder();
            Object join = extraMemberInAnnotation(typeElement, GenBuilder.class, "join");
            final String[] joinStrings;
            if (join instanceof List) {
                List list = (List) join;
                int len = list.size();
                joinStrings = new String[len];
                for (int i = 0; i < len; ++i) {
                    joinStrings[i] = list.get(i).toString();
                }
            } else {
                joinStrings = new String[0];
            }

            Map<String, String> nameResolveMap = resolveNameSymbols(trees, typeElement);
            ClassName targetClassName = makeTargetName(typeElement.getQualifiedName().toString(),
                    typeElement.getSimpleName().toString(), nameResolveMap.get("package"));
            TypeSpec typeSpec =
                    doProcess(targetClassName, typeElement, typeUtils, elementUtils, messager, nameResolveMap,
                            useGetter, inheritable, nested, elementAdder, joinStrings);

            try { // write to file
                JavaFile.builder(targetClassName.packageName(), typeSpec).indent("    ").build()
                        .writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.WARNING, e.toString());
            }
        }
    }

    private TypeSpec doProcess(ClassName targetClassName, TypeElement typeElement,
                               Types typeUtils, Elements elementUtils,
                               Messager messager, Map<String, String> nameResolveMap,
                               boolean useGetter, boolean inheritable, boolean nested,
                               boolean elementAdder, String[] join) {
        ClassName builderClassName = targetClassName.nestedClass("Builder");
        TypeVariableName typeVariableName = TypeVariableName.get("T", builderClassName);
        String targetParamName = makeParamName(targetClassName.simpleName());
        String builderParamName = "builder";

        List<FieldSpec> targetFields = new ArrayList<>();
        List<FieldSpec> builderFields = new ArrayList<>();
        List<MethodSpec> targetMethods = new ArrayList<>();
        List<MethodSpec> builderMethods = new ArrayList<>();
        List<TypeSpec> nestTypes = new ArrayList<>();
        CodeBlock.Builder targetCopyCode = CodeBlock.builder();
        CodeBlock.Builder builderDefaultCode = CodeBlock.builder();
        CodeBlock.Builder builderCopyCode = CodeBlock.builder();

        for (Tree member : trees.getTree(typeElement).getMembers()) {
            switch (member.getKind()) {
                case VARIABLE: {
                    JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) member;
                    Type type = variableDecl.getType().type;
                    String name = variableDecl.getName().toString();
                    JCTree.JCExpression initializer = variableDecl.getInitializer();
                    Set<Modifier> modifiers = variableDecl.getModifiers().getFlags();
                    String value = convertInitValueExpr(initializer, nameResolveMap);
                    TypeName typeName = TypeName.get(type);
                    String javadoc = elementUtils.getDocComment(variableDecl.sym);
                    if (javadoc == null) {
                        javadoc = "";
                    }

                    if (modifiers.contains(Modifier.STATIC)) { // just copy
                        targetFields.add(FieldSpec.builder(typeName, name, modifiers.toArray(EMPTY_MODIFIERS))
                                                  .initializer(value)
                                                  .addAnnotations(convertAnnotations(variableDecl.sym,
                                                          ElementType.FIELD))
                                                  .addJavadoc(javadoc)
                                                  .build());
                    } else {
                        if (useGetter) {
                            // private field
                            targetFields.add(FieldSpec.builder(typeName, name, Modifier.PRIVATE, Modifier.FINAL)
                                                      .addAnnotations(convertAnnotations(variableDecl.sym,
                                                              ElementType.FIELD,
                                                              GenBuilder.Getter.class,
                                                              GenBuilder.Setter.class,
                                                              GenBuilder.Enums.class))
                                                      .build());
                            // make getter
                            String content = "return $N";
                            GenBuilder.Getter getter =
                                    variableDecl.sym.getAnnotation(GenBuilder.Getter.class);
                            if (getter != null) {
                                content = getter.value();
                            }
                            targetMethods.add(MethodSpec.methodBuilder(makeBeanName(name, true, type))
                                            .addAnnotations(convertAnnotations(variableDecl.sym, ElementType.METHOD))
                                            .addJavadoc(javadoc)
                                            .addModifiers(Modifier.PUBLIC)
                                            .returns(typeName)
                                            .addStatement(content, name)
                                            .build());
                        } else {
                            // public field
                            targetFields.add(FieldSpec.builder(typeName, name, Modifier.PUBLIC, Modifier.FINAL)
                                                      .addAnnotations(convertAnnotations(variableDecl.sym,
                                                              ElementType.FIELD,
                                                              GenBuilder.Getter.class,
                                                              GenBuilder.Setter.class,
                                                              GenBuilder.Enums.class))
                                                      .addJavadoc(javadoc)
                                                      .build());
                        }
                        // private field in Builder
                        builderFields.add(FieldSpec.builder(typeName, name, Modifier.PRIVATE)
                                                   .addAnnotations(convertAnnotations(variableDecl.sym,
                                                           ElementType.FIELD,
                                                           GenBuilder.Getter.class,
                                                           GenBuilder.Setter.class,
                                                           GenBuilder.Enums.class))
                                                   .build());
                        GenBuilder.Enums enums = variableDecl.sym.getAnnotation(GenBuilder.Enums.class);
                        if (enums != null) {
                            String[] names = enums.names();
                            int[] ints = enums.intValues();
                            String[] strings = enums.stringValues();
                            int length = names.length;
                            if (ints.length > 0) {
                                if (length != ints.length) {
                                    throw new IllegalArgumentException("GenBuilder.Enums: "
                                            + "names().length != intValues().length");
                                }
                                for (int i = 0; i < length; ++i) {
                                    builderMethods.add(MethodSpec.methodBuilder(makeBeanName(name, false, type)
                                            + "To" + names[i])
                                                    .addJavadoc(javadoc)
                                                    .addModifiers(Modifier.PUBLIC)
                                                    .returns(inheritable ? typeVariableName : builderClassName)
                                                    .addStatement("this.$N = $L", name, ints[i])
                                                    .addStatement(inheritable
                                                                  ? "return (" + typeVariableName.name + ") this"
                                                                  : "return this")
                                                    .build());
                                }
                            } else if (strings.length > 0) {
                                if (length != strings.length) {
                                    throw new IllegalArgumentException("GenBuilder.Enums: "
                                            + "names().length != stringValues().length");
                                }
                                for (int i = 0; i < length; ++i) {
                                    builderMethods.add(MethodSpec.methodBuilder(makeBeanName(name, false, type)
                                            + "To" + names[i])
                                                    .addJavadoc(javadoc)
                                                    .addModifiers(Modifier.PUBLIC)
                                                    .returns(inheritable ? typeVariableName : builderClassName)
                                                    .addStatement("this.$N = $S", name, strings[i])
                                                    .addStatement(inheritable
                                                                  ? "return (" + typeVariableName.name + ") this"
                                                                  : "return this")
                                                    .build());
                                }
                            } else {
                                throw new IllegalArgumentException("GenBuilder.Enums: "
                                        + "intValues().length <= 0 && stringValues().length <= 0");
                            }
                        } else {
                            // make setter in Builder
                            String statement = "this.$N = $N";
                            GenBuilder.Setter setter = variableDecl.sym.getAnnotation(GenBuilder.Setter.class);
                            if (setter != null) {
                                statement = setter.value();
                            }
                            ParameterSpec ps = ParameterSpec.builder(typeName, name)
                                                            .addAnnotations(convertAnnotations(variableDecl.sym,
                                                                    ElementType.PARAMETER))
                                                            .build();
                            builderMethods.add(MethodSpec.methodBuilder(makeBeanName(name, false, type))
                                            .addJavadoc(javadoc)
                                            .addModifiers(Modifier.PUBLIC)
                                            .returns(inheritable ? typeVariableName : builderClassName)
                                            .addParameter(ps)
                                            .addStatement(statement, name, name)
                                            .addStatement(inheritable ? "return (" + typeVariableName.name + ") this"
                                                              : "return this")
                                            .build());
                        }

                        // make statement will be used in constructors
                        targetCopyCode.addStatement("this.$N = $N.$N", name, builderParamName, name);
                        builderDefaultCode.addStatement("this.$N = $L", name, value);
                        builderCopyCode.addStatement("this.$N = $N.$N", name, targetParamName, name);

                        if (elementAdder) { // addToXXX -> List.add / Map.put
                            TypeMirror collectionType = elementUtils
                                    .getTypeElement("java.util.Collection").asType();
                            TypeMirror listType = elementUtils
                                    .getTypeElement("java.util.List").asType();
                            TypeMirror setType = elementUtils
                                    .getTypeElement("java.util.Set").asType();
                            TypeMirror mapType = elementUtils
                                    .getTypeElement("java.util.Map").asType();
                            TypeMirror rawType = typeUtils.erasure(type);
                            String rawTypeName = String.valueOf(rawType);
                            if (typeUtils.isAssignable(collectionType, rawType)
                                    || typeUtils.isAssignable(listType, rawType)
                                    || typeUtils.isAssignable(setType, rawType)
                                    || "java.util.ArrayList".equals(rawTypeName)
                                    || "java.util.LinkedList".equals(rawTypeName)
                                    || "java.util.HashSet".equals(rawTypeName)
                                    || "java.util.TreeSet".equals(rawTypeName)) {
                                Type valueType = type.allparams().get(0);
                                builderMethods.add(MethodSpec.methodBuilder(makeElementAddName(name, type))
                                                .addJavadoc(javadoc)
                                                .addModifiers(Modifier.PUBLIC)
                                                .returns(inheritable ? typeVariableName : builderClassName)
                                                .addParameter(TypeName.get(valueType), "value")
                                                .addStatement("$N.add(value)", name)
                                                .addStatement(inheritable
                                                              ? "return (" + typeVariableName.name + ") this"
                                                              : "return this")
                                                .build());
                            } else if (typeUtils.isAssignable(mapType, rawType)
                                    || "java.util.HashMap".equals(rawTypeName)
                                    || "java.util.LinkedHashMap".equals(rawTypeName)
                                    || "java.util.WeakHashMap".equals(rawTypeName)
                                    || "java.util.ConcurrentHashMap".equals(rawTypeName)
                                    || "java.util.TreeMap".equals(rawTypeName)) {
                                Type keyType = type.allparams().get(0);
                                Type valueType = type.allparams().get(1);
                                builderMethods.add(MethodSpec.methodBuilder(makeElementAddName(name, type))
                                                .addJavadoc(javadoc)
                                                .addModifiers(Modifier.PUBLIC)
                                                .returns(inheritable ? typeVariableName : builderClassName)
                                                .addParameter(TypeName.get(keyType), "key")
                                                .addParameter(TypeName.get(valueType), "value")
                                                .addStatement("$N.put(key, value)", name)
                                                .addStatement(inheritable
                                                              ? "return (" + typeVariableName.name + ") this"
                                                              : "return this")
                                                .build());
                            }
                        }
                    }
                }
                    break;
                case METHOD: { // just copy
                    JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) member;
                    copyMethod(targetMethods, methodDecl, elementUtils.getDocComment(methodDecl.sym));
                }
                    break;
                case CLASS: {
                    if (nested) {
                        JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) member;
                        String simpleName = classDecl.getSimpleName().toString();
                        if (!classDecl.getModifiers().getFlags().contains(Modifier.STATIC)) {
                            messager.printMessage(Diagnostic.Kind.WARNING, "Will skip " + simpleName);
                            break;
                        }
                        nestTypes.add(doProcess(targetClassName.nestedClass(simpleName), classDecl.sym,
                                typeUtils, elementUtils, messager, nameResolveMap,
                                useGetter, inheritable, nested, elementAdder, join));
                    }
                }
                    break;
            }
        }

        if (!builderMethods.isEmpty()) {
            // public static Builder builder()
            targetMethods.add(MethodSpec.methodBuilder(builderParamName)
                                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                        .returns(builderClassName)
                                        .addStatement("return new $N()", builderClassName.simpleName())
                                        .build());
            // public XXX()
            targetMethods.add(MethodSpec.constructorBuilder()
                                        .addModifiers(Modifier.PUBLIC)
                                        .addStatement("this(new $N())", builderClassName.simpleName())
                                        .build());
            // private XXX(Builder)
            targetMethods.add(MethodSpec.constructorBuilder()
                                        .addModifiers(Modifier.PRIVATE)
                                        .addParameter(builderClassName, builderParamName)
                                        .addCode(targetCopyCode.build())
                                        .build());
            // public Builder newBuilder()
            targetMethods.add(MethodSpec.methodBuilder("new" + builderClassName.simpleName())
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(builderClassName)
                                        .addStatement("return new $N(this)", builderClassName.simpleName())
                                        .build());
            // public Builder()
            builderMethods.add(MethodSpec.constructorBuilder()
                                         .addModifiers(Modifier.PUBLIC)
                                         .addCode(builderDefaultCode.build())
                                         .build());
            // private Builder(XXX)
            builderMethods.add(MethodSpec.constructorBuilder()
                                         .addModifiers(Modifier.PRIVATE)
                                         .addParameter(targetClassName, targetParamName)
                                         .addCode(builderCopyCode.build())
                                         .build());
            // public XXX build()
            builderMethods.add(MethodSpec.methodBuilder("build")
                                         .addModifiers(Modifier.PUBLIC)
                                         .returns(targetClassName)
                                         .addStatement("return new $N(this)", targetClassName.simpleName())
                                         .build());

            for (String s : join) {
                s = s.substring(0, s.lastIndexOf(".class"));
                ClassName ownerClass = ClassName.bestGuess(s);
                String ownerParam = makeParamName(ownerClass.simpleName());
                // private XXX xxx;
                builderFields.add(FieldSpec.builder(ownerClass, ownerParam, Modifier.PRIVATE).build());
                // public Builder setXXX(XXX xxx)
                builderMethods.add(MethodSpec.methodBuilder("set" + ownerClass.simpleName())
                                             .addModifiers(Modifier.PUBLIC)
                                             .addParameter(ownerClass, ownerParam)
                                             .returns(inheritable ? typeVariableName : builderClassName)
                                             .addStatement("this.$N = $N", ownerParam, ownerParam)
                                             .addStatement(inheritable
                                                           ? "return (" + typeVariableName.name + ") this"
                                                           : "return this")
                                             .build());
                // public XXX intoXXX()
                builderMethods.add(MethodSpec.methodBuilder("into" + ownerClass.simpleName())
                                             .addModifiers(Modifier.PUBLIC)
                                             .returns(ownerClass)
                                             .addStatement("this.$N.set$N(build())",
                                                     ownerParam, targetClassName.simpleName())
                                             .addStatement("return $N", ownerParam)
                                             .build());
            }

            // create Builder type
            TypeSpec.Builder builderBuilder = TypeSpec.classBuilder(builderClassName)
                                                      .addJavadoc("@see $N\n", targetClassName.simpleName())
                                                      .addModifiers(makeClassModifiers(true, !inheritable))
                                                      .addFields(builderFields)
                                                      .addMethods(builderMethods);
            if (inheritable) {
                builderBuilder.addTypeVariable(typeVariableName);
            }
            nestTypes.add(builderBuilder.build());
        }

        // create XXX type
        List<TypeName> interfaces = new ArrayList<>();
        for (TypeMirror typeMirror : typeElement.getInterfaces()) {
            interfaces.add(TypeName.get(typeMirror));
        }
        List<TypeVariableName> typeParameters = new ArrayList<>();
        for (TypeParameterElement typeParameter : typeElement.getTypeParameters()) {
            typeParameters.add(TypeVariableName.get(typeParameter));
        }
        String classDoc = elementUtils.getDocComment(typeElement);
        return TypeSpec.classBuilder(targetClassName)
                       .addJavadoc(classDoc == null ? "" : classDoc)
                       .addModifiers(makeClassModifiers(
                               typeElement.getNestingKind() != NestingKind.TOP_LEVEL,
                               !inheritable))
                       .addAnnotations(convertAnnotations(typeElement,
                               ElementType.TYPE, GenBuilder.class))
                       .superclass(TypeName.get(typeElement.getSuperclass()))
                       .addSuperinterfaces(interfaces)
                       .addTypeVariables(typeParameters)
                       .addFields(targetFields)
                       .addMethods(targetMethods)
                       .addTypes(nestTypes)
                       .build();
    }

    private static Object extraMemberInAnnotation(TypeElement typeElement,
                                                  Class<?> annotationClass,
                                                  String annotationMemberName) {
        String nameGenBuilder = annotationClass.getName();
        AnnotationValue annotationValue = null;
    outer:
        for (AnnotationMirror am : typeElement.getAnnotationMirrors()) {
            if (nameGenBuilder.equals(am.getAnnotationType().toString())) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                        am.getElementValues().entrySet()) {
                    if (annotationMemberName.equals(entry.getKey().getSimpleName().toString())) {
                        annotationValue = entry.getValue();
                        break outer;
                    }
                }
            }
        }
        if (annotationValue != null) {
            return annotationValue.getValue();
        }
        return null;
    }

    private static Map<String, String> resolveNameSymbols(Trees trees, TypeElement typeElement) {
        Map<String, String> nameResolveMap = new HashMap<>();
        CompilationUnitTree compilationUnit = trees.getPath(typeElement).getCompilationUnit();
        nameResolveMap.put("package", compilationUnit.getPackageName().toString());
        for (ImportTree importTree : compilationUnit.getImports()) {
            String qualified = importTree.getQualifiedIdentifier().toString();
            nameResolveMap.put(qualified.substring(qualified.lastIndexOf(".") + 1), qualified);
        }
        return nameResolveMap;
    }

    private static String convertInitValueExpr(JCTree.JCExpression initializer,
                                               Map<String, String> nameResolveMap) {
        if (initializer == null) {
            return "null";
        }
        if (JCTree.Tag.LITERAL.equals(initializer.getTag())) {
            return initializer.toString();
        }
        String value = initializer.toString();
        final List<String> idents = new ArrayList<>();
        initializer.accept(new TreeScanner() {
            @Override
            public void visitIdent(final JCTree.JCIdent ident) {
                idents.add(ident.getName().toString());
            }
        });
        for (String ident : idents) {
            String replace = nameResolveMap.get(ident);
            if (replace != null && !value.contains(replace)) {
                value = value.replace(ident, replace);
            }
        }
        return value;
    }

    private static List<AnnotationSpec> convertAnnotations(AnnotatedConstruct annotatedConstruct, ElementType type,
                                                           Class<?>... classes) {
        ArrayList<AnnotationSpec> annotationSpecs = new ArrayList<>();
    outer:
        for (AnnotationMirror annotation : annotatedConstruct.getAnnotationMirrors()) {
            DeclaredType annotationType = annotation.getAnnotationType();
            for (Class<?> klass : classes) {
                if (klass.isAnnotation()
                        && klass.getCanonicalName().equals(String.valueOf(annotationType))) {
                    continue outer;
                }
            }
            if (annotationType.getKind() == TypeKind.DECLARED) {
                Target target = annotationType.asElement().getAnnotation(Target.class);
                if (target != null) {
                    for (ElementType elementType : target.value()) {
                        if (elementType.equals(type)) {
                            annotationSpecs.add(AnnotationSpec.get(annotation));
                        }
                    }
                } else {
                    // If a Target meta-annotation is not present on an annotation type declaration,
                    // the declared type may be used on any program element.
                    annotationSpecs.add(AnnotationSpec.get(annotation));
                }
            }
        }
        return annotationSpecs;
    }

    private static void copyMethod(List<MethodSpec> targetMethods, JCTree.JCMethodDecl methodDecl, String doc) {
        String methodName = methodDecl.getName().toString();
        if ("<init>".equals(methodName)) {
            return;
        }

        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        boolean varargs = false;
        for (JCTree.JCVariableDecl parameter : methodDecl.getParameters()) {
            if (parameter.vartype != null) {
                varargs = true;
                parameterSpecs.add(ParameterSpec.builder(ArrayTypeName.get(parameter.vartype.type),
                        parameter.name.toString(),
                        parameter.getModifiers().getFlags().toArray(EMPTY_MODIFIERS))
                                .addAnnotations(convertAnnotations(parameter.sym, ElementType.PARAMETER))
                                .build());
            } else {
                parameterSpecs.add(ParameterSpec.builder(TypeName.get(parameter.type),
                        parameter.name.toString(),
                        parameter.getModifiers().getFlags().toArray(EMPTY_MODIFIERS))
                                .addAnnotations(convertAnnotations(parameter.sym, ElementType.PARAMETER))
                                .build());
            }
        }

        List<TypeName> thrownTypeNames = new ArrayList<>();
        for (Type thrownType : methodDecl.sym.getThrownTypes()) {
            thrownTypeNames.add(TypeName.get(thrownType));
        }

        List<TypeVariableName> typeVariables = new ArrayList<>();
        for (JCTree.JCTypeParameter typeParameter : methodDecl.getTypeParameters()) {
            typeVariables.add(TypeVariableName.get(typeParameter.getName().toString()));
        }

        String bodyText = makeBodyBlock(methodDecl.getBody().toString());
        targetMethods.add(MethodSpec.methodBuilder(methodName)
                                    .addJavadoc(doc == null ? "" : doc)
                                    .addAnnotations(convertAnnotations(methodDecl.sym, ElementType.METHOD))
                                    .addModifiers(methodDecl.getModifiers().getFlags())
                                    .addTypeVariables(typeVariables)
                                    .returns(TypeName.get(methodDecl.getReturnType().type))
                                    .addParameters(parameterSpecs)
                                    .varargs(varargs)
                                    .addExceptions(thrownTypeNames)
                                    .addCode(bodyText)
                                    .build());
    }

    private static ClassName makeTargetName(String qualifiedName, String simpleName, String packageName) {
        final String targetSimpleName;
        if (simpleName.endsWith("Desc") || simpleName.endsWith("Meta")) {
            targetSimpleName = simpleName.substring(0, simpleName.length() - 4);
        } else if (simpleName.endsWith("Outline")) {
            targetSimpleName = simpleName.substring(0, simpleName.length() - 7);
        } else {
            targetSimpleName = simpleName + "Impl";
        }
        return ClassName.get(packageName, targetSimpleName);
    }

    private static String makeBeanName(String fieldName, boolean getter, TypeMirror type) {
        if ("boolean".equals(type.toString()) && (fieldName.startsWith("is") || fieldName.startsWith("has"))) {
            return fieldName;
        }
        String capName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        if (getter) {
            return "get" + capName;
        }
        return "set" + capName;
    }

    private static String makeElementAddName(String fieldName, TypeMirror type) {
        String capName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        return "addTo" + capName;
    }

    private static String makeParamName(String typeSimpleName) {
        return Character.toLowerCase(typeSimpleName.charAt(0)) + typeSimpleName.substring(1);
    }

    private static String makeBodyBlock(String body) {
        if (body == null || body.isEmpty()) {
            return "";
        }
        int index = 0;
        char[] chars = body.toCharArray();
        int length = chars.length;
        for (int i = 0; i < length; ++i) {
            if (Character.isWhitespace(chars[i]) || chars[i] == '{') {
                continue;
            }
            index = i;
            break;
        }
        return body.substring(index, body.lastIndexOf('}'));
    }

    private static Modifier[] makeClassModifiers(boolean isStatic, boolean isFinal) {
        ArrayList<Modifier> modifiers = new ArrayList<>();
        modifiers.add(Modifier.PUBLIC);
        if (isStatic) {
            modifiers.add(Modifier.STATIC);
        }
        if (isFinal) {
            modifiers.add(Modifier.FINAL);
        }
        return modifiers.toArray(EMPTY_MODIFIERS);
    }

    private static final Modifier[] EMPTY_MODIFIERS = new Modifier[0];
}
