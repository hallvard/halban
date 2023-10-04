package no.hal.plugin.di.gen;

import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({
    "no.hal.plugin.di.annotation.Service",
    "no.hal.plugin.di.annotation.Reference",
    "no.hal.plugin.di.annotation.Scope",

    "jakarta.inject.Inject",
    "jakarta.inject.Singleton"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class GeneratorProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<Element, Map<Element, Collection<TypeElement>>> typeAnnotations = new HashMap<>();
        for (var annotation : annotations) {
            for (var elt : roundEnv.getElementsAnnotatedWith(annotation)) {
                Element typeElement = switch(elt.getKind()) {
                    case CLASS -> elt;
                    case CONSTRUCTOR -> elt.getEnclosingElement();
                    case METHOD ->  elt.getEnclosingElement();
                    case PARAMETER -> elt.getEnclosingElement().getEnclosingElement();
                    default -> null;
                };
                if (typeElement != null) {
                    var elements = typeAnnotations.get(typeElement);
                    if (elements == null) {
                        elements = new HashMap<>();
                        typeAnnotations.put(typeElement, elements);
                    }
                    var eltAnnotations = elements.get(elt);
                    if (eltAnnotations == null) {
                        eltAnnotations = new ArrayList<>();
                        elements.put(elt, eltAnnotations);
                    }
                    eltAnnotations.add(annotation);
                }
            }
        }
        for (var typeEntry : typeAnnotations.entrySet()) {
            Element typeElement = typeEntry.getKey();
            InjectorDelegateImplGenerator injectorDelegateImplGenerator = new InjectorDelegateImplGenerator(typeElement, typeEntry.getValue());
            JavaFile injectorDelegate = injectorDelegateImplGenerator.generateInjectorDelegate();
            try {
                var sourceFile = processingEnv.getFiler().createSourceFile(injectorDelegateImplGenerator.getInjectorDelegateQualifiedName(), typeElement);
                try (PrintWriter out = new PrintWriter(sourceFile.openWriter())) {
                    injectorDelegate.writeTo(out);
                } catch (IOException ioex) {
                    System.err.println("Exception writing file for " + injectorDelegate + ": " + ioex);
                }
            } catch (IOException ioex) {
                System.err.println("Exception create source file for " + injectorDelegate.typeSpec + ": " + ioex);
            }
        }
        return true;
    }
}
