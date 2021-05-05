package io.servertap.mojang.api.models;

import java.util.List;

public class NameHistory {
    private List<NameChange> nameChanges;

    public NameHistory(List<NameChange> nameChanges) {
        setNameChanges(nameChanges);
    }

    public List<NameChange> getNameChanges() {
        return nameChanges;
    }

    public void setNameChanges(List<NameChange> nameChanges) {
        this.nameChanges = nameChanges;
    }
}
