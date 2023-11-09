package no.hal.plugin.di.gen;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner14;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import no.hal.plugin.di.Injector;
import no.hal.plugin.di.annotation.Component;
import no.hal.plugin.di.annotation.Reference;
import no.hal.plugin.di.annotation.Scoped;

public class InjectorDelegateImplGenerator {

    private static final String INJECTOR_VAR_NAME = "_injector";
    private static final String QUALIFIER_VAR_NAME = "_qualifier";
    private static final String SCOPE_VAR_NAME = "_scope";
    private static final String INJECTOR_DELEGATE_IMPL_SUFFIX = "InjectorDelegateImpl";

    private final Element typeElement;
    private final Map<Element, Collection<TypeElement>> eltAnnotations;

    public InjectorDelegateImplGenerator(Element typeElement, Map<Element, Collection<TypeElement>> eltAnnotations) {
        this.typeElement = typeElement;
        this.eltAnnotations = eltAnnotations;
    }

    private String getElementSimpleName() {
        return typeElement.getSimpleName().toString();
    }
    private String getElementQualifiedName() {
        return typeElement.toString();
    }
    public String getElementPackageName() {
        var qName = getElementQualifiedName();
        return qName.substring(0, qName.lastIndexOf('.'));
    }
    private String getInjectorDelegateSimpleName() {
        return getElementSimpleName() + INJECTOR_DELEGATE_IMPL_SUFFIX;
    }

    public TypeSpec generateInjectorDelegate() {
        TypeName beanTypeName = ClassName.get(getElementPackageName(), getElementSimpleName());
        var classBuilder = TypeSpec.classBuilder(getInjectorDelegateSimpleName())
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .superclass(ParameterizedTypeName.get(ClassName.get("no.hal.plugin.di", "AbstractInjectorDelegate"), beanTypeName))
            .addMethod(
                methodImplBuilder("forClass")
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), beanTypeName))
                    .addStatement("return $T.class", beanTypeName)
                    .build()
            );
        var scopeProvider = getScopeProvider();
        if (scopeProvider != null) {
            classBuilder.addMethod(buildGetMethod(beanTypeName, scopeProvider));
        }
        classBuilder.addMethod(buildCreateMethod(beanTypeName, scopeProvider));
        classBuilder.addMethod(buildInjectMethod(beanTypeName));

        return classBuilder.build();
    }

    private Consumer<MethodSpec.Builder> getScopeProvider() {
        if (typeElement.getAnnotation(Singleton.class) != null) {
            return methodBuilder -> methodBuilder.addStatement("var $L = Injector.globalScope()", SCOPE_VAR_NAME);
        } else {
            var scoped = typeElement.getAnnotation(Scoped.class);
            if (scoped != null) {
                TypeMirror typeMirror = null;
                try {
                    scoped.value(); // this should throw
                } catch(MirroredTypeException mte) {
                    typeMirror = mte.getTypeMirror();
                }
                TypeMirror scopeType = typeMirror;
                return methodBuilder -> methodBuilder.addStatement("var $L = Injector.scopeFor($T.class)", SCOPE_VAR_NAME, scopeType);
            }
        }
        return null;
    }

    private MethodSpec buildGetMethod(TypeName beanTypeName, Consumer<MethodSpec.Builder> scopeProvider) {
        var methodBuilder = methodImplBuilder("getInstance")
            .returns(beanTypeName)
            .addParameter(ClassName.get(Injector.class), INJECTOR_VAR_NAME)
            .addParameter(ClassName.get(Object.class), QUALIFIER_VAR_NAME);
        scopeProvider.accept(methodBuilder);
        methodBuilder.addStatement("return $L.getInstance($T.class, null, $L)", INJECTOR_VAR_NAME, beanTypeName, SCOPE_VAR_NAME);
        return methodBuilder.build();
    }

    private MethodSpec buildCreateMethod(TypeName beanTypeName, Consumer<MethodSpec.Builder> scopeProvider) {
        var methodBuilder = methodImplBuilder("createInstance")
            .returns(beanTypeName)
            .addParameter(ClassName.get(Injector.class), INJECTOR_VAR_NAME)
            .addParameter(ClassName.get(Object.class), QUALIFIER_VAR_NAME);
        var cons = findInjectableConstructor();
        String instanceVarName = "_a" + getElementSimpleName();
        addInjectableCall(cons, "var " + instanceVarName + " = new $T($L)", beanTypeName, methodBuilder);
        String scope = "null";
        if (scopeProvider != null) {
            scopeProvider.accept(methodBuilder);
            scope = SCOPE_VAR_NAME;
        }
        methodBuilder.addStatement("$L.registerInstance($L, $T.class, $L, $L)", INJECTOR_VAR_NAME, instanceVarName, beanTypeName, QUALIFIER_VAR_NAME, scope);
        methodBuilder.addStatement("return " + instanceVarName);
        return methodBuilder.build();
    }

    private MethodSpec buildInjectMethod(TypeName beanTypeName) {
        var methodBuilder = methodImplBuilder("injectIntoInstance")
            .returns(boolean.class)
            .addParameter(beanTypeName, "_bean")
            .addParameter(ClassName.get(Injector.class), INJECTOR_VAR_NAME);
        for (var field : findInjectableFields()) {
            addInjectableFieldAssignment(field, "_bean", methodBuilder);
        }
        for (var method : findInjectableMethods()) {
            addInjectableCall(method, "_bean.$L($L)", method.getSimpleName(), methodBuilder);
        }
        methodBuilder.addStatement("return true");
        return methodBuilder.build();
    }

    private TypeElement getAnnotation(Class<? extends Annotation> annotationClass, Collection<TypeElement> annotations) {
        if (annotations != null) {
            for (var ann : annotations) {
                if (ann.getQualifiedName().toString().equals(annotationClass.getName())) {
                    return ann;
                }
            }
        }
        return null;
    }
    private boolean hasAnnotation(Class<? extends Annotation> annotationClass, Collection<TypeElement> annotations) {
        return getAnnotation(annotationClass, annotations) != null;
    }
    private boolean hasAnnotation(Class<? extends Annotation> annotationClass, Map.Entry<Element, Collection<TypeElement>> entry) {
        return hasAnnotation(annotationClass, entry.getValue());
    }

    private Element findInjectableConstructor() {
        var injectables = injectableMethods(true).toList();
        if (injectables.isEmpty()) {
            Collection<Element> constructors = getElementsOfKind(typeElement, ElementKind.CONSTRUCTOR).stream()
                .filter(element -> element.getEnclosingElement() == typeElement)
                .toList();
            if (constructors.isEmpty()) {
                throw new RuntimeException("No injectable constructor found in " + typeElement);
            } else if (constructors.size() > 1) {
                throw new RuntimeException("More than one injectable constructor found in " + typeElement + ": " + constructors);
            }
            return constructors.iterator().next();
        }
        if (injectables.size() > 1) {
            throw new RuntimeException("More than one injectable constructor found in " + typeElement + ": " + injectables);
        }
        return injectables.get(0).getKey();
    }

    private Collection<Element> findInjectableMethods() {
        return injectableMethods(false)
        .map(elEntry -> elEntry.getKey())
        .toList();
    }

    private Collection<Element> findInjectableFields() {
        return injectableFields()
        .map(elEntry -> elEntry.getKey())
        .toList();
    }

    private Stream<Map.Entry<Element, Collection<TypeElement>>> injectableMethods(boolean isConstructor) {
        return eltAnnotations.entrySet().stream()
            .filter(eltEntry -> eltEntry.getKey().getKind() == (isConstructor ? ElementKind.CONSTRUCTOR : ElementKind.METHOD))
            .filter(eltEntry -> hasAnnotation(Inject.class, eltEntry) ||
                                    isConstructor ? hasAnnotation(Component.class, eltEntry) : hasAnnotation(Reference.class, eltEntry));
    }
    private Stream<Map.Entry<Element, Collection<TypeElement>>> injectableFields() {
        return eltAnnotations.entrySet().stream()
            .filter(eltEntry -> eltEntry.getKey().getKind() == ElementKind.FIELD)
            .filter(eltEntry -> hasAnnotation(Inject.class, eltEntry) || hasAnnotation(Reference.class, eltEntry));
    }

    private Stream<Map.Entry<Element, Collection<TypeElement>>> injectableClasses() {
        return eltAnnotations.entrySet().stream()
            .filter(eltEntry -> eltEntry.getKey().getKind() == ElementKind.CLASS)
            .filter(eltEntry -> hasAnnotation(Inject.class, eltEntry) || hasAnnotation(Component.class, eltEntry) || hasAnnotation(Scoped.class, eltEntry));
    }

    private MethodSpec.Builder methodImplBuilder(String methodName) {
        return MethodSpec.methodBuilder(methodName)
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC);
    }

    private String getQualifierArg(Element element) {
        Named named = element.getAnnotation(Named.class);
        String qualifier = "null";
        if (named != null) {
            qualifier = "\"" + named.value() + "\"";
        }
        return qualifier;
    }

    private void addInjectableFieldAssignment(Element field, Object receiver, MethodSpec.Builder methodBuilder) {
        String fieldName = field.getSimpleName().toString();
        var fieldType = field.asType();
        String qualifier = getQualifierArg(field);
        methodBuilder.addStatement("$L.$L = $L.provideInstance($T.class, $L)", receiver, fieldName, INJECTOR_VAR_NAME, fieldType, qualifier);
    }

    private void addInjectableCall(Element executable, String format, Object receiver, MethodSpec.Builder methodBuilder) {
        var argNames = addInjectableArgs(executable, methodBuilder);
        methodBuilder.addStatement(format, receiver, argNames.stream().collect(Collectors.joining(", ")));
    }

    private Collection<String> addInjectableArgs(Element elt, MethodSpec.Builder methodBuilder) {
        Collection<Element> parameters = getElementsOfKind(elt, ElementKind.PARAMETER);
        Collection<String> argNames = new ArrayList<>();
        String argNamePrefix = "_" + (elt.getKind() != ElementKind.CONSTRUCTOR ? elt.getSimpleName() + "_" : "");
        for (var param : parameters) {
            String argName = argNamePrefix + "arg" + (argNames.size() + 1);
            var paramType = param.asType();
            String qualifier = getQualifierArg(param);
            methodBuilder.addStatement("$T $L = $L.provideInstance($T.class, $L)", paramType, argName, INJECTOR_VAR_NAME, paramType, qualifier);
            argNames.add(argName);
        }
        return argNames;
    }

    private Collection<Element> getElementsOfKind(Element elt, ElementKind kind) {
        Collection<Element> elements = new ArrayList<>();
        ElementVisitor<Void, Void> visitor = new ElementScanner14<>() {
            @Override
            public Void scan(Element e, Void p) {
                if (e.getKind() == kind) {
                    elements.add(e);
                } else {
                    super.scan(e, p);
                }
                return null;
            }
        };
        visitor.visit(elt);
        return elements;
    }
}
