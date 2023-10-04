module plugin.di.gen {
    requires plugin.api;
    requires jakarta.inject;
    requires plugin.di;

    requires java.compiler;
    requires com.squareup.javapoet;

    provides javax.annotation.processing.Processor with no.hal.plugin.di.gen.GeneratorProcessor;
}
