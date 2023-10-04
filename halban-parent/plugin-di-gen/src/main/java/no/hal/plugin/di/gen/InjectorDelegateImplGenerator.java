package no.hal.plugin.di.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementScanner14;

import no.hal.plugin.di.annotation.Reference;
import no.hal.plugin.di.annotation.Scoped;
import no.hal.plugin.di.annotation.Component;

public class InjectorDelegateImplGenerator {

    private static final String INJECTOR_VAR_NAME = "_injector";
    private static final String INJECTOR_DELEGATE_IMPL_SUFFIX = "InjectorDelegateImpl";

    private Element typeElement;
    private Map<Element, Collection<TypeElement>> eltAnnotations;

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
    private String getElementPackageName() {
        var qName = getElementQualifiedName();
        return qName.substring(0, qName.lastIndexOf('.'));
    }
    String getInjectorDelegateQualifiedName() {
        return getElementQualifiedName() + INJECTOR_DELEGATE_IMPL_SUFFIX;
    }
    private String getInjectorDelegateSimpleName() {
        return getElementSimpleName() + INJECTOR_DELEGATE_IMPL_SUFFIX;
    }

    public JavaFile generateInjectorDelegate() {
        System.out.println("Generating InjectorDelegate for " + typeElement);
        System.out.println("Annotations: " + eltAnnotations);
        TypeName beanTypeName = ClassName.get(getElementPackageName(), getElementSimpleName());
        TypeSpec injectorDelegate = TypeSpec.classBuilder(getInjectorDelegateSimpleName())
            .addModifiers(Modifier.PUBLIC)
            .superclass(ParameterizedTypeName.get(ClassName.get("no.hal.plugin.di", "AbstractInjectorDelegate"), beanTypeName))
            .addMethod(
                methodImplBuilder("forClass")
                    .returns(ParameterizedTypeName.get(ClassName.get(Class.class), beanTypeName))
                    .addStatement("return $T.class", beanTypeName)
                    .build()
            )
            .addMethod(
                buildCreateMethod(beanTypeName)
            )
            .addMethod(
                buildInjectMethod(beanTypeName)
            )
            .build();
        return JavaFile.builder("no.hal.plugin.di.sample", injectorDelegate).build();
    }

    private ClassName getInjectorInterfaceTypeName() {
        return ClassName.get("no.hal.plugin.di", "Injector");
    }

    private MethodSpec buildCreateMethod(TypeName beanTypeName) {
        var methodBuilder = methodImplBuilder("createInstance")
            .returns(beanTypeName)
            .addParameter(getInjectorInterfaceTypeName(), INJECTOR_VAR_NAME);
        var cons = findInjectableConstructor();
        addInjectableCall(cons, "return new $T($L)", beanTypeName, methodBuilder);
        return methodBuilder.build();
    }

    private MethodSpec buildInjectMethod(TypeName beanTypeName) {
        var methodBuilder = methodImplBuilder("injectIntoInstance")
            .returns(boolean.class)
            .addParameter(beanTypeName, "_bean")
            .addParameter(getInjectorInterfaceTypeName(), INJECTOR_VAR_NAME);
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
            Collection<Element> constructors = getElementsOfKind(typeElement, ElementKind.CONSTRUCTOR);
            if (constructors.isEmpty()) {
                throw new RuntimeException("No injectable constructor found in " + typeElement);
            } else if (constructors.size() > 1) {
                throw new RuntimeException("More than one injectable constructor found in " + typeElement);
            }
            return constructors.iterator().next();
        }
        if (injectables.size() > 1) {
            throw new RuntimeException("More than one injectable constructor found in " + typeElement);
        }
        return injectables.get(0).getKey();
    }

    private Collection<Element> findInjectableMethods() {
        return injectableMethods(false)
        .map(elEntry -> elEntry.getKey())
        .toList();
    }

    private Stream<Map.Entry<Element, Collection<TypeElement>>> injectableMethods(boolean isConstructor) {
        return eltAnnotations.entrySet().stream()
            .filter(eltEntry -> eltEntry.getKey().getKind() == (isConstructor ? ElementKind.CONSTRUCTOR : ElementKind.METHOD))
            .filter(eltEntry -> hasAnnotation(Inject.class, eltEntry) ||
                                    isConstructor ? hasAnnotation(Component.class, eltEntry) : hasAnnotation(Reference.class, eltEntry));
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
            Named named = param.getAnnotation(Named.class);
            if (named != null) {
                methodBuilder.addStatement("$T $L = $L.getInstance($T.class, $S)", paramType, argName, INJECTOR_VAR_NAME, paramType, named.value());
            } else {
                methodBuilder.addStatement("$T $L = $L.provideInstance($T.class, null)", paramType, argName, INJECTOR_VAR_NAME, paramType);
            }
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
