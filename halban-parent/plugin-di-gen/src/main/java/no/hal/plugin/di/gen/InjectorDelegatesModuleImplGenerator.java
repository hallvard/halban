package no.hal.plugin.di.gen;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import no.hal.plugin.di.DelegatingInjector;
import no.hal.plugin.di.InjectorDelegate;

public class InjectorDelegatesModuleImplGenerator {

    private static final String INJECTOR_DELEGATES_MODULE_IMPL_NAME = "InjectorDelegatesModuleImpl";

    private final Collection<InjectorDelegateImplGenerator> injectorDelegateGenerators;

    private final String packageName;

    public InjectorDelegatesModuleImplGenerator(Collection<InjectorDelegateImplGenerator> injectorDelegateGenerators) {
        this.injectorDelegateGenerators = injectorDelegateGenerators;
        String shortestPackageName = null;
        for (var injectorDelegateGenerator : injectorDelegateGenerators) {
            String injectorDelegatePackageName = injectorDelegateGenerator.getElementPackageName();
            if (shortestPackageName == null || injectorDelegatePackageName.length() < shortestPackageName.length()) {
                shortestPackageName = injectorDelegatePackageName;
            }
        }
        this.packageName = shortestPackageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public JavaFile generateInjectorDelegatesModule() {
        var injectorDelegates = injectorDelegateGenerators.stream()
            .map(InjectorDelegateImplGenerator::generateInjectorDelegate)
            .toList();
        TypeSpec injectorDelegatesModule = TypeSpec.classBuilder(INJECTOR_DELEGATES_MODULE_IMPL_NAME)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(ClassName.get(DelegatingInjector.Module.class))
            .addMethod(
                MethodSpec.methodBuilder("getInjectorDelegates")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ArrayTypeName.of(ClassName.get(InjectorDelegate.class)))
                    .addStatement("return new InjectorDelegate[]{\n" + 
                        injectorDelegates.stream()
                            .map(injectorDelegate -> "           new " + injectorDelegate.name + "()")
                            .collect(Collectors.joining(",\n"))
                        + "}")
                    .build()
            )
            .addTypes(
                injectorDelegates
            )
            .build();
        return JavaFile.builder(packageName, injectorDelegatesModule).build();
    }
}
