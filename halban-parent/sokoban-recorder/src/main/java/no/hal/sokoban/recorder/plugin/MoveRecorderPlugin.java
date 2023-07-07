package no.hal.sokoban.recorder.plugin;

import java.util.Collection;
import java.util.List;

import no.hal.plugin.Contribution;
import no.hal.plugin.Plugin;

public class MoveRecorderPlugin implements Plugin {
    
    @Override
    public Collection<Contribution> getContributions() {
        return List.of(new MoveRecorderContribution());
    }
}
