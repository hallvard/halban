package no.hal.sokoban.app.settings;

import no.hal.plugin.di.AbstractInjectorDelegate;
import no.hal.plugin.di.DelegatingInjector;
import no.hal.plugin.di.Injector;
import no.hal.plugin.di.InjectorDelegate;
import no.hal.settings.Setting.Value;
import no.hal.settings.Settings;

public class SettingsInjectorDelegatesModule implements DelegatingInjector.Module {

  @Override
  public InjectorDelegate[] getInjectorDelegates() {
    return new InjectorDelegate[]{new ValueInjectorDelegate()};
  }

  private static class ValueInjectorDelegate extends AbstractInjectorDelegate<Value> {
    @Override
    public Class<Value> forClass() {
      return Value.class;
    }

    @Override
    public Value getInstance(Injector injector, Object qualifier) {
      Settings settings = injector.getInstance(Settings.class, null, Injector.localScope());
      return settings.getValue(qualifier.toString());
    }

    @Override
    public Value createInstance(Injector _injector, Object _qualifier) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean injectIntoInstance(Value _bean, Injector _injector) {
        throw new UnsupportedOperationException();
    }
  }
}